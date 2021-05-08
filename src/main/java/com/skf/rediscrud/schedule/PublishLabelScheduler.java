package com.skf.rediscrud.schedule;

import com.skf.rediscrud.service.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PublishLabelScheduler {

    private static final Logger logger = LoggerFactory.getLogger(PublishLabelScheduler.class);

    @Autowired
    private CrudService crudService;

    private Long initTs = System.currentTimeMillis() + 3_000;

    @Scheduled(cron = "* * * * * *") // every second
    public void publishLabel() {
        if (System.currentTimeMillis() > initTs) {
            Long now = System.currentTimeMillis() / 1000 * 1000;
            logger.debug("publishLabel() [now:" + now + "]");
            crudService.putLabel(now);
        }
    }

}
