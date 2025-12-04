package com.devh.payment_sim.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devh.payment_sim.core.ApiResponse;
import com.devh.payment_sim.dto.OpenAccountRequest;
import com.devh.payment_sim.model.BankAccount;
import com.devh.payment_sim.service.BankAccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/bank-accounts")
@RequiredArgsConstructor
public class BankAccountController {
    private final BankAccountService bankAccountService;

    @PostMapping("/open")
    public ResponseEntity<ApiResponse<BankAccount>> openBankAccount(@Valid @RequestBody OpenAccountRequest request) {
        BankAccount account = bankAccountService.createAccount(request);
       return ResponseEntity.ok(ApiResponse.success("Bank Account created successfully", account));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<BankAccount>>> listBankAccounts(@PathVariable Long userId){
        List<BankAccount> bankAccounts = bankAccountService.getBankAccountsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success("List of bank accounts fetched successfully by userId", bankAccounts));
    }
}
