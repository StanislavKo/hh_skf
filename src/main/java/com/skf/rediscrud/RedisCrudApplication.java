package com.skf.rediscrud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.skf.rediscrud.controller,com.skf.rediscrud.service,com.skf.rediscrud.config,com.skf.rediscrud.schedule")
public class RedisCrudApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisCrudApplication.class, args);
    }

}