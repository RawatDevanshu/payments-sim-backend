package com.devh.payment_sim.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.devh.payment_sim.config.AbstractPostgresTest;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.repository.UserRepository;
import com.devh.payment_sim.repository.WalletRepository;

public class TransactionConcurrencyTest extends AbstractPostgresTest {
    
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UPIPinService upiPinService;

    @BeforeEach
    void setUp() {
        walletRepository.deleteAll();
        userRepository.deleteAll();

        // Create sender user
        User senderUser = User.builder()
                .email("sender@example.com")
                .name("Sender User")
                .phone("1234567890")
                .passwordHash("hashedpassword")
                .role(com.devh.payment_sim.model.Role.ROLE_USER)
                .build();
        senderUser = userRepository.save(senderUser);

        // Create receiver user
        User receiverUser = User.builder()
                .email("receiver@example.com")
                .name("Receiver User")
                .phone("0987654321")
                .passwordHash("hashedpassword")
                .role(com.devh.payment_sim.model.Role.ROLE_USER)
                .build();
        receiverUser = userRepository.save(receiverUser);

        // Create sender wallet with $100 balance
        Wallet sender = Wallet.builder()
                .upiHandle("sender@upi")
                .balance(java.math.BigDecimal.valueOf(100))
                .user(senderUser)
                .build();
        walletRepository.save(sender);

        // Create receiver wallet with $0 balance
        Wallet receiver = Wallet.builder()
                .upiHandle("receiver@upi")
                .balance(java.math.BigDecimal.valueOf(0))
                .user(receiverUser)
                .build();
        walletRepository.save(receiver);    
        upiPinService.setPin("sender@upi", "1234");
    }

    @Test
    void testConcurrentTransfers_PreventDoubleSpend() throws InterruptedException {
        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);

        // CountDownLatch(1) acts as starting gate
        CountDownLatch latch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i=0; i<numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    latch.await(); // Wait for the starting signal

                    transactionService.sendMoney("sender@upi", "receiver@upi", java.math.BigDecimal.valueOf(30), "1234", "concurrent transfer test");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
        }

        latch.countDown(); // Release all threads to start concurrently
        executorService.shutdown();

        // Wait for all threads to finish
        while (!executorService.isTerminated()) {
            Thread.sleep(100);
        }

        // Verify balances
        Wallet updatedSender = walletRepository.findByUpiHandle("sender@upi").orElseThrow();

        // Only 3 successful transfers of $30 each should have occurred, leaving the sender with $10
        // If locking didn't work, balance would be negative or corrupt
        assertEquals(3, successCount.get(), "Exactly 3 transactions should succeed");
        assertEquals(2, failureCount.get(), "Exactly 2 transactions should fail");
        assertEquals(java.math.BigDecimal.valueOf(10).setScale(2), updatedSender.getBalance(), "Sender's balance should be $10 after 3 successful transfers");
    }
}
