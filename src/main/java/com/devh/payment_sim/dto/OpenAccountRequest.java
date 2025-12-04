package com.devh.payment_sim.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OpenAccountRequest {

    @NotNull(message = "User Id is required")
    @Positive(message = "User Id must be a positive number")
    private Long userId;

    @NotNull(message = "Account Number is required")
    @Pattern(regexp = "^\\d{9,18}$", message = "Account Number must be between 9 to 18 digits")
    private String accountNumber;

    @NotNull(message = "Bank pin is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN must of length 4 to 6")
    private String bankPin;
}
