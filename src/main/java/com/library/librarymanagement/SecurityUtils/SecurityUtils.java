package com.library.librarymanagement.SecurityUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtils {

    // Generate AES Secret Key
    public static SecretKey generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // 128-bit AES
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating AES key: " + e.getMessage());
        }
    }
    public static SecretKey generateKEKKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // 128-bit AES
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating AES key: " + e.getMessage());
        }
    }

    // Encrypt data using AES
    public static String encryptAES(String data, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data: " + e.getMessage());
        }
    }

    // Decrypt data using AES
    public static String decryptAES(String encryptedData, SecretKey secretKey) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedData = Base64.getDecoder().decode(encryptedData);
            byte[] originalData = cipher.doFinal(decodedData);
            return new String(originalData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data: " + e.getMessage());
        }
    }
    public static String wrapKey(SecretKey dataKey, SecretKey kek) throws Exception {
        try{ Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.WRAP_MODE, kek);
            byte[] wrappedKey = cipher.wrap(dataKey);
            return Base64.getEncoder().encodeToString(wrappedKey);}
        catch (Exception e){
            throw new RuntimeException("Error wraping key: " + e.getMessage());
        }
    }

    // Method to unwrap (decrypt) a key
    public static SecretKey unwrapKey(String wrappedKey, SecretKey kek) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.UNWRAP_MODE, kek);
            byte[] wrappedKeyBytes = Base64.getDecoder().decode(wrappedKey);
            return (SecretKey) cipher.unwrap(wrappedKeyBytes, "AES", Cipher.SECRET_KEY);
        }catch (Exception e){
            throw new RuntimeException("Error unwraping key: " + e.getMessage());
        }
    }

    // Convert SecretKey to String (for storing or sharing)
    public static String keyToString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // Convert String back to SecretKey
    public static SecretKey stringToKey(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}
