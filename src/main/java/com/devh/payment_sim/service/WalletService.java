package com.devh.payment_sim.service;

import com.devh.payment_sim.model.Wallet;

public interface WalletService {
    Wallet createWallet(Long userId, String upiHandle);
    Wallet getWalletByUpi(String upiHandle);
}
