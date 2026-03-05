package com.devh.payment_sim.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.dto.CreateWalletRequest;
import com.devh.payment_sim.dto.WalletBankTransferRequest;
import com.devh.payment_sim.dto.response.EntityToResponseMapper;
import com.devh.payment_sim.dto.response.IdempotencyResponse;
import com.devh.payment_sim.dto.response.TransactionResponse;
import com.devh.payment_sim.dto.response.WalletResponse;
import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.security.CustomUserDetails;
import com.devh.payment_sim.service.IdempotencyService;
import com.devh.payment_sim.service.TransactionService;
import com.devh.payment_sim.service.WalletService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final TransactionService transactionService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@Valid @RequestBody CreateWalletRequest request){
        log.info("===CREATE WALLET INITIATED=== Upi Handle: {}", request.getUpiHandle());
        Wallet wallet = walletService.createWallet(request.getUserId(), request.getUpiHandle());
        log.info("===CREATE WALLET COMPLETED=== WalletId: {}", wallet.getId());

        WalletResponse response = EntityToResponseMapper.toWalletResponse(wallet);
        return ResponseEntity.ok(ApiResponse.success("Wallet created successfully", response));
    }

    @GetMapping("/{upiHandle}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletByUpi(@PathVariable String upiHandle) {
        log.info("===FETCH WALLET INTIATED=== Upi Handle: {}", upiHandle);
        Wallet wallet = walletService.getWalletByUpi(upiHandle);
        log.info("===FETCH WALLET COMPLETED=== WalletId: {}", wallet.getId());

        WalletResponse response = EntityToResponseMapper.toWalletResponse(wallet);
        return ResponseEntity.ok(ApiResponse.success("Wallet fetched by upi handle successfully", response));
    }

    @PostMapping("/topup")
    public ResponseEntity<ApiResponse<TransactionResponse>> topUpWallet(
        @Valid @RequestBody WalletBankTransferRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        // Idempotency check
        if(idempotencyKey != null && !idempotencyKey.isEmpty()){
            IdempotencyResponse cachedResponse = idempotencyService
                .handleIdempotencyKey(idempotencyKey, user.getUserId(), "/api/wallets/topup");

            if(cachedResponse.isFromCache()){
                log.info("Returning cached response - Key: {}", idempotencyKey);
                try{
                    TransactionResponse response = objectMapper.readValue(
                        cachedResponse.getResponseBody(), 
                        TransactionResponse.class
                    );
                    return ResponseEntity
                        .status(cachedResponse.getStatusCode())
                        .body(ApiResponse.success("Wallet top up successfull", response));
                } catch (JsonProcessingException ex) {
                    log.error("Failed to parse cached response", ex);
                    throw new RuntimeException("Invalid cached data");
                }
            }
        }

        // Actual transaction logic
        log.info("===TXN TOPUP INITIATED=== type: bank to wallet, from: {}, to: {}", request.getBankAccountNumber(), request.getWalletUpiHandle());
        Transaction tx;
        try{
            tx = transactionService.topUpFromBank(
                                user.getUserId(), 
                                request.getWalletUpiHandle(), 
                                request.getBankAccountNumber(), 
                                request.getTransferAmount(), 
                                request.getRawPin(),
                                request.getRemarks()
                            );
        } catch (Exception ex) {
            // record failure for idempotency key if provided, then rethrow
            if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                idempotencyService.markFailure(idempotencyKey, ex.getMessage(), 400);
            }
            throw ex; // bubble to global handler
        }
        log.info("===TXN TOPUP COMPLETED=== txn id: {}", tx.getTransactionId());
        
        TransactionResponse response = EntityToResponseMapper.toTransactionResponse(tx);

        // Store success
        if(idempotencyKey != null && !idempotencyKey.isEmpty()){
            try{
                String responseJson = objectMapper.writeValueAsString(response);
                idempotencyService.markSuccess(idempotencyKey, responseJson, 200);
            } catch (Exception ex){
                log.warn("Failed to store idempotency result, continuing anyway", ex);
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Wallet top up successfull", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdrawFromWallet(
        @Valid @RequestBody WalletBankTransferRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @AuthenticationPrincipal CustomUserDetails user
    ) {

         // Idempotency check
        if(idempotencyKey != null && !idempotencyKey.isEmpty()){
            IdempotencyResponse cachedResponse = idempotencyService
                .handleIdempotencyKey(idempotencyKey, user.getUserId(), "/api/wallets/withdraw");

            if(cachedResponse.isFromCache()){
                log.info("Returning cached response - Key: {}", idempotencyKey);
                try{
                    TransactionResponse response = objectMapper.readValue(
                        cachedResponse.getResponseBody(), 
                        TransactionResponse.class
                    );
                    return ResponseEntity
                        .status(cachedResponse.getStatusCode())
                        .body(ApiResponse.success("Wallet top up successfull", response));
                } catch (JsonProcessingException ex) {
                    log.error("Failed to parse cached response", ex);
                    throw new RuntimeException("Invalid cached data");
                }
            }
        }

        // Actual transaction
        log.info("===TXN WITHDRAW INITIATED=== type: wallet to bank, from: {}, to: {}", request.getWalletUpiHandle(), request.getBankAccountNumber());
        Transaction tx;
        try{
            tx = transactionService.withdrawFromWallet(
                                user.getUserId(), 
                                request.getWalletUpiHandle(), 
                                request.getBankAccountNumber(), 
                                request.getTransferAmount(), 
                                request.getRawPin(), 
                                request.getRemarks()
                                );
                            } catch (Exception ex) {
                // record failure for idempotency key if provided, then rethrow
                if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                    idempotencyService.markFailure(idempotencyKey, ex.getMessage(), 400);
                }
                throw ex; // bubble to global handler
        }
        log.info("===TXN WITHDRAW COMPLETED=== txn id: {}", tx.getTransactionId());
            
        TransactionResponse response = EntityToResponseMapper.toTransactionResponse(tx);
        
        // Store success
        if(idempotencyKey != null && !idempotencyKey.isEmpty()) {
            try{
            String responseJson = objectMapper.writeValueAsString(response);
            idempotencyService.markSuccess(idempotencyKey, responseJson, 200);
            } catch (Exception ex){
                log.warn("Failed to store idempotency result, continuing anyway", ex);
            }
        }

        return ResponseEntity.ok(ApiResponse.success("Money withdrawn from wallet successfully", response));
    }
    
}
