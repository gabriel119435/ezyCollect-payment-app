package org.example.service;

import org.example.dto.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 128-bit aes symmetrical encryption
 */
@Component
public class CryptoService {
    private final String key;
    private static final String ALGO = "AES";

    public CryptoService(@Value("${crypto.key}") String key) {
        if (key.length() != 16) throw new ValidationException("crypto.key should have length 16");
        this.key = key;
    }

    public String encrypt(String plaintext) {
        try {
            Cipher encryptor = Cipher.getInstance(ALGO);
            encryptor.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), ALGO));
            return Base64.getEncoder().encodeToString(encryptor.doFinal(plaintext.getBytes()));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            Cipher decryptor = Cipher.getInstance(ALGO);
            decryptor.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), ALGO));
            return new String(decryptor.doFinal(Base64.getDecoder().decode(cipherText)));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }
}