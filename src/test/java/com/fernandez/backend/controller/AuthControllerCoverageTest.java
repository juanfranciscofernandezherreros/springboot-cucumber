package com.fernandez.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerCoverageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void logout_controller_is_executed_for_coverage() throws Exception {

        mockMvc.perform(
                post("/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer dummy-token")
        ).andExpect(status().isOk());
    }
}
