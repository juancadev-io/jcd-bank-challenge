package com.bank.onboarding.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoConverterTest {

    private CryptoConverter converter;

    @BeforeEach
    void setUp() throws Exception {
        EncryptionService encryptionService = new EncryptionService("test-encryption-key-2024");
        converter = new CryptoConverter();
        converter.setEncryptionService(encryptionService);
    }

    @Test
    void convertToDatabaseColumn_encryptsValue() {
        String result = converter.convertToDatabaseColumn("test-value");

        assertNotNull(result);
        assertNotEquals("test-value", result);
    }

    @Test
    void convertToEntityAttribute_decryptsValue() {
        String encrypted = converter.convertToDatabaseColumn("test-value");
        String result = converter.convertToEntityAttribute(encrypted);

        assertEquals("test-value", result);
    }

    @Test
    void convertToDatabaseColumn_null_returnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_null_returnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }
}
