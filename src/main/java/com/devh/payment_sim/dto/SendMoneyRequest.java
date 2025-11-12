package com.devh.payment_sim.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SendMoneyRequest {
    private String fromUpiHandle;
    private String toUpiHandle;
    private BigDecimal transferAmount;
    private String upiPin;
    private String remarks;
}
