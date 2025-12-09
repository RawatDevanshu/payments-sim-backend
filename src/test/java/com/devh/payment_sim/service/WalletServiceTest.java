package com.devh.payment_sim.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.devh.payment_sim.exception.ConflictException;
import com.devh.payment_sim.exception.ResourceNotFoundException;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.model.Wallet;
import com.devh.payment_sim.repository.UserRepository;
import com.devh.payment_sim.repository.WalletRepository;
import com.devh.payment_sim.service.impl.WalletServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class WalletServiceTest {
    
    @Mock
    private WalletRepository walletRepository;

    @Mock 
    private UserRepository userRepository;

    @InjectMocks
    private WalletServiceImpl walletService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateWallet_Success() {

        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.existsByUpiHandle("user@upi")).thenReturn(false);

        Wallet savedWallet = Wallet.builder()
                .id(1L)
                .user(user)
                .upiHandle("user@upi")
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .build();

        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        // Act
        Wallet result = walletService.createWallet(1L, "user@upi");

        // Assert
        assertNotNull(result);
        assertEquals("user@upi", result.getUpiHandle());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void testCreateWallet_UserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> walletService.createWallet(99L, "user@upi"));
    }

    @Test
    void testCreateWallet_UpiHandleConflict() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(walletRepository.existsByUpiHandle("user@upi")).thenReturn(true);

        assertThrows(ConflictException.class, () -> walletService.createWallet(1L, "user@upi"));
    }

    @Test
    void testGetWalletByUpi_Success(){
        Wallet wallet = Wallet.builder()
                .id(1L)
                .upiHandle("user@upi")
                .balance(BigDecimal.ZERO)
                .isActive(true)
                .build();
        
        when(walletRepository.findByUpiHandle("user@upi")).thenReturn(Optional.of(wallet));

        Wallet result = walletService.getWalletByUpi("user@upi");

        assertNotNull(result);
        assertEquals("user@upi", result.getUpiHandle());
    }

    @Test
    void testGetWalletByUpi_NotFound() {
        when(walletRepository.findByUpiHandle("missing@upi")).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> walletService.getWalletByUpi("missing@upi"));
    }

}
