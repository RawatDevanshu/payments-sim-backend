package com.devh.payment_sim.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "upipins")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UPIPin {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false)
    private String pinHash;

    @Column(nullable = false)
    private boolean isLocked;

    @Column(nullable = false)
    private int failedAttempts;
}
