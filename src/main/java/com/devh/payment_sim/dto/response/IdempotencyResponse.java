package com.devh.payment_sim.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyResponse {
    private boolean fromCache;
    private Integer statusCode;
    private String responseBody;
}
