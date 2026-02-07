package com.bank.onboarding.backend.service;

import com.bank.onboarding.backend.dto.AccountCreateDTO;
import com.bank.onboarding.backend.dto.AccountResponseDTO;
import com.bank.onboarding.backend.entity.Account;
import com.bank.onboarding.backend.exception.BusinessException;
import com.bank.onboarding.backend.exception.ResourceNotFoundException;
import com.bank.onboarding.backend.repository.AccountRepository;
import com.bank.onboarding.backend.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public AccountResponseDTO createAccount(AccountCreateDTO dto) {
        if (!customerRepository.existsById(dto.getCustomerId())) {
            throw new ResourceNotFoundException("Customer", "id", dto.getCustomerId());
        }

        if (accountRepository.existsByCustomerId(dto.getCustomerId())) {
            throw new BusinessException("Customer with id " + dto.getCustomerId() + " already has an account");
        }

        String accountNumber = generateAccountNumber();

        Account account = new Account(dto.getCustomerId(), accountNumber, "ACTIVE");
        Account saved = accountRepository.save(account);
        return new AccountResponseDTO(saved);
    }

    public List<AccountResponseDTO> getAllAccounts() {
        return accountRepository.findAll()
                .stream()
                .map(AccountResponseDTO::new)
                .toList();
    }

    public List<AccountResponseDTO> getAccountsByCustomerId(Long customerId) {
        return accountRepository.findAllByCustomerId(customerId)
                .stream()
                .map(AccountResponseDTO::new)
                .toList();
    }

    public AccountResponseDTO updateAccountStatus(Long id, String status) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
        account.setStatus(status);
        Account saved = accountRepository.save(account);
        return new AccountResponseDTO(saved);
    }

    private String generateAccountNumber() {
        long timestamp = System.currentTimeMillis();
        int random = new Random().nextInt(9000) + 1000;
        return "ACC-" + timestamp + "-" + random;
    }
}
