package com.devh.payment_sim.model;

public enum TransactionStatus {
    CREATED,
    VALIDATING,
    PROCESSING,
    DEBIT_PENDING,
    CREDIT_PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}
