package com.bank.onboarding.backend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() throws Exception {
        encryptionService = new EncryptionService("test-encryption-key-2024");
    }

    @Test
    void encrypt_andDecrypt_returnOriginalValue() {
        String original = "juan@test.com";
        String encrypted = encryptionService.encrypt(original);
        String decrypted = encryptionService.decrypt(encrypted);

        assertNotEquals(original, encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void encrypt_null_returnsNull() {
        assertNull(encryptionService.encrypt(null));
    }

    @Test
    void decrypt_null_returnsNull() {
        assertNull(encryptionService.decrypt(null));
    }

    @Test
    void encrypt_deterministicForSameInput() {
        String first = encryptionService.encrypt("hello");
        String second = encryptionService.encrypt("hello");

        assertEquals(first, second);
    }

    @Test
    void encrypt_differentInputs_produceDifferentOutputs() {
        String encrypted1 = encryptionService.encrypt("value1");
        String encrypted2 = encryptionService.encrypt("value2");

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void decrypt_invalidBase64_throwsRuntimeException() {
        assertThrows(RuntimeException.class, () -> encryptionService.decrypt("not-valid-base64!!!"));
    }
}
