package com.bank.onboarding.backend.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void constructor_setsFields() {
        Customer customer = new Customer("CC", "123456", "Juan Perez", "juan@test.com");

        assertEquals("CC", customer.getDocumentType());
        assertEquals("123456", customer.getDocumentNumber());
        assertEquals("Juan Perez", customer.getFullName());
        assertEquals("juan@test.com", customer.getEmail());
    }

    @Test
    void defaultConstructor_fieldsAreNull() {
        Customer customer = new Customer();

        assertNull(customer.getId());
        assertNull(customer.getDocumentType());
        assertNull(customer.getDocumentNumber());
        assertNull(customer.getFullName());
        assertNull(customer.getEmail());
        assertNull(customer.getCreatedAt());
        assertNull(customer.getUpdatedAt());
    }

    @Test
    void onCreate_setsTimestamps() {
        Customer customer = new Customer("CC", "123456", "Juan Perez", "juan@test.com");

        customer.onCreate();

        assertNotNull(customer.getCreatedAt());
        assertNotNull(customer.getUpdatedAt());
    }

    @Test
    void onUpdate_updatesTimestamp() {
        Customer customer = new Customer("CC", "123456", "Juan Perez", "juan@test.com");
        customer.onCreate();

        var originalUpdatedAt = customer.getUpdatedAt();
        customer.onUpdate();

        assertNotNull(customer.getUpdatedAt());
    }

    @Test
    void setters_updateFields() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setDocumentType("CE");
        customer.setDocumentNumber("999");
        customer.setFullName("Test");
        customer.setEmail("test@test.com");

        assertEquals(1L, customer.getId());
        assertEquals("CE", customer.getDocumentType());
        assertEquals("999", customer.getDocumentNumber());
        assertEquals("Test", customer.getFullName());
        assertEquals("test@test.com", customer.getEmail());
    }
}
