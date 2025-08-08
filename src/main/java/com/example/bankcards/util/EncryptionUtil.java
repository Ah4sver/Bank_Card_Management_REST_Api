package com.example.bankcards.util;

import com.example.bankcards.exception.EncryptionException;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

@Component
public class EncryptionUtil {


    private static final String ALGORITHM = "AES";

    private final Key secretKey;


    public EncryptionUtil(@Value("${encryption.secret}") String secret) {

        if (secret.length() != 16) {
            throw new IllegalArgumentException("Секретный ключ должен иметь длину 16 символов");
        }
        this.secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }


    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new EncryptionException("Ошибка в шифровании данных", e);
        }
    }


    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedData);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new EncryptionException("Ошибка в дешифровке данных", e);
        }
    }
}
