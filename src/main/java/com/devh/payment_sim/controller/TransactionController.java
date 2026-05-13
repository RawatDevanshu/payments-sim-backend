package com.devh.payment_sim.controller;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.core.PageResponse;
import com.devh.payment_sim.dto.SendMoneyRequest;
import com.devh.payment_sim.dto.response.EntityToResponseMapper;
import com.devh.payment_sim.dto.response.IdempotencyResponse;
import com.devh.payment_sim.dto.response.TransactionResponse;
import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.security.CustomUserDetails;
import com.devh.payment_sim.service.IdempotencyService;
import com.devh.payment_sim.service.TransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> sendMoney(
        @Valid @RequestBody SendMoneyRequest request,
        @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
        @AuthenticationPrincipal CustomUserDetails user
    ){
        Long userId = user.getUserId();

        // Idempotency check
        if(idempotencyKey != null && !idempotencyKey.isEmpty()) {
            IdempotencyResponse cachedResponse = idempotencyService
                .handleIdempotencyKey(idempotencyKey, userId, "/api/transactions/transfer");

            if(cachedResponse.isFromCache()){
                log.info("Returning cached response - Key: {}", idempotencyKey);
                try{
                    TransactionResponse response = objectMapper.readValue(
                        cachedResponse.getResponseBody(), 
                        TransactionResponse.class
                    );
                    return ResponseEntity
                        .status(cachedResponse.getStatusCode())
                        .body(ApiResponse.success("Money sent successfully", response));

                } catch (JsonProcessingException ex) {
                    log.error("Failed to parse cached response", ex);
                    throw new RuntimeException("Invalid cached data");
                }
            }
                
        }

        // Actual transaction logic with simple failure recording
        log.info("===TXN SEND MONEY INITIATED=== type: wallet to wallet, from: {}, to: {}", request.getFromUpiHandle(), request.getToUpiHandle());
        Transaction tx;
        try {
            tx = transactionService.sendMoney(
                        request.getFromUpiHandle(), 
                        request.getToUpiHandle(), 
                        request.getTransferAmount(), 
                        request.getUpiPin(),
                        request.getRemarks());
        } catch (Exception ex) {
            // record failure for idempotency key if provided, then rethrow
            if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                idempotencyService.markFailure(idempotencyKey, ex.getMessage(), 400);
            }
            throw ex; // bubble to global handler
        }
        log.info("===TXN SEND MONEY COMPLETED=== txn id: {}", tx.getTransactionId());
        
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

        return ResponseEntity.ok(ApiResponse.success("Money from wallet sent successfully", response));
    }

    @GetMapping("/wallet/{walletUpiHandle}")
    public ResponseEntity<ApiResponse<PageResponse<TransactionResponse>>> getAllTransactionsForWallet(@PathVariable String walletUpiHandle, Pageable pageable){
        
        Sort defaultSort = Sort.by(
                    Sort.Order.desc("timestamp"),
                    Sort.Order.desc("id")
            );

        Sort sortToUse = pageable.getSort().isUnsorted() ? defaultSort : pageable.getSort();

        
        Pageable sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    sortToUse
            );


        log.info("===FETCH WALLET TXN(S) INITIATED=== Upi Handle: {}", walletUpiHandle);
        Page<Transaction> page = transactionService.getTransactionsByWalletUpi(walletUpiHandle, sortedPageable);
        
        PageResponse<TransactionResponse> body = PageResponse.from(
                    page.map(EntityToResponseMapper::toTransactionResponse)
            );

        log.info("===FETCH WALLET TXN(S) COMPLETED=== Count: {}, Upi Handle: {}", body.getItems().size(), walletUpiHandle);

        return ResponseEntity.ok(ApiResponse.success("All transactions retieved for given wallet handle", body));
    }
}
