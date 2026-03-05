package com.devh.payment_sim.service;

import com.devh.payment_sim.dto.response.IdempotencyResponse;

public interface IdempotencyService {
    public IdempotencyResponse handleIdempotencyKey(String key, Long userId, String endpoint);
    public void markSuccess(String key, String response, Integer statusCode);
    public void markFailure(String key, String errorMessage, Integer statusCode);
}