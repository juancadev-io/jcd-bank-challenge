package com.bank.onboarding.backend.service;

import com.bank.onboarding.backend.dto.AccountCreateDTO;
import com.bank.onboarding.backend.dto.AccountResponseDTO;
import com.bank.onboarding.backend.entity.Account;
import com.bank.onboarding.backend.exception.BusinessException;
import com.bank.onboarding.backend.exception.ResourceNotFoundException;
import com.bank.onboarding.backend.repository.AccountRepository;
import com.bank.onboarding.backend.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public AccountResponseDTO createAccount(AccountCreateDTO dto) {
        log.info("Creating account for customerId={}", dto.getCustomerId());

        if (!customerRepository.existsById(dto.getCustomerId())) {
            log.warn("Customer not found with id={}", dto.getCustomerId());
            throw new ResourceNotFoundException("Customer", "id", dto.getCustomerId());
        }

        if (accountRepository.existsByCustomerId(dto.getCustomerId())) {
            log.warn("Customer id={} already has an account", dto.getCustomerId());
            throw new BusinessException("Customer with id " + dto.getCustomerId() + " already has an account");
        }

        String accountNumber = generateAccountNumber();

        Account account = new Account(dto.getCustomerId(), accountNumber, "ACTIVE");
        Account saved = accountRepository.save(account);
        log.info("Account created with id={}, accountNumber={}", saved.getId(), saved.getAccountNumber());
        return new AccountResponseDTO(saved);
    }

    public List<AccountResponseDTO> getAllAccounts() {
        log.info("Fetching all accounts");
        return accountRepository.findAll()
                .stream()
                .map(AccountResponseDTO::new)
                .toList();
    }

    public List<AccountResponseDTO> getAccountsByCustomerId(Long customerId) {
        log.info("Fetching accounts for customerId={}", customerId);
        return accountRepository.findAllByCustomerId(customerId)
                .stream()
                .map(AccountResponseDTO::new)
                .toList();
    }

    public AccountResponseDTO updateAccountStatus(Long id, String status) {
        log.info("Updating account id={} to status={}", id, status);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Account not found with id={}", id);
                    return new ResourceNotFoundException("Account", "id", id);
                });
        account.setStatus(status);
        Account saved = accountRepository.save(account);
        log.info("Account id={} status updated to {}", id, status);
        return new AccountResponseDTO(saved);
    }

    private String generateAccountNumber() {
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(9000) + 1000;
        return "ACC-" + timestamp + "-" + random;
    }
}
