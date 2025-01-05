package com.library.librarymanagement.SecurityUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtils {

    private String bookId;
    private String userId;
    private String unwrappedKey;

    // Generate a Key Encryption Key (KEK)
    public static SecretKey generateKEKKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // 256-bit AES
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating KEK: " + e.getMessage());
        }
    }

    // Wrap the AES key using a KEK
    public static String wrapKey(SecretKey aesKey, SecretKey kek) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // Ensure consistent mode
        cipher.init(Cipher.WRAP_MODE, kek);
        byte[] wrappedKey = cipher.wrap(aesKey);
        return Base64.getEncoder().encodeToString(wrappedKey);
    }

    public static SecretKey unwrapKey(String wrappedKeyBase64, SecretKey kek) throws Exception {
        byte[] wrappedKey = Base64.getDecoder().decode(wrappedKeyBase64);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding"); // Ensure consistent mode
        cipher.init(Cipher.UNWRAP_MODE, kek);
        return (SecretKey) cipher.unwrap(wrappedKey, "AES", Cipher.SECRET_KEY);
    }

    public static SecretKey deriveKEK(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }



    // Generate a new AES key
    public static SecretKey generateAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(256); // or 128 depending on the key size
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating AES key: " + e.getMessage());
        }
    }

    // Encrypt data with AES
    public static String encryptAES(String data, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedData);
    }

    // Decrypt data with AES
    public static String decryptAES(String encryptedDataBase64, SecretKey aesKey) throws Exception {
        byte[] encryptedData = Base64.getDecoder().decode(encryptedDataBase64);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decryptedData = cipher.doFinal(encryptedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    // Convert SecretKey to String
    public static String keyToString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // Convert String back to SecretKey
    public SecretKey stringToKey(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, "AES");
    }

    // Example method for retrieving data and decrypting it
    public void retrieveData(String encryptedBookId, String encryptedUserId, String wrappedKeyBase64, SecretKey kek) {
        try {
            // Unwrap the AES key using the KEK
            SecretKey aesKey = unwrapKey(wrappedKeyBase64, kek);

            // Decrypt the bookId and userId
            this.bookId = decryptAES(encryptedBookId, aesKey);
            this.userId = decryptAES(encryptedUserId, aesKey);

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving and decrypting data: " + e.getMessage());
        }
    }

    // Getters and Setters for bookId and userId
    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUnwrappedKey() {
        return unwrappedKey;
    }

    public void setUnwrappedKey(String unwrappedKey) {
        this.unwrappedKey = unwrappedKey;
    }
}
