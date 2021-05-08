package com.skf.rediscrud.controller;

import com.skf.rediscrud.service.DatabaseCleanupService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CrudControllerExistenceTest {

    private static final Logger logger = LoggerFactory.getLogger(CrudControllerExistenceTest.class);

    @Autowired
    private CrudController controller;

    @Autowired
    private DatabaseCleanupService databaseCleanupService;

    @AfterAll
    public void afterAll() {
        logger.info("Cleanup up the test database");
        databaseCleanupService.truncate();
    }

    @Test
    public void contextLoads() throws Exception {
        assertThat(controller).isNotNull();
    }

}
