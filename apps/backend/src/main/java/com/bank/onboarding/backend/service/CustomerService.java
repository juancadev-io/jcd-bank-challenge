package com.bank.onboarding.backend.service;

import com.bank.onboarding.backend.dto.CustomerCreateDTO;
import com.bank.onboarding.backend.dto.CustomerResponseDTO;
import com.bank.onboarding.backend.entity.Customer;
import com.bank.onboarding.backend.exception.BusinessException;
import com.bank.onboarding.backend.exception.ResourceNotFoundException;
import com.bank.onboarding.backend.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponseDTO createCustomer(CustomerCreateDTO dto) {
        log.info("Creating customer with documentType={}", dto.getDocumentType());

        if (customerRepository.existsByDocumentNumber(dto.getDocumentNumber())) {
            log.warn("Duplicate document number detected");
            throw new BusinessException("A customer with document number '" + dto.getDocumentNumber() + "' already exists");
        }

        if (customerRepository.existsByEmail(dto.getEmail())) {
            log.warn("Duplicate email detected");
            throw new BusinessException("A customer with email '" + dto.getEmail() + "' already exists");
        }

        Customer customer = new Customer(
                dto.getDocumentType(),
                dto.getDocumentNumber(),
                dto.getFullName(),
                dto.getEmail()
        );

        Customer saved = customerRepository.save(customer);
        log.info("Customer created with id={}", saved.getId());
        return new CustomerResponseDTO(saved);
    }

    public List<CustomerResponseDTO> getAllCustomers() {
        log.info("Fetching all customers");
        List<CustomerResponseDTO> result = customerRepository.findAll()
                .stream()
                .map(CustomerResponseDTO::new)
                .toList();
        log.info("Found {} customers", result.size());
        return result;
    }

    public CustomerResponseDTO getCustomerById(Long id) {
        log.info("Fetching customer with id={}", id);
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Customer not found with id={}", id);
                    return new ResourceNotFoundException("Customer", "id", id);
                });
        return new CustomerResponseDTO(customer);
    }
}
