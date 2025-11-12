package com.devh.payment_sim.dto;

import lombok.Data;

@Data
public class UPIPinRequest {
    private String walletUpiHandle;
    private String pin;
}
