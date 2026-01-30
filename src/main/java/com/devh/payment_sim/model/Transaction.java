package com.devh.payment_sim.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // internal DB id

    @UuidGenerator
    @Column(nullable = false, unique = true, updatable = false)
    private String transactionId;  // public stable ID

    // Wallet -> Wallet transfer
    @ManyToOne
    @JoinColumn(name="from_wallet_id")
    private Wallet fromWallet;

    @ManyToOne
    @JoinColumn(name="to_wallet_id")
    private Wallet toWallet;

    // Bank -> Wallet topup OR Wallet -> Bank withdraw
    @ManyToOne
    @JoinColumn(name="from_bank_account_id")
    private BankAccount fromBankAccount;

    @ManyToOne
    @JoinColumn(name="to_bank_account_id")
    private BankAccount toBankAccount;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    private String remarks;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }
}
