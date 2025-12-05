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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    private final TransactionService transactionService;
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<WalletResponse>> createWallet(@Valid @RequestBody CreateWalletRequest request){
        Wallet wallet = walletService.createWallet(request.getUserId(), request.getUpiHandle());

        WalletResponse response = EntityToResponseMapper.toWalletResponse(wallet);
        return ResponseEntity.ok(ApiResponse.success("Wallet created successfully", response));
    }

    @GetMapping("/{upiHandle}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWalletByUpi(@PathVariable String upiHandle) {
        Wallet wallet = walletService.getWalletByUpi(upiHandle);

        WalletResponse response = EntityToResponseMapper.toWalletResponse(wallet);
        return ResponseEntity.ok(ApiResponse.success("Wallet fetched by upi handle successfully", response));
    }

    @PostMapping("/topup")
    public ResponseEntity<ApiResponse<TransactionResponse>> topUpWallet(
        @Valid @RequestBody WalletBankTransferRequest request,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        Transaction tx = transactionService.topUpFromBank(
                            user.getUserId(), 
                            request.getWalletUpiHandle(), 
                            request.getBankAccountNumber(), 
                            request.getTransferAmount(), 
                            request.getRawPin(),
                            request.getRemarks()
                        );
        
        TransactionResponse response = EntityToResponseMapper.toTransactionResponse(tx);

        return ResponseEntity.ok(ApiResponse.success("Wallet top up successfull", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdrawFromWallet(
        @RequestBody WalletBankTransferRequest request,
        @AuthenticationPrincipal CustomUserDetails user
    ) {
        Transaction tx = transactionService.withdrawFromWallet(
                            user.getUserId(), 
                            request.getWalletUpiHandle(), 
                            request.getBankAccountNumber(), 
                            request.getTransferAmount(), 
                            request.getRawPin(), 
                            request.getRemarks()
                            );
            
        TransactionResponse response = EntityToResponseMapper.toTransactionResponse(tx);
        
        return ResponseEntity.ok(ApiResponse.success("Money withdrawn from wallet successfully", response));
    }
    
}
