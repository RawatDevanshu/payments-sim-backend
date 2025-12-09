package com.devh.payment_sim.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.devh.payment_sim.dto.OpenAccountRequest;
import com.devh.payment_sim.exception.ResourceNotFoundException;
import com.devh.payment_sim.model.BankAccount;
import com.devh.payment_sim.model.User;
import com.devh.payment_sim.repository.BankAccountRepository;
import com.devh.payment_sim.repository.UserRepository;
import com.devh.payment_sim.service.impl.BankAccountServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BankAccountServiceTest {
    
    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BankAccountServiceImpl bankAccountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateAccount_Success() {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        OpenAccountRequest request = new OpenAccountRequest();
        request.setUserId(1L);
        request.setAccountNumber("1234567890");
        request.setBankPin("1234");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BankAccount result = bankAccountService.createAccount(request);

        // Assert
        assertNotNull(result);
        assertEquals("1234567890", result.getAccountNumber());
        assertEquals(BigDecimal.valueOf(10000), result.getBalance());
        assertNotEquals("1234", result.getBankPinHash());
        assertEquals(user, result.getUser());
        verify(bankAccountRepository, times(1)).save(any(BankAccount.class));
    }

    @Test
    void testCreateAccount_UserNotFound() {
        OpenAccountRequest request = new OpenAccountRequest();
        request.setUserId(99L);
        request.setAccountNumber("1234567890");
        request.setBankPin("1234");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> bankAccountService.createAccount(request));
    }

    @Test
    void testGetBankAccountByUserId_Success(){
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        BankAccount acc1 = new BankAccount();
        acc1.setId(101L);
        
        BankAccount acc2 = new BankAccount();
        acc2.setId(102L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bankAccountRepository.findByUser(user)).thenReturn(List.of(acc1, acc2));

        List<BankAccount> result = bankAccountService.getBankAccountsByUserId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(101L, result.get(0).getId());
        assertEquals(102L, result.get(1).getId());
        verify(userRepository).findById(1L);
        verify(bankAccountRepository).findByUser(user);
        verifyNoMoreInteractions(userRepository, bankAccountRepository);
    }

    @Test
    void getBankAccountsByUserId_userMissing_throwsResourceNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, 
            () -> bankAccountService.getBankAccountsByUserId(99L)
        );

        verify(userRepository).findById(99L);
        verify(bankAccountRepository, never()).findByUser(any());
        verifyNoMoreInteractions(userRepository, bankAccountRepository);
    }

    @Test
    void getBankAccountByAccountNumber_Success() {
        BankAccount account = new BankAccount();
        account.setAccountNumber("1234567890");
        account.setId(1L);
        account.setBalance(BigDecimal.valueOf(10000));

        when(bankAccountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(account));

        BankAccount result = bankAccountService.getBankAccountByAccountNumber("1234567890");

        assertNotNull(result);
        assertEquals("1234567890", result.getAccountNumber());
        assertEquals(1L, result.getId());
        verify(bankAccountRepository).findByAccountNumber("1234567890");
        verifyNoMoreInteractions(bankAccountRepository);
    }

    @Test
    void getBankAccountByAccountNumber_NotFound() {
        when(bankAccountRepository.findByAccountNumber("MISSING"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bankAccountService.getBankAccountByAccountNumber("MISSING"));
        
        verify(bankAccountRepository).findByAccountNumber("MISSING");
        verifyNoMoreInteractions(bankAccountRepository);
    }
}
