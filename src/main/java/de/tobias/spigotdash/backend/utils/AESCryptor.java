package de.tobias.spigotdash.backend.utils;

import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESCryptor {

    public static final fieldLogger thisLogger = new fieldLogger("CRYPTO-AES", globalLogger.constructed);

    public static SecretKey generateKey() {
        thisLogger.INFO("Generating new Key...", 10);
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(256);
            SecretKey key = generator.generateKey();
            thisLogger.INFO("New Key generated!", 10);
            return key;
        } catch(Exception ex) {
            return null;
        }
    }

    public static SecretKey keyFromBytes(byte[] bytes) {
        try {
            thisLogger.INFO("Parsing AES Key from Bytes...", 10);
            SecretKeySpec secretKeySpec = new SecretKeySpec(bytes, "AES");
            return secretKeySpec;
        } catch(Exception ex) {
            thisLogger.ERROREXEP("Failed to parse AES Key from Bytes", ex, 0);
            return null;
        }
    }

    public static byte[] encryptBytes(byte[] bytes, SecretKey key) {
        try {
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] byteCipherText = aesCipher.doFinal(bytes);
            return byteCipherText;
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static byte[] decryptBytes(byte[] bytes, SecretKey key) {
        try {
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedBytes = aesCipher.doFinal(bytes);
            return decryptedBytes;
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }


}
