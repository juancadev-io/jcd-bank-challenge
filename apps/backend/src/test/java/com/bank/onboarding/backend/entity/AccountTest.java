package com.bank.onboarding.backend.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void constructor_setsFields() {
        Account account = new Account(1L, "ACC-123", "ACTIVE");

        assertEquals(1L, account.getCustomerId());
        assertEquals("ACC-123", account.getAccountNumber());
        assertEquals("ACTIVE", account.getStatus());
        assertEquals(BigDecimal.ZERO, account.getBalance());
    }

    @Test
    void defaultConstructor_fieldsAreNull() {
        Account account = new Account();

        assertNull(account.getId());
        assertNull(account.getCustomerId());
        assertNull(account.getAccountNumber());
        assertNull(account.getStatus());
        assertNull(account.getBalance());
        assertNull(account.getCreatedAt());
        assertNull(account.getUpdatedAt());
    }

    @Test
    void onCreate_setsTimestampsAndDefaults() {
        Account account = new Account();

        account.onCreate();

        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertEquals("ACTIVE", account.getStatus());
    }

    @Test
    void onCreate_doesNotOverrideExistingValues() {
        Account account = new Account(1L, "ACC-123", "INACTIVE");
        account.setBalance(new BigDecimal("100.00"));

        account.onCreate();

        assertEquals("INACTIVE", account.getStatus());
        assertEquals(new BigDecimal("100.00"), account.getBalance());
    }

    @Test
    void onUpdate_updatesTimestamp() {
        Account account = new Account(1L, "ACC-123", "ACTIVE");
        account.onCreate();

        account.onUpdate();

        assertNotNull(account.getUpdatedAt());
    }

    @Test
    void setters_updateFields() {
        Account account = new Account();
        account.setId(1L);
        account.setCustomerId(2L);
        account.setAccountNumber("ACC-999");
        account.setStatus("INACTIVE");
        account.setBalance(new BigDecimal("500.00"));

        assertEquals(1L, account.getId());
        assertEquals(2L, account.getCustomerId());
        assertEquals("ACC-999", account.getAccountNumber());
        assertEquals("INACTIVE", account.getStatus());
        assertEquals(new BigDecimal("500.00"), account.getBalance());
    }
}
