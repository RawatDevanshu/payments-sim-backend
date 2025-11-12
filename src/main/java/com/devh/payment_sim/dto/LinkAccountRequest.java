package com.devh.payment_sim.dto;

import lombok.Data;

@Data
public class LinkAccountRequest {
    private Long userId;
    private String accountNumber;
    private String bankName;
    private String ifscCode;
}
