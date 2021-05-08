package com.skf.rediscrud.service;

import com.skf.rediscrud.consts.Consts;
import com.skf.rediscrud.pojo.Visit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AntiFloodServiceImpl implements AntiFloodService {

    private static final Logger logger = LoggerFactory.getLogger(AntiFloodServiceImpl.class);

    @Override
    public boolean isOk(HttpServletRequest req, Map<String, Visit> visits) {
        String ipAddress = req.getRemoteAddr();
        Visit visit = visits.get(ipAddress);
        if (visit == null) {
            visit = new Visit(new LinkedList<Long>());
            visits.put(ipAddress, visit);
        }

        synchronized (visit) {
            visit.setEnters(visit.getEnters().stream().filter(x -> x > System.currentTimeMillis() - 1000).collect(Collectors.toList()));
            visit.getEnters().add(System.currentTimeMillis());
        }

        if (visit.getEnters().size() > Consts.MAX_FLOOD_RATE) {
            return false;
        }

        return true;
    }
}
