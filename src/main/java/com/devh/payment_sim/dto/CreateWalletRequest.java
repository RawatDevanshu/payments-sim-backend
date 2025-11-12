package com.devh.payment_sim.dto;

import lombok.Data;

@Data
public class CreateWalletRequest {
    private Long userId;
    private String upiHandle;
}
