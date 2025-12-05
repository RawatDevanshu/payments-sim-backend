package com.devh.payment_sim.dto.response;

import com.devh.payment_sim.model.BankAccount;
import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.model.Wallet;


public final class EntityToResponseMapper {
    private EntityToResponseMapper(){}

    public static UserResponse toUserResponse(User u){
        if(u == null) return null;

        return UserResponse.builder()
            .id(u.getId())
            .name(u.getName())
            .email(u.getEmail())
            .role(u.getRole() != null ? u.getRole() : null)
            .createdAt(u.getCreatedAt())
            .build();
    }

    public static WalletResponse toWalletResponse(Wallet w){
        if(w == null) return null;

        return WalletResponse.builder()
                .id(w.getId())
                .upiHandle(w.getUpiHandle())
                .balance(w.getBalance())
                .isActive(w.isActive())
                .userId(w.getUser() != null ? w.getUser().getId() : null)
                .build();
    }

    public static String maskAccount(String accountNumber) {
        if(accountNumber == null || accountNumber.length() <= 4) return accountNumber;
        
        String last4 = accountNumber.substring(accountNumber.length()-4);
        return "*".repeat(accountNumber.length()-4) + last4;
    }

    public static TransactionResponse toTransactionResponse(Transaction t) {
        if(t == null) return null;

        return TransactionResponse.builder()
                .id(t.getId())
                .fromWalletUpi(t.getFromWallet() != null ? t.getFromWallet().getUpiHandle() : null)
                .toWalletUpi(t.getToWallet() != null ? t.getToWallet().getUpiHandle() : null)
                .fromBankAccount(t.getFromBankAccount() != null ? maskAccount(t.getFromBankAccount().getAccountNumber()) : null)
                .toBankAccount(t.getToBankAccount() != null ? maskAccount(t.getToBankAccount().getAccountNumber()) : null)
                .amount(t.getAmount())
                .type(t.getType())
                .status(t.getStatus())
                .remarks(t.getRemarks())
                .timestamp(t.getTimestamp())
                .build();
    }

    public static BankAccountResponse toBankAccountResponse(BankAccount b) {
        if(b == null) return null;

        return BankAccountResponse.builder()
                .id(b.getId())
                .userId(b.getUser() != null ? b.getUser().getId() : null)
                .accountNumber(b.getAccountNumber())
                .balance(b.getBalance())
                .createdAt(b.getCreatedAt())
                .build();
    }
}
