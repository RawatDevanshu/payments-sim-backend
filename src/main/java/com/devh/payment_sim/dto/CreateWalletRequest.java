package com.devh.payment_sim.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateWalletRequest {

    @NotNull(message = "User Id is required")
    @Positive(message = "User Id must be a positive number")
    private Long userId;

    @NotNull(message = "Wallet UPI handle is required")
    @Pattern(regexp= "^[a-zA-Z0-9\\.]+@[a-zA-Z]+$", message= "Invalid UPI handle format, example: john123@bankname")
    private String upiHandle;
}
