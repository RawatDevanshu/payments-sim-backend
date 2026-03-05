package com.devh.payment_sim.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.dto.WalletBankTransferRequest;
import com.devh.payment_sim.dto.response.IdempotencyResponse;
import com.devh.payment_sim.dto.response.TransactionResponse;
import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.security.CustomUserDetails;
import com.devh.payment_sim.model.Role;
import com.devh.payment_sim.service.IdempotencyService;
import com.devh.payment_sim.service.TransactionService;
import com.devh.payment_sim.service.WalletService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import org.springframework.http.ResponseEntity;

class WalletControllerIdempotencyTest {

    @Mock
    private WalletService walletService;

    @Mock
    private TransactionService transactionService;

    @Mock
    private IdempotencyService idempotencyService;

    private ObjectMapper objectMapper;

    private WalletController controller;
    private CustomUserDetails user;

    private WalletBankTransferRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        controller = new WalletController(walletService, transactionService, idempotencyService, objectMapper);

        user = CustomUserDetails.builder()
                .userId(1L)
                .username("tester")
                .password("pass")
                .role(Role.ROLE_USER)
                .build();

        request = new WalletBankTransferRequest();
        request.setWalletUpiHandle("wallet@upi");
        request.setBankAccountNumber("1234567890");
        request.setTransferAmount(BigDecimal.valueOf(100));
        request.setRawPin("1234");
        request.setRemarks("ok");
    }

    @Test
    void topUpWallet_returnsCached_whenKeyAlreadyUsed() throws Exception {
        String key = "topup-key-123";

        // simulate existing cache entry with a serialized response
        TransactionResponse txResp = new TransactionResponse();
        txResp.setTransactionId("tx-1");
        txResp.setAmount(BigDecimal.valueOf(100));

        String body = objectMapper.writeValueAsString(txResp);

        when(idempotencyService.handleIdempotencyKey(key, user.getUserId(), "/api/wallets/topup"))
                .thenReturn(IdempotencyResponse.builder()
                        .fromCache(true)
                        .statusCode(200)
                        .responseBody(body)
                        .build());

        // controller should not invoke service
        ResponseEntity<ApiResponse<TransactionResponse>> resp = controller.topUpWallet(request, key, user);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("tx-1", resp.getBody().getData().getTransactionId());
        verify(transactionService, never()).topUpFromBank(any(), any(), any(), any(), any(), any());
        verify(idempotencyService, never()).markSuccess(any(), any(), any());
    }

    @Test
    void topUpWallet_storesSuccess_whenNewKey() throws Exception {
        String key = "new-topup-key";

        when(idempotencyService.handleIdempotencyKey(key, user.getUserId(), "/api/wallets/topup"))
                .thenReturn(IdempotencyResponse.builder().fromCache(false).build());

        Transaction tx = Transaction.builder().transactionId("tx-new").build();
        when(transactionService.topUpFromBank(any(), any(), any(), any(), any(), any()))
                .thenReturn(tx);

        ResponseEntity<ApiResponse<TransactionResponse>> resp = controller.topUpWallet(request, key, user);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("tx-new", resp.getBody().getData().getTransactionId());
        verify(idempotencyService).markSuccess(eq(key), anyString(), eq(200));
    }

    @Test
    void topUpWallet_onException_marksFailure() {
        String key = "fail-topup-key";
        when(idempotencyService.handleIdempotencyKey(key, user.getUserId(), "/api/wallets/topup"))
                .thenReturn(IdempotencyResponse.builder().fromCache(false).build());

        when(transactionService.topUpFromBank(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> controller.topUpWallet(request, key, user));
        verify(idempotencyService).markFailure(eq(key), contains("boom"), eq(400));
    }

    @Test
    void withdrawFromWallet_returnsCached_whenKeyAlreadyUsed() throws Exception {
        String key = "withdraw-key-123";

        // simulate existing cache entry with a serialized response
        TransactionResponse txResp = new TransactionResponse();
        txResp.setTransactionId("tx-2");
        txResp.setAmount(BigDecimal.valueOf(100));

        String body = objectMapper.writeValueAsString(txResp);

        when(idempotencyService.handleIdempotencyKey(key, user.getUserId(), "/api/wallets/withdraw"))
                .thenReturn(IdempotencyResponse.builder()
                        .fromCache(true)
                        .statusCode(200)
                        .responseBody(body)
                        .build());

        // controller should not invoke service
        ResponseEntity<ApiResponse<TransactionResponse>> resp = controller.withdrawFromWallet(request, key, user);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("tx-2", resp.getBody().getData().getTransactionId());
        verify(transactionService, never()).withdrawFromWallet(any(), any(), any(), any(), any(), any());
        verify(idempotencyService, never()).markSuccess(any(), any(), any());
    }

    @Test
    void withdrawFromWallet_storesSuccess_whenNewKey() throws Exception {
        String key = "new-withdraw-key";

        when(idempotencyService.handleIdempotencyKey(key, user.getUserId(), "/api/wallets/withdraw"))
                .thenReturn(IdempotencyResponse.builder().fromCache(false).build());

        Transaction tx = Transaction.builder().transactionId("tx-new-withdraw").build();
        when(transactionService.withdrawFromWallet(any(), any(), any(), any(), any(), any()))
                .thenReturn(tx);

        ResponseEntity<ApiResponse<TransactionResponse>> resp = controller.withdrawFromWallet(request, key, user);

        assertEquals(200, resp.getStatusCodeValue());
        assertEquals("tx-new-withdraw", resp.getBody().getData().getTransactionId());
        verify(idempotencyService).markSuccess(eq(key), anyString(), eq(200));
    }

    @Test
    void withdrawFromWallet_onException_marksFailure() {
        String key = "fail-withdraw-key";
        when(idempotencyService.handleIdempotencyKey(key, user.getUserId(), "/api/wallets/withdraw"))
                .thenReturn(IdempotencyResponse.builder().fromCache(false).build());

        when(transactionService.withdrawFromWallet(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> controller.withdrawFromWallet(request, key, user));
        verify(idempotencyService).markFailure(eq(key), contains("boom"), eq(400));
    }
}