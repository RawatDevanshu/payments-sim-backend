package com.devh.payment_sim.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.dto.CreateWalletRequest;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.service.WalletService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<Wallet>> createWallet(@Valid @RequestBody CreateWalletRequest request){
        Wallet wallet = walletService.createWallet(request.getUserId(), request.getUpiHandle());
        return ResponseEntity.ok(ApiResponse.success("Wallet created successfully", wallet));
    }

    @GetMapping("/{upiHandle}")
    public ResponseEntity<ApiResponse<Wallet>> getWalletByUpi(@PathVariable String upiHandle) {
        Wallet wallet = walletService.getWalletByUpi(upiHandle);
        return ResponseEntity.ok(ApiResponse.success("Wallet fetched by upi handle successfully", wallet));
    }
}
