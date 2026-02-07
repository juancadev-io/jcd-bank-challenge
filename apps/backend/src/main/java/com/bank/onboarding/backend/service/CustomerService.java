package com.bank.onboarding.backend.service;

import com.bank.onboarding.backend.dto.CustomerCreateDTO;
import com.bank.onboarding.backend.dto.CustomerResponseDTO;
import com.bank.onboarding.backend.entity.Customer;
import com.bank.onboarding.backend.exception.BusinessException;
import com.bank.onboarding.backend.exception.ResourceNotFoundException;
import com.bank.onboarding.backend.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponseDTO createCustomer(CustomerCreateDTO dto) {
        if (customerRepository.existsByDocumentNumber(dto.getDocumentNumber())) {
            throw new BusinessException("A customer with document number '" + dto.getDocumentNumber() + "' already exists");
        }

        if (customerRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("A customer with email '" + dto.getEmail() + "' already exists");
        }

        Customer customer = new Customer(
                dto.getDocumentType(),
                dto.getDocumentNumber(),
                dto.getFullName(),
                dto.getEmail()
        );

        Customer saved = customerRepository.save(customer);
        return new CustomerResponseDTO(saved);
    }

    public List<CustomerResponseDTO> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(CustomerResponseDTO::new)
                .toList();
    }

    public CustomerResponseDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return new CustomerResponseDTO(customer);
    }
}
