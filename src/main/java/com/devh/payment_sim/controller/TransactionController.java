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
import com.devh.payment_sim.dto.SendMoneyRequest;
import com.devh.payment_sim.dto.response.EntityToResponseMapper;
import com.devh.payment_sim.dto.response.TransactionResponse;
import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> sendMoney(@Valid @RequestBody SendMoneyRequest request){
        Transaction transaction = transactionService.sendMoney(
                        request.getFromUpiHandle(), 
                        request.getToUpiHandle(), 
                        request.getTransferAmount(), 
                        request.getUpiPin(),
                        request.getRemarks());
        
        TransactionResponse response = EntityToResponseMapper.toTransactionResponse(transaction);

        return ResponseEntity.ok(ApiResponse.success("Money from wallet sent successfully", response));
    }

    @GetMapping("/wallet/{walletUpiHandle}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactionsForWallet(@PathVariable String walletUpiHandle){
        List<Transaction> transactions = transactionService.getTransactionsByWalletUpi(walletUpiHandle);

        List<TransactionResponse> response = transactions.stream().map(EntityToResponseMapper::toTransactionResponse).toList();

        return ResponseEntity.ok(ApiResponse.success("All transactions retieved for given wallet handle", response));
    }
}
