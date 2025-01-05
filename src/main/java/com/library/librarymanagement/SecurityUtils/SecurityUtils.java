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
    public String book_id;
    public String user_id;
    // Generate AES Secret Key
    public String unwrappedkey;
    public SecretKey generateAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128); // 128-bit AES
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating AES key: " + e.getMessage());
        }
    }
    public SecretKey generateKEKKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256); // 128-bit AES
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating AES key: " + e.getMessage());
        }
    }
    /*


            String retrieve = "SELECT  `book_id`, `user_id`, `borrow_date`, `due_date`, `return_date`, `fine`, `en_key` FROM `borrow_records`";
            try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement preparedStatement = connection.prepareStatement(retrieve)) {

             ResultSet resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {

                book_id = decryptAES(resultSet.getString("book_id"),unwrappedkey),;
                user_id = decryptAES(resultSet.getString("user_id"),unwrappedkey);
                borrowdate = resultSet.getString("borrow_date");
                duedate = resultSet.getString("due_date");
                returndate = resultSet.getString("return_date");
                fine = resultSet.getString("fine");
                unwrappedkey = unwrapKey(resultSet.getString("en_key"),generateKEKkey());

                }
             }
             catch(Exception e){
            throw new RuntimeException("Error retrieving data: " + e.getMessage());
            }
     */


    // Encrypt data using AES
    public String encryptAES(String data, SecretKey secretKey) {
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
    public String decryptAES(String encryptedData, SecretKey key) throws Exception {
        try {
            // Initialize cipher in DECRYPT_MODE
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            // Decode Base64 encrypted data
            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);

            // Decrypt and convert to String
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data: " + e.getMessage());
        }
    }


    public String wrapKey(SecretKey dataKey, SecretKey kek) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.WRAP_MODE, kek);  // Using KEK to wrap the AES key

            byte[] wrappedKey = cipher.wrap(dataKey);  // Wrap the data key
            return Base64.getEncoder().encodeToString(wrappedKey);  // Encode it to Base64 for storage
        } catch (Exception e) {
            throw new RuntimeException("Error wrapping key: " + e.getMessage());
        }
    }


    public SecretKey unwrapKey(String wrappedKey, SecretKey kek) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.UNWRAP_MODE, kek);  // Use KEK to unwrap the AES key
            byte[] wrappedKeyBytes = Base64.getDecoder().decode(wrappedKey);  // Decode wrapped key
            return (SecretKey) cipher.unwrap(wrappedKeyBytes, "AES", Cipher.SECRET_KEY);  // Unwrap and return SecretKey
        } catch (Exception e) {
            throw new RuntimeException("Error unwrapping key: " + e.getMessage());
        }
    }




    // Convert SecretKey to String (for storing or sharing)
    public  String keyToString(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    // Convert String back to SecretKey
    public SecretKey stringToKey(String keyString) {
        byte[] decodedKey = Base64.getDecoder().decode(keyString);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}
