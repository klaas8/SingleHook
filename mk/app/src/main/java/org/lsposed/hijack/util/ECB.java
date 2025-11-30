package org.lsposed.hijack.util;

import android.text.TextUtils;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class ECB {

    private static final int ITERATIONS = 128;
    private static final int KEY_LENGTH = 128;
    private static final int SALT_LENGTH = 8;

    public static String e(String key, String plaintext) {
        try {
            byte[] salt = generateSalt();
            SecretKey secretKey = generateSecretKey(key.toCharArray(), salt);
            byte[] encryptedData = encryptData(plaintext.getBytes(), secretKey);
            byte[] combinedData = new byte[salt.length + encryptedData.length];
            System.arraycopy(salt, 0, combinedData, 0, salt.length);
            System.arraycopy(encryptedData, 0, combinedData, salt.length, encryptedData.length);
            return bytesToHex(combinedData);
        } catch (Throwable e) {}
        return plaintext;
    }

    public static String d(String key, String ciphertext) {
        try {
            byte[] combinedData = hexToBytes(ciphertext);
            byte[] salt = new byte[SALT_LENGTH];
            byte[] encryptedData = new byte[combinedData.length - SALT_LENGTH];
            System.arraycopy(combinedData, 0, salt, 0, SALT_LENGTH);
            System.arraycopy(combinedData, SALT_LENGTH, encryptedData, 0, encryptedData.length);
            SecretKey secretKey = generateSecretKey(key.toCharArray(), salt);
            byte[] decryptedData = decryptData(encryptedData, secretKey);
            return new String(decryptedData);
        } catch (Throwable e) {}
        return ciphertext;
    }

    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    private static SecretKey generateSecretKey(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        SecretKey secretKey = factory.generateSecret(spec);
        return new SecretKeySpec(secretKey.getEncoded(), "AES");
    }

    private static byte[] encryptData(byte[] data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    private static byte[] decryptData(byte[] data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}