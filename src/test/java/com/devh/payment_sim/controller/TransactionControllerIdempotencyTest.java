package com.devh.payment_sim.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.dto.SendMoneyRequest;
import com.devh.payment_sim.dto.response.IdempotencyResponse;
import com.devh.payment_sim.dto.response.TransactionResponse;
import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.security.CustomUserDetails;
import com.devh.payment_sim.model.Role;
import com.devh.payment_sim.service.IdempotencyService;
import com.devh.payment_sim.service.TransactionService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;

class TransactionControllerIdempotencyTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private IdempotencyService idempotencyService;

    private ObjectMapper objectMapper;

    private TransactionController controller;
    private CustomUserDetails user;

    private SendMoneyRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        controller = new TransactionController(transactionService, idempotencyService, objectMapper);

        user = CustomUserDetails.builder()
                .userId(1L)
                .username("tester")
                .password("pass")
                .role(Role.ROLE_USER)
                .build();

        request = new SendMoneyRequest();
        request.setFromUpiHandle("a@upi");
        request.setToUpiHandle("b@upi");
        request.setTransferAmount(BigDecimal.valueOf(100));
        request.setUpiPin("1234");
        request.setRemarks("ok");
    }

    @Test
    void sendMoney_returnsCached_whenKeyAlreadyUsed() throws Exception {
        String key = "key-123";

        // simulate existing cache entry with a serialized response
        TransactionResponse txResp = new TransactionResponse();
        txResp.setTransactionId("tx-1");
        txResp.setAmount(BigDecimal.valueOf(100));

        String body = objectMapper.writeValueAsString(txResp);

        when(idempotencyService.handleIdempotencyKey(key, user.getUserId(), "/api/transactions/transfer"))
                .thenReturn(IdempotencyResponse.builder()
                        .fromCache(true)
                        .statusCode(200)
                        .responseBody(body)
                        .build());

        // controller should not invoke service
        ResponseEntity<ApiResponse<TransactionResponse>> resp = controller.sendMoney(request, key, user);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("tx-1", resp.getBody().getData().getTransactionId());
        verify(transactionService, never()).sendMoney(any(), any(), any(), any(), any());
        verify(idempotencyService, never()).markSuccess(any(), any(), any());
    }

    @Test
    void sendMoney_storesSuccess_whenNewKey() throws Exception {
        String key = "new-key";

        when(idempotencyService.handleIdempotencyKey(key, user.getUserId(), "/api/transactions/transfer"))
                .thenReturn(IdempotencyResponse.builder().fromCache(false).build());

        Transaction tx = Transaction.builder().transactionId("tx-new").build();
        when(transactionService.sendMoney(any(), any(), any(), any(), any()))
                .thenReturn(tx);

        ResponseEntity<ApiResponse<TransactionResponse>> resp = controller.sendMoney(request, key, user);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("tx-new", resp.getBody().getData().getTransactionId());
        verify(idempotencyService).markSuccess(eq(key), anyString(), eq(200));
    }

    @Test
    void sendMoney_onException_marksFailure() {
        String key = "fail-key";
        when(idempotencyService.handleIdempotencyKey(key, user.getUserId(), "/api/transactions/transfer"))
                .thenReturn(IdempotencyResponse.builder().fromCache(false).build());

        when(transactionService.sendMoney(any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> controller.sendMoney(request, key, user));
        verify(idempotencyService).markFailure(eq(key), contains("boom"), eq(400));
    }
}
