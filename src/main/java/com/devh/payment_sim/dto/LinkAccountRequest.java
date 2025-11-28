package com.devh.payment_sim.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LinkAccountRequest {

    @NotNull(message = "User Id is required")
    @Positive(message = "User Id must be a positive number")
    private Long userId;

    @NotNull(message = "Account Number is required")
    @Pattern(regexp = "^\\d{9,18}$", message = "Account Number must be between 9 to 18 digits")
    private String accountNumber;

    @NotNull(message = "Bank Name is required")
    @Size(min = 2, max = 100, message = "Bank name must be between 2 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z ]+$", message = "Bank Name must contain only letters and spaces")
    private String bankName;

    @NotNull(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format, example: ABCD0001234")
    private String ifscCode;
}
