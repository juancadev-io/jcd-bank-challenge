package com.bank.onboarding.backend.repository;

import com.bank.onboarding.backend.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByDocumentNumber(String documentNumber);

    Optional<Customer> findByEmail(String email);

    boolean existsByDocumentNumber(String documentNumber);

    boolean existsByEmail(String email);
}
