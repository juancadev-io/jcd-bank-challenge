package com.bank.onboarding.backend.controller;

import com.bank.onboarding.backend.dto.CustomerCreateDTO;
import com.bank.onboarding.backend.dto.CustomerResponseDTO;
import com.bank.onboarding.backend.exception.BusinessException;
import com.bank.onboarding.backend.exception.GlobalExceptionHandler;
import com.bank.onboarding.backend.exception.ResourceNotFoundException;
import com.bank.onboarding.backend.service.CustomerService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(GlobalExceptionHandler.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomerResponseDTO buildResponse() {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(1L);
        dto.setDocumentType("CC");
        dto.setDocumentNumber("123456");
        dto.setFullName("Juan Perez");
        dto.setEmail("juan@test.com");
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());
        return dto;
    }

    @Test
    void createCustomer_returns201() throws Exception {
        when(customerService.createCustomer(any())).thenReturn(buildResponse());

        CustomerCreateDTO request = new CustomerCreateDTO("CC", "123456", "Juan Perez", "juan@test.com");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documentNumber").value("123456"));
    }

    @Test
    void createCustomer_invalidBody_returns400() throws Exception {
        CustomerCreateDTO request = new CustomerCreateDTO("", "", "", "not-email");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void createCustomer_duplicateDocument_returns400() throws Exception {
        when(customerService.createCustomer(any()))
                .thenThrow(new BusinessException("A customer with document number '123456' already exists"));

        CustomerCreateDTO request = new CustomerCreateDTO("CC", "123456", "Juan Perez", "juan@test.com");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("A customer with document number '123456' already exists"));
    }

    @Test
    void getAllCustomers_returns200() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Juan Perez"));
    }

    @Test
    void getAllCustomers_empty_returns200() throws Exception {
        when(customerService.getAllCustomers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getCustomerById_returns200() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getCustomerById_notFound_returns404() throws Exception {
        when(customerService.getCustomerById(99L))
                .thenThrow(new ResourceNotFoundException("Customer", "id", 99L));

        mockMvc.perform(get("/api/customers/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void getCustomerById_unexpectedError_returns500() throws Exception {
        when(customerService.getCustomerById(1L))
                .thenThrow(new RuntimeException("Unexpected DB error"));

        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.details").value("Unexpected DB error"));
    }
}
