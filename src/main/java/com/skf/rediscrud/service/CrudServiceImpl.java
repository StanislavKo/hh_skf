package com.skf.rediscrud.service;

import com.skf.rediscrud.pojo.ContentSlice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CrudServiceImpl implements CrudService {

    private static final Logger logger = LoggerFactory.getLogger(CrudServiceImpl.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class LastTime {
        private Long ts;
        private Integer count;
    }

    private LastTime lastTime = new LastTime(System.currentTimeMillis(), 0);

    @Async
    @Override
    public void putContent(String content) {
        Long now = System.currentTimeMillis();

        // For high availability it's possible to use Spring Cloud Eureka clusters.
        // Take a look at an example (DATA, WEBSOCKET) in my project: https://www.linkedin.com/feed/update/urn:li:activity:6754052807495557120
        // In this architecture each node in a cluster will have unique id (if i remember correctly it's possible to get it in code here)
        // So just append this unique string to cluster node id to get distributed unique value
        String unique = "0";
        synchronized (CrudServiceImpl.class) {
            if (lastTime.ts.equals(now)) {
                unique = "" + ++lastTime.count;
            } else {
                lastTime.ts = now;
                lastTime.count = 0;
            }
        }
        redisTemplate.opsForList().rightPush("contentTimes", "" + now + "_" + unique);
        redisTemplate.opsForHash().put("contentObjs", "" + now + "_" + unique, content);
        redisTemplate.opsForValue().set("contentLast", content);
    }

    @Override
    public void putLabel(Long ts) {
        redisTemplate.opsForList().rightPush("contentTimes", ts + "_label");
    }

    @Override
    public String getLast() {
        return redisTemplate.opsForValue().get("contentLast");
    }

    @Override
    public ContentSlice getByTime(Long start, Long end) {
        logger.debug("[start:" + start + "], [end:" + end + "]");
        Long startKey = start / 1000 * 1000 - 2000;
        Long endKey = end / 1000 * 1000 + 3000;
        Long startPos = redisTemplate.opsForList().indexOf("contentTimes", startKey + "_label");
        Long endPos = redisTemplate.opsForList().indexOf("contentTimes", endKey + "_label");
        Long headPosTs = Long.valueOf(redisTemplate.opsForList().index("contentTimes", 0).split("_")[0]);
        Long tailPosTs = Long.valueOf(redisTemplate.opsForList().index("contentTimes", redisTemplate.opsForList().size("contentTimes") - 1).split("_")[0]);
        logger.debug("[headPosTs:" + headPosTs + "], [tailPosTs:" + tailPosTs + "]");
        if (startPos == null && startKey < headPosTs) {
            startPos = 0L;
        } else if (startPos == null && startKey > tailPosTs) {
            startPos = Long.MAX_VALUE;
        }
        if (endPos == null && endKey < headPosTs) {
            endPos = 0L;
        } else if (endPos == null && endKey > tailPosTs) {
            endPos = Long.MAX_VALUE;
        }
        logger.debug("[startPos:" + startPos + "], [endPos:" + endPos + "]");

        List<String> contentTses = redisTemplate.opsForList().range("contentTimes", startPos, endPos);
        List<Object> contentTses2 = contentTses.stream()
                .filter(x -> !x.contains("_label"))
                .filter(x -> (Long.valueOf(x.split("_")[0]) > start && Long.valueOf(x.split("_")[0]) < end))
                .collect(Collectors.toList());

        List<String> result = redisTemplate.opsForHash().multiGet("contentObjs", contentTses2).stream().map(x -> x.toString()).collect(Collectors.toList());
        ContentSlice contentSlice = new ContentSlice(result);
        return contentSlice;
    }

}
