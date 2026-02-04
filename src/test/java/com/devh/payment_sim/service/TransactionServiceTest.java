package com.devh.payment_sim.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCrypt;

import com.devh.payment_sim.exception.InsufficientFundsException;
import com.devh.payment_sim.exception.InvalidPinException;
import com.devh.payment_sim.exception.ResourceNotFoundException;
import com.devh.payment_sim.model.BankAccount;
import com.devh.payment_sim.model.Transaction;
import com.devh.payment_sim.model.TransactionType;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.repository.BankAccountRepository;
import com.devh.payment_sim.repository.TransactionRepository;
import com.devh.payment_sim.repository.WalletRepository;
import com.devh.payment_sim.service.impl.TransactionServiceImpl;
import com.devh.payment_sim.statemachine.TransactionStateMachine;

class TransactionServiceTest {

    @Mock
    private TransactionStateMachine stateMachine;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UPIPinService upiPinService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Wallet senderWallet;
    private Wallet receiverWallet;
    private BankAccount bankAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = User.builder().id(1L).email("u@example.com").build();

        senderWallet = Wallet.builder()
                .id(101L)
                .upiHandle("sender@upi")
                .balance(BigDecimal.valueOf(1000))
                .user(user)
                .build();

        receiverWallet = Wallet.builder()
                .id(102L)
                .upiHandle("receiver@upi")
                .balance(BigDecimal.valueOf(100))
                .user(user)
                .build();

        bankAccount = BankAccount.builder()
                .id(201L)
                .accountNumber("1234567890")
                .bankPinHash(BCrypt.hashpw("1234", BCrypt.gensalt()))
                .balance(BigDecimal.valueOf(2000))
                .user(user)
                .build();

        when(transactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(walletRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bankAccountRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testSendMoney_Success() {
       when(walletRepository.findByUpiHandle("sender@upi")).thenReturn(Optional.of(senderWallet));
       when(walletRepository.findByUpiHandle("receiver@upi")).thenReturn(Optional.of(receiverWallet));
       when(upiPinService.validatePin("sender@upi", "1234")).thenReturn(true);

       Transaction tx = transactionService.sendMoney("sender@upi", "receiver@upi", BigDecimal.valueOf(250), "1234", "payment");

       assertEquals(BigDecimal.valueOf(750).setScale(2), senderWallet.getBalance());
       assertNotNull(tx);
       assertEquals(BigDecimal.valueOf(250).setScale(2), tx.getAmount());
       assertEquals(TransactionType.WALLET_TRANSFER, tx.getType());

       verify(walletRepository, times(2)).save(any(Wallet.class));
       verify(transactionRepository, times(6)).save(any(Transaction.class));
    }

    @Test
    void testSendMoney_selfPayment_throwsIllegalArgEx() {
        assertThrows(IllegalArgumentException.class, 
            () -> transactionService.sendMoney("same@upi", "same@upi", BigDecimal.TEN, "1234", "test remark"));
    }

    @Test
    void testSendMoney_invalidPin_throwsInvalidPinException() {
       when(walletRepository.findByUpiHandle("sender@upi")).thenReturn(Optional.of(senderWallet));
       when(walletRepository.findByUpiHandle("receiver@upi")).thenReturn(Optional.of(receiverWallet));
       when(upiPinService.validatePin("sender@upi", "wrongpin")).thenReturn(false);

       assertThrows(InvalidPinException.class, 
                () -> transactionService.sendMoney("sender@upi", "receiver@upi", BigDecimal.TEN, "wrong", "test remarks")
        );
    }

    @Test
    void testSendMoney_insufficientFunds_throwsInsufficientFundsEx() {
        senderWallet.setBalance(BigDecimal.valueOf(50));
        when(walletRepository.findByUpiHandle("sender@upi")).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUpiHandle("receiver@upi")).thenReturn(Optional.of(receiverWallet));
        when(upiPinService.validatePin("sender@upi", "1234")).thenReturn(true);

        assertThrows(InsufficientFundsException.class, 
                () -> transactionService.sendMoney("sender@upi", "receiver@upi", BigDecimal.valueOf(250), "1234", "test remarks")
        );
    }

    @Test
    void testTopUpFromBank_Success() {
        when(bankAccountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(bankAccount));
        when(walletRepository.findByUpiHandle("receiver@upi")).thenReturn(Optional.of(receiverWallet));

        Transaction tx = transactionService.topUpFromBank(1L, "receiver@upi", "1234567890", BigDecimal.valueOf(500), "1234", "topup");

        assertEquals(BigDecimal.valueOf(1500).setScale(2), bankAccount.getBalance());
        assertEquals(BigDecimal.valueOf(600).setScale(2), receiverWallet.getBalance());

        assertEquals(TransactionType.BANK_TOPUP, tx.getType());
    }

    @Test
    void testTopUpFromBank_wrongUser_throwsIllegalArgumentEx() {
        when(bankAccountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(bankAccount));
        bankAccount.getUser().setId(2L);

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.topUpFromBank(1L, "receiver@upi", "1234567890", BigDecimal.TEN, "1234", "remarks"));
    }

    @Test
    void testTopUpFromBank_invalidPin_throwsInvalidPinException() {
        when(bankAccountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(bankAccount));
        when(walletRepository.findByUpiHandle("receiver@upi")).thenReturn(Optional.of(receiverWallet));

        assertThrows(InvalidPinException.class,
                () -> transactionService.topUpFromBank(1L, "receiver@upi", "1234567890", BigDecimal.TEN, "wrong", "remarks"));
    }

    @Test
    void testWithdrawFromWallet_success() {
        when(bankAccountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(bankAccount));
        when(walletRepository.findByUpiHandle("sender@upi")).thenReturn(Optional.of(senderWallet));
        when(upiPinService.validatePin("sender@upi", "1234")).thenReturn(true);

        Transaction tx = transactionService.withdrawFromWallet(1L, "sender@upi", "1234567890", BigDecimal.valueOf(500), "1234", "withdraw");

        assertEquals(BigDecimal.valueOf(500).setScale(2), senderWallet.getBalance());
        assertEquals(BigDecimal.valueOf(2500).setScale(2), bankAccount.getBalance());
        assertEquals(TransactionType.WALLET_WITHDRAW, tx.getType());
    }

    @Test
    void withdrawFromWallet_insufficientFunds_throwsInsufficientFundsException() {
        senderWallet.setBalance(BigDecimal.valueOf(100));
        when(bankAccountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(bankAccount));
        when(walletRepository.findByUpiHandle("sender@upi")).thenReturn(Optional.of(senderWallet));
        when(upiPinService.validatePin("sender@upi", "1234")).thenReturn(true);

        assertThrows(InsufficientFundsException.class,
                () -> transactionService.withdrawFromWallet(1L, "sender@upi", "1234567890", BigDecimal.valueOf(500), "1234", "withdraw"));
    }

    @Test
    void getTransactionsByWalletUpi_success() {
        when(walletRepository.findByUpiHandle("sender@upi")).thenReturn(Optional.of(senderWallet));
        when(transactionRepository.findByFromWalletOrToWallet(senderWallet, senderWallet))
                .thenReturn(List.of(Transaction.builder().id(1L).build()));

        List<Transaction> txs = transactionService.getTransactionsByWalletUpi("sender@upi");

        assertEquals(1, txs.size());
    }

    @Test
    void getTransactionsByWalletUpi_walletNotFound_throwsResourceNotFound() {
        when(walletRepository.findByUpiHandle("missing@upi")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.getTransactionsByWalletUpi("missing@upi"));
    }

}
