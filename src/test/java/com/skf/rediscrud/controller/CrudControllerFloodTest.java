package com.skf.rediscrud.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skf.rediscrud.consts.Consts;
import com.skf.rediscrud.pojo.ContentSlice;
import com.skf.rediscrud.service.DatabaseCleanupService;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class CrudControllerFloodTest {

    private static final Logger logger = LoggerFactory.getLogger(CrudControllerFloodTest.class);

    @Autowired
    private DatabaseCleanupService databaseCleanupService;

    @Autowired
    private MockMvc mockMvc;

    @AfterAll
    public void afterAll() {
        logger.info("Cleanup up the test database");
        databaseCleanupService.truncate();
    }

    @Test
    public void shouldReturnTooManyRequests() throws Exception {
        this.mockMvc.perform(post("/api/v1/publish").content("{\"content\":\"111\"}").contentType("application/json")).andDo(print()).andExpect(status().isOk());
        for (int i = 0; i < Consts.MAX_FLOOD_RATE; i++) {
            this.mockMvc.perform(get("/api/v1/getLast")).andExpect(status().isOk()).andExpect(content().string("111"));
        }
        Assert.assertTrue(true);
        this.mockMvc.perform(get("/api/v1/getLast")).andExpect(status().is(429));
    }

}