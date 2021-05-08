package com.skf.rediscrud.controller;

import com.skf.rediscrud.consts.Consts;
import com.skf.rediscrud.pojo.Visit;
import com.skf.rediscrud.pojo.PublishData;
import com.skf.rediscrud.pojo.ContentSlice;
import com.skf.rediscrud.service.AntiFloodService;
import com.skf.rediscrud.service.CrudService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1")
public class CrudController {

    private static final Logger logger = LoggerFactory.getLogger(CrudController.class);

    @Autowired
    private CrudService crudService;

    @Autowired
    private AntiFloodService antiFloodService;

    private Map<String, Visit> visits = new ConcurrentHashMap<>();

    @PostMapping(value = "/publish")
    public ResponseEntity<String> publish(@RequestBody PublishData publishData) {
        logger.debug("publish [data:" + publishData + "]");
        if (publishData == null) {
            logger.info("Null content");
            return new ResponseEntity("invalid_content", HttpStatus.BAD_REQUEST);
        }
        if (publishData.getContent() == null || publishData.getContent().isEmpty()) {
            logger.info("Empty content");
            return new ResponseEntity("invalid_content", HttpStatus.BAD_REQUEST);
        }
        if (publishData.getContent().length() > Consts.MAX_CONTENT_LENGTH) {
            logger.info("Invalid content [" + publishData.getContent().length() + "]");
            return new ResponseEntity("invalid_content", HttpStatus.BAD_REQUEST);
        }

        crudService.putContent(publishData.getContent());
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(value = "/getLast", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getLast(HttpServletRequest req) {
        logger.debug("getLast");
        if (!antiFloodService.isOk(req, visits)) {
            return new ResponseEntity("too_many_requests", HttpStatus.TOO_MANY_REQUESTS);
        }

        String content = crudService.getLast();
        return new ResponseEntity(content, HttpStatus.OK);
    }

    @GetMapping(value = "/getByTime")
    public ResponseEntity<String> getByTime(HttpServletRequest req, @RequestParam(value = "start", required = true) Long start,
                                            @RequestParam(value = "end", required = true) Long end) {
        logger.debug("getByTime");
        if (!antiFloodService.isOk(req, visits)) {
            return new ResponseEntity("too_many_requests", HttpStatus.TOO_MANY_REQUESTS);
        }
        if (start > end) {
            logger.info("Start is greater than end");
            return new ResponseEntity("Start_is_greater_than_end", HttpStatus.BAD_REQUEST);
        }

        ContentSlice contents = crudService.getByTime(start, end);
        return new ResponseEntity(contents, HttpStatus.OK);
    }

}