package com.devh.payment_sim.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UPIPinRequest {

    @NotNull(message = "Wallet UPI handle is required")
    @Pattern(regexp= "^[a-zA-Z0-9\\.]+@[a-zA-Z]+$", message= "Invalid UPI handle format, example: john123@bankname")
    private String walletUpiHandle;

    @NotNull(message = "Wallet pin is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN must of length 4 to 6")
    private String pin;
}
