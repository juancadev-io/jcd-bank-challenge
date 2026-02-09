package com.bank.onboarding.backend.controller;

import com.bank.onboarding.backend.dto.AccountCreateDTO;
import com.bank.onboarding.backend.dto.AccountResponseDTO;
import com.bank.onboarding.backend.dto.AccountStatusDTO;
import com.bank.onboarding.backend.dto.TransactionDTO;
import com.bank.onboarding.backend.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponseDTO> createAccount(@Valid @RequestBody AccountCreateDTO dto) {
        AccountResponseDTO created = accountService.createAccount(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDTO>> getAccounts(
            @RequestParam(required = false) Long customerId) {
        if (customerId != null) {
            return ResponseEntity.ok(accountService.getAccountsByCustomerId(customerId));
        }
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AccountResponseDTO> updateAccountStatus(
            @PathVariable Long id, @Valid @RequestBody AccountStatusDTO dto) {
        return ResponseEntity.ok(accountService.updateAccountStatus(id, dto.getStatus()));
    }

    @PostMapping("/{id}/transaction")
    public ResponseEntity<AccountResponseDTO> transaction(
            @PathVariable Long id, @Valid @RequestBody TransactionDTO dto) {
        return ResponseEntity.ok(accountService.transaction(id, dto));
    }
}
