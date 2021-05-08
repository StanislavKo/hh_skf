package com.skf.rediscrud.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class DatabaseCleanupService implements InitializingBean {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public void afterPropertiesSet() {
    }

    /**
     * Utility method that truncates all identified tables
     */
    public void truncate() {
        redisTemplate.opsForList().trim("contentTimes", 0, 0);
        redisTemplate.opsForList().leftPop("contentTimes");
    }

}
