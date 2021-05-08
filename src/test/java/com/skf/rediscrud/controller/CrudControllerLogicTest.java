package com.skf.rediscrud.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.Comparator;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class CrudControllerLogicTest {

    private static final Logger logger = LoggerFactory.getLogger(CrudControllerLogicTest.class);

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
    public void shouldReturnBadRequest() throws Exception {
        this.mockMvc.perform(post("/api/v1/publish").content("{\"content\":\"\"}").contentType("application/json")).andDo(print()).andExpect(status().is(400));
        this.mockMvc.perform(post("/api/v1/publish").content("{\"content\":\"content_too_long_too_long_too_long_too_long_too_long_too_long\"}").contentType("application/json")).andDo(print()).andExpect(status().is(400));
        this.mockMvc.perform(get("/api/v1/getByTime").param("start", "2").param("end", "1")).andDo(print()).andExpect(status().is(400));
    }

    @Test
    public void shouldReturnLastContent() throws Exception {
        this.mockMvc.perform(post("/api/v1/publish").content("{\"content\":\"111\"}").contentType("application/json")).andDo(print()).andExpect(status().isOk());
        this.mockMvc.perform(get("/api/v1/getLast")).andDo(print()).andExpect(status().isOk()).andExpect(content().contentType("text/plain;charset=UTF-8")).andExpect(content().string("111"));
    }

    @Test
    public void shouldReturnSlice() throws Exception {
        Thread.sleep(2_000);
        databaseCleanupService.truncate();
        Thread.sleep(4_000);
        Long startTs = null;
        Long endTs = null;
        for (int i = 0; i < 150; i++) {
            this.mockMvc.perform(post("/api/v1/publish").content("{\"content\":\"content_" + System.currentTimeMillis() + "\"}").contentType("application/json")).andExpect(status().isOk());
            if (i == 50) {
                startTs = System.currentTimeMillis();
            }
            if (i == 75) {
                endTs = System.currentTimeMillis();
            }
            Thread.sleep((int)(Math.random()*200));
        }
        ResultActions result = this.mockMvc.perform(get("/api/v1/getByTime").param("start", "" + startTs).param("end", "" + endTs)).andDo(print()).andExpect(status().isOk());
        String contentSliceStr = result.andReturn().getResponse().getContentAsString();
        ContentSlice contentSlice = new ObjectMapper().readValue(contentSliceStr, new TypeReference<ContentSlice>(){});

        Assert.assertEquals(25, contentSlice.getContents().size());
        Assert.assertTrue(contentSlice.getContents().stream().map(x -> Long.valueOf(x.split("_")[1])).mapToLong(x -> x).min().getAsLong() > startTs);
        Assert.assertTrue(contentSlice.getContents().stream().map(x -> Long.valueOf(x.split("_")[1])).mapToLong(x -> x).max().getAsLong() < endTs);
    }

}