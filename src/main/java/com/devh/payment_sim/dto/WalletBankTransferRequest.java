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
public class WalletBankTransferRequest {
    
    @NotNull(message = "Account Number is required")
    @Pattern(regexp = "^\\d{9,18}$", message = "Account Number must be between 9 to 18 digits")
    private String bankAccountNumber;

    @NotNull(message = "Wallet UPI handle is required")
    @Pattern(regexp= "^[a-zA-Z0-9\\.]+@[a-zA-Z]+$", message= "Invalid UPI handle format, example: john123@bankname")
    private String walletUpiHandle;

    @NotNull(message = "Transfer amount is required")
    @DecimalMin(value = "1.00", message = "Transfer amount must be greater than 0")
    @DecimalMax(value = "100000.00", message = "Transfer amount cannot exceed ₹100,000")
    @Digits(integer=10, fraction=2, message="Invalid transfer amount format")
    private BigDecimal transferAmount;

    @NotNull(message = "Bank pin is required")
    @Pattern(regexp = "^\\d{4,6}$", message = "PIN must of length 4 to 6")
    private String rawPin;

    @Size(max = 100, message = "Remarks cannot exceed 100 characters")
    private String remarks;
}
