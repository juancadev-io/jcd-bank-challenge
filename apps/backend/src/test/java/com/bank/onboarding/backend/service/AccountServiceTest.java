package com.bank.onboarding.backend.service;

import com.bank.onboarding.backend.dto.AccountCreateDTO;
import com.bank.onboarding.backend.dto.AccountResponseDTO;
import com.bank.onboarding.backend.dto.TransactionDTO;
import com.bank.onboarding.backend.entity.Account;
import com.bank.onboarding.backend.exception.BusinessException;
import com.bank.onboarding.backend.exception.ResourceNotFoundException;
import com.bank.onboarding.backend.repository.AccountRepository;
import com.bank.onboarding.backend.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    private AccountCreateDTO createDTO;
    private Account account;

    @BeforeEach
    void setUp() {
        createDTO = new AccountCreateDTO(1L);

        account = new Account(1L, "ACC-1234567890-1234", "ACTIVE");
        account.setId(1L);
        account.setBalance(BigDecimal.ZERO);
    }

    @Test
    void createAccount_success() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.existsByCustomerId(1L)).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponseDTO result = accountService.createAccount(createDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getCustomerId());
        assertEquals("ACTIVE", result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertTrue(result.getAccountNumber().startsWith("ACC-"));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_customerNotFound_throwsResourceNotFoundException() {
        when(customerRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.createAccount(createDTO));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void createAccount_duplicateAccount_throwsBusinessException() {
        when(customerRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.existsByCustomerId(1L)).thenReturn(true);

        assertThrows(BusinessException.class,
                () -> accountService.createAccount(createDTO));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void getAllAccounts_returnsList() {
        when(accountRepository.findAll()).thenReturn(List.of(account));

        List<AccountResponseDTO> result = accountService.getAllAccounts();

        assertEquals(1, result.size());
        assertEquals("ACC-1234567890-1234", result.get(0).getAccountNumber());
    }

    @Test
    void getAllAccounts_emptyList() {
        when(accountRepository.findAll()).thenReturn(Collections.emptyList());

        List<AccountResponseDTO> result = accountService.getAllAccounts();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAccountsByCustomerId_returnsList() {
        when(accountRepository.findAllByCustomerId(1L)).thenReturn(List.of(account));

        List<AccountResponseDTO> result = accountService.getAccountsByCustomerId(1L);

        assertEquals(1, result.size());
        assertEquals("ACC-1234567890-1234", result.get(0).getAccountNumber());
    }

    @Test
    void updateAccountStatus_success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountResponseDTO result = accountService.updateAccountStatus(1L, "INACTIVE");

        assertNotNull(result);
        verify(accountRepository).save(account);
    }

    @Test
    void updateAccountStatus_notFound_throwsResourceNotFoundException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.updateAccountStatus(99L, "INACTIVE"));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void transaction_deposit_success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        TransactionDTO dto = new TransactionDTO("DEPOSIT", new BigDecimal("100.00"));
        AccountResponseDTO result = accountService.transaction(1L, dto);

        assertNotNull(result);
        verify(accountRepository).save(account);
        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @Test
    void transaction_withdrawal_success() {
        account.setBalance(new BigDecimal("500.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        TransactionDTO dto = new TransactionDTO("WITHDRAWAL", new BigDecimal("200.00"));
        AccountResponseDTO result = accountService.transaction(1L, dto);

        assertNotNull(result);
        verify(accountRepository).save(account);
        assertEquals(new BigDecimal("300.00"), account.getBalance());
    }

    @Test
    void transaction_insufficientFunds_throwsBusinessException() {
        account.setBalance(new BigDecimal("50.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        TransactionDTO dto = new TransactionDTO("WITHDRAWAL", new BigDecimal("100.00"));

        assertThrows(BusinessException.class,
                () -> accountService.transaction(1L, dto));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void transaction_accountNotFound_throwsResourceNotFoundException() {
        when(accountRepository.findById(99L)).thenReturn(Optional.empty());

        TransactionDTO dto = new TransactionDTO("DEPOSIT", new BigDecimal("100.00"));

        assertThrows(ResourceNotFoundException.class,
                () -> accountService.transaction(99L, dto));

        verify(accountRepository, never()).save(any());
    }

    @Test
    void transaction_inactiveAccount_throwsBusinessException() {
        account.setStatus("INACTIVE");
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        TransactionDTO dto = new TransactionDTO("DEPOSIT", new BigDecimal("100.00"));

        assertThrows(BusinessException.class,
                () -> accountService.transaction(1L, dto));

        verify(accountRepository, never()).save(any());
    }
}
