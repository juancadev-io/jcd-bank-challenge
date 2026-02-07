package com.bank.onboarding.backend.service;

import com.bank.onboarding.backend.dto.CustomerCreateDTO;
import com.bank.onboarding.backend.dto.CustomerResponseDTO;
import com.bank.onboarding.backend.entity.Customer;
import com.bank.onboarding.backend.exception.BusinessException;
import com.bank.onboarding.backend.exception.ResourceNotFoundException;
import com.bank.onboarding.backend.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerCreateDTO createDTO;
    private Customer customer;

    @BeforeEach
    void setUp() {
        createDTO = new CustomerCreateDTO("CC", "123456", "Juan Perez", "juan@test.com");

        customer = new Customer("CC", "123456", "Juan Perez", "juan@test.com");
        customer.setId(1L);
    }

    @Test
    void createCustomer_success() {
        when(customerRepository.existsByDocumentNumber("123456")).thenReturn(false);
        when(customerRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);

        CustomerResponseDTO result = customerService.createCustomer(createDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("CC", result.getDocumentType());
        assertEquals("123456", result.getDocumentNumber());
        assertEquals("Juan Perez", result.getFullName());
        assertEquals("juan@test.com", result.getEmail());
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void createCustomer_duplicateDocumentNumber_throwsBusinessException() {
        when(customerRepository.existsByDocumentNumber("123456")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> customerService.createCustomer(createDTO));

        assertTrue(exception.getMessage().contains("123456"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void createCustomer_duplicateEmail_throwsBusinessException() {
        when(customerRepository.existsByDocumentNumber("123456")).thenReturn(false);
        when(customerRepository.existsByEmail("juan@test.com")).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> customerService.createCustomer(createDTO));

        assertTrue(exception.getMessage().contains("juan@test.com"));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void getAllCustomers_returnsList() {
        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<CustomerResponseDTO> result = customerService.getAllCustomers();

        assertEquals(1, result.size());
        assertEquals("Juan Perez", result.get(0).getFullName());
    }

    @Test
    void getCustomerById_found() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerResponseDTO result = customerService.getCustomerById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getCustomerById_notFound_throwsResourceNotFoundException() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> customerService.getCustomerById(99L));
    }
}
