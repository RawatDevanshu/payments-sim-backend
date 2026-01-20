package com.devh.payment_sim.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.dto.CreateWalletRequest;
import com.devh.payment_sim.dto.WalletBankTransferRequest;
import com.devh.payment_sim.dto.response.EntityToResponseMapper;
import com.devh.payment_sim.dto.response.TransactionResponse;
import com.devh.payment_sim.dto.response.WalletResponse;
import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.security.CustomUserDetails;
import com.devh.payment_sim.service.TransactionService;
import com.devh.payment_sim.service.WalletService;

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
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("===TXN TOPUP INITIATED=== type: bank to wallet, from: {}, to: {}", request.getBankAccountNumber(), request.getWalletUpiHandle());
        Transaction tx = transactionService.topUpFromBank(
                            user.getUserId(), 
                            request.getWalletUpiHandle(), 
                            request.getBankAccountNumber(), 
                            request.getTransferAmount(), 
                            request.getRawPin(),
                            request.getRemarks()
                        );
        log.info("===TXN TOPUP COMPLETED=== txn id: {}", tx.getId());
        
        TransactionResponse response = EntityToResponseMapper.toTransactionResponse(tx);

        return ResponseEntity.ok(ApiResponse.success("Wallet top up successfull", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdrawFromWallet(
        @Valid @RequestBody WalletBankTransferRequest request,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        log.info("===TXN WITHDRAW INITIATED=== type: wallet to bank, from: {}, to: {}", request.getWalletUpiHandle(), request.getBankAccountNumber());
        Transaction tx = transactionService.withdrawFromWallet(
                            user.getUserId(), 
                            request.getWalletUpiHandle(), 
                            request.getBankAccountNumber(), 
                            request.getTransferAmount(), 
                            request.getRawPin(), 
                            request.getRemarks()
                            );
        log.info("===TXN WITHDRAW COMPLETED=== txn id: {}", tx.getId());
            
        TransactionResponse response = EntityToResponseMapper.toTransactionResponse(tx);
        
        return ResponseEntity.ok(ApiResponse.success("Money withdrawn from wallet successfully", response));
    }
    
}
