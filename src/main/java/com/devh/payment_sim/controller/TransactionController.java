package com.devh.payment_sim.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devh.payment_sim.dto.SendMoneyRequest;
import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Transaction> sendMoney(@RequestBody SendMoneyRequest request){
        Transaction transaction = transactionService.sendMoney(
                        request.getFromUpiHandle(), 
                        request.getToUpiHandle(), 
                        request.getTransferAmount(), 
                        request.getUpiPin(),
                        request.getRemarks());

        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/wallet/{walletUpiHandle}")
    public ResponseEntity<List<Transaction>> getAllTransactionsForWallet(@PathVariable String walletUpiHandle){
        List<Transaction> transactions = transactionService.getTransactionsByWalletUpi(walletUpiHandle);

        return ResponseEntity.ok(transactions);
    }
}
