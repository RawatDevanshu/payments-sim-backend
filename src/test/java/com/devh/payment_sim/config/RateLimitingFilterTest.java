package com.devh.payment_sim.config;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
public class RateLimitingFilterTest extends AbstractPostgresTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testRateLimiting_AllowsTenRequests_BlocksEleventh() throws Exception {
        String dummyLoginPayload = "{\"email\":\"nonexistent@example.com\",\"password\":\"password123\"}";

        // 1. The first 10 requests should proceed past the rate limiter.
        // Even if the login fails (e.g. 401 Unauthorized or 400 Bad Request),
        // it proves the rate limiter did NOT block it with a 429.
        for (int i = 0; i < 10; i++) {
            int requestNumber = i;
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(dummyLoginPayload))
                    .andExpect(result -> assertNotEquals(429, result.getResponse().getStatus(), "Request" + requestNumber + " should not be rate-limited")); // Should not be 429
        }

        // 2. The 11th request must be blocked by the rate limiter with HTTP 429
        mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(dummyLoginPayload))
            .andExpect(status().is(429)) // Expect HTTP 429 Too Many Requests
            .andExpect(jsonPath("$.success").value(false))

            .andExpect(jsonPath("$.message").value("Too many requests. Please try again later."));
    }
}