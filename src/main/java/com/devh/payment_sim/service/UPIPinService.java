package com.devh.payment_sim.service;

public interface UPIPinService {
    void setPin(String walletUpiHandle, String rawPin);
    boolean validatePin(String walletUpiHandle, String rawPin);
}
