package com.devh.payment_sim.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="transactions")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id; // internal DB id

    @Column(nullable = false, unique = true, updatable = false)
    @ToString.Include
    private String transactionId;  // public stable ID

    // Wallet -> Wallet transfer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="from_wallet_id")
    private Wallet fromWallet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="to_wallet_id")
    private Wallet toWallet;

    // Bank -> Wallet topup OR Wallet -> Bank withdraw
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="from_bank_account_id")
    private BankAccount fromBankAccount;

    @ManyToOne(fetch = FetchType.LAZY)
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
