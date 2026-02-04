package com.devh.payment_sim.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMoneyRequest {

    @NotNull(message = "Sender UPI handle is required")
    @Pattern(regexp= "^[a-zA-Z0-9\\.]+@[a-zA-Z]+$", message= "Invalid Sender UPI handle format, example: john123@bankname")
    private String fromUpiHandle;

    @NotNull(message = "Receiver UPI handle is required")
    @Pattern(regexp= "^[a-zA-Z0-9\\.]+@[a-zA-Z]+$", message= "Invalid Receiver UPI handle format, example: john123@bankname")
    private String toUpiHandle;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "1.00", message = "Transfer amount must be greater than 0")
    @DecimalMax(value = "100000.00", message = "Transfer amount cannot exceed ₹100,000")
    @Digits(integer=10, fraction=2, message="Invalid transfer amount format")
    private BigDecimal transferAmount;

    @NotNull(message = "UPI pin is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "UPI PIN must be 4 or 6 digits")
    private String upiPin;

    @Size(max = 100, message = "Remarks cannot exceed 100 characters")
    private String remarks;
}
