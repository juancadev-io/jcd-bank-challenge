package com.bank.onboarding.backend.controller;

import com.bank.onboarding.backend.dto.AccountCreateDTO;
import com.bank.onboarding.backend.dto.AccountResponseDTO;
import com.bank.onboarding.backend.dto.AccountStatusDTO;
import com.bank.onboarding.backend.exception.BusinessException;
import com.bank.onboarding.backend.exception.GlobalExceptionHandler;
import com.bank.onboarding.backend.exception.ResourceNotFoundException;
import com.bank.onboarding.backend.service.AccountService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    private AccountResponseDTO buildResponse() {
        AccountResponseDTO dto = new AccountResponseDTO();
        dto.setId(1L);
        dto.setCustomerId(1L);
        dto.setAccountNumber("ACC-1234567890-1234");
        dto.setStatus("ACTIVE");
        dto.setBalance(BigDecimal.ZERO);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    @Test
    void createAccount_returns201() throws Exception {
        when(accountService.createAccount(any())).thenReturn(buildResponse());

        AccountCreateDTO request = new AccountCreateDTO(1L);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("ACC-1234567890-1234"));
    }

    @Test
    void createAccount_nullCustomerId_returns400() throws Exception {
        AccountCreateDTO request = new AccountCreateDTO();

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void createAccount_customerNotFound_returns404() throws Exception {
        when(accountService.createAccount(any()))
                .thenThrow(new ResourceNotFoundException("Customer", "id", 99L));

        AccountCreateDTO request = new AccountCreateDTO(99L);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createAccount_duplicate_returns400() throws Exception {
        when(accountService.createAccount(any()))
                .thenThrow(new BusinessException("Customer with id 1 already has an account"));

        AccountCreateDTO request = new AccountCreateDTO(1L);

        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Customer with id 1 already has an account"));
    }

    @Test
    void getAccounts_all_returns200() throws Exception {
        when(accountService.getAllAccounts()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountNumber").value("ACC-1234567890-1234"));
    }

    @Test
    void getAccounts_byCustomerId_returns200() throws Exception {
        when(accountService.getAccountsByCustomerId(1L)).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/accounts").param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1));
    }

    @Test
    void getAccounts_empty_returns200() throws Exception {
        when(accountService.getAllAccounts()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void updateAccountStatus_returns200() throws Exception {
        AccountResponseDTO response = buildResponse();
        response.setStatus("INACTIVE");
        when(accountService.updateAccountStatus(eq(1L), eq("INACTIVE"))).thenReturn(response);

        AccountStatusDTO request = new AccountStatusDTO("INACTIVE");

        mockMvc.perform(patch("/api/accounts/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void updateAccountStatus_notFound_returns404() throws Exception {
        when(accountService.updateAccountStatus(eq(99L), eq("INACTIVE")))
                .thenThrow(new ResourceNotFoundException("Account", "id", 99L));

        AccountStatusDTO request = new AccountStatusDTO("INACTIVE");

        mockMvc.perform(patch("/api/accounts/99/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateAccountStatus_invalidStatus_returns400() throws Exception {
        AccountStatusDTO request = new AccountStatusDTO("INVALID");

        mockMvc.perform(patch("/api/accounts/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
