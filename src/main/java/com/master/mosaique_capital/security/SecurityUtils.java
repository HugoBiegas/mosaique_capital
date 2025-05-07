package com.master.mosaique_capital.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.regex.Pattern;

@Component
public class SecurityUtils {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String AES_CIPHER = "AES/CBC/PKCS5Padding";

    // Expressions régulières pour validation
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private static final Pattern STRONG_PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$");

    /**
     * Hache une valeur avec SHA-256
     */
    public String hashValue(String value) throws NoSuchAlgorithmException {
        if (StringUtils.isBlank(value)) {
            return null;
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Chiffre une valeur sensible avec AES
     */
    public String encryptSensitiveValue(String value, String secretKey) {
        try {
            if (StringUtils.isBlank(value) || StringUtils.isBlank(secretKey)) {
                return null;
            }

            // Générer un vecteur d'initialisation (IV)
            byte[] iv = new byte[16];
            SECURE_RANDOM.nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            // Créer la clé secrète
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), "AES");

            // Chiffrer
            Cipher cipher = Cipher.getInstance(AES_CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            // Concaténer IV et données chiffrées et encoder en Base64
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Error encrypting value", e);
        }
    }

    /**
     * Déchiffre une valeur sensible avec AES
     */
    public String decryptSensitiveValue(String encryptedValue, String secretKey) {
        try {
            if (StringUtils.isBlank(encryptedValue) || StringUtils.isBlank(secretKey)) {
                return null;
            }

            // Décoder la valeur Base64
            byte[] combined = Base64.getDecoder().decode(encryptedValue);

            // Extraire IV et données chiffrées
            byte[] iv = new byte[16];
            byte[] encrypted = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, iv.length);
            System.arraycopy(combined, iv.length, encrypted, 0, encrypted.length);

            // Créer les specs
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), "AES");

            // Déchiffrer
            Cipher cipher = Cipher.getInstance(AES_CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Error decrypting value", e);
        }
    }

    /**
     * Valide le format d'un email
     */
    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valide la force d'un mot de passe
     */
    public boolean isStrongPassword(String password) {
        return password != null && STRONG_PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Sanitize un texte contre les injections
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        // Enlever les caractères dangereux
        return input.replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;")
                .replaceAll("'", "&#x27;")
                .replaceAll("\\(", "&#40;")
                .replaceAll("\\)", "&#41;")
                .replaceAll("/", "&#x2F;");
    }

    /**
     * Génère un sel aléatoire
     */
    public String generateSalt(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        byte[] salt = new byte[length];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Génère un token aléatoire
     */
    public String generateRandomToken(int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }

        byte[] token = new byte[length];
        SECURE_RANDOM.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
}