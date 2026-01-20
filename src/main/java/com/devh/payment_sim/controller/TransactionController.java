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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> sendMoney(@Valid @RequestBody SendMoneyRequest request){
        log.info("===TXN SEND MONEY INITIATED=== type: wallet to wallet, from: {}, to: {}", request.getFromUpiHandle(), request.getToUpiHandle());
        Transaction tx = transactionService.sendMoney(
                        request.getFromUpiHandle(), 
                        request.getToUpiHandle(), 
                        request.getTransferAmount(), 
                        request.getUpiPin(),
                        request.getRemarks());
        log.info("===TXN SEND MONEY COMPLETED=== txn id: {}", tx.getId());
        
        TransactionResponse response = EntityToResponseMapper.toTransactionResponse(tx);

        return ResponseEntity.ok(ApiResponse.success("Money from wallet sent successfully", response));
    }

    @GetMapping("/wallet/{walletUpiHandle}")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getAllTransactionsForWallet(@PathVariable String walletUpiHandle){
        log.info("===FETCH WALLET TXN(S) INITIATED=== Upi Handle: {}", walletUpiHandle);
        List<Transaction> transactions = transactionService.getTransactionsByWalletUpi(walletUpiHandle);
        log.info("===FETCH WALLET TXN(S) COMPLETED=== Count: {}, Upi Handle: {}", transactions.size(), walletUpiHandle);

        List<TransactionResponse> response = transactions.stream().map(EntityToResponseMapper::toTransactionResponse).toList();

        return ResponseEntity.ok(ApiResponse.success("All transactions retieved for given wallet handle", response));
    }
}
