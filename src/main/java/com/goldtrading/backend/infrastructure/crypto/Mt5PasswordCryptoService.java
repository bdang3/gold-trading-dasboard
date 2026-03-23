package com.goldtrading.backend.infrastructure.crypto;

import com.goldtrading.backend.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class Mt5PasswordCryptoService {
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LENGTH = 12;
    private final byte[] keyBytes;
    private final SecureRandom secureRandom = new SecureRandom();

    public Mt5PasswordCryptoService(@Value("${app.mt5.encryption-key}") String key) {
        this.keyBytes = Arrays.copyOf(key.getBytes(), 32);
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes());
            return "v1:" + Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new BusinessException("ENCRYPTION_ERROR", "Failed to encrypt MT5 password");
        }
    }
}
