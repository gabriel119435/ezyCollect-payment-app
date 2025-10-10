package org.example.service;

import org.example.dto.internal.exceptions.BadConfigurationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {

    @Test
    void encryptAndDecryptSuccess() {
        CryptoService crypto = new CryptoService("1234567890123456"); // 16-char key
        String plaintext = "hello world" ;

        String encrypted = crypto.encrypt(plaintext);
        assertNotEquals(plaintext, encrypted);

        String decrypted = crypto.decrypt(encrypted);
        assertEquals(plaintext, decrypted);
    }

    @Test
    void invalidKeyLengthError() {
        assertThrows(BadConfigurationException.class, () -> new CryptoService("shortkey"));
        assertThrows(BadConfigurationException.class, () -> new CryptoService("thiskeyistoolong123"));
    }

    @Test
    void invalidCypherText() {
        CryptoService crypto = new CryptoService("1234567890123456");
        assertThrows(RuntimeException.class, () -> crypto.decrypt("invalidCipherText"));
    }
}