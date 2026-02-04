package com.devh.payment_sim.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.devh.payment_sim.model.TransactionStatus;
import com.devh.payment_sim.model.TransactionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String fromWalletUpi;
    private String toWalletUpi;
    private String fromBankAccount; // masked
    private String toBankAccount; // masked
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private String remarks;
    private LocalDateTime timestamp;
}
