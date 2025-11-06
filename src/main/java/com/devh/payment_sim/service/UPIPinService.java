package com.devh.payment_sim.service;

public interface UPIPinService {
    void setPin(Long walletId, String rawPin);
    boolean validatePin(Long walletId, String rawPin);
}
