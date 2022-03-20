package de.tobias.spigotdash.backend.utils;

import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSACryptor {

    public static final fieldLogger thisLogger = new fieldLogger("CRYPTO-RSA", globalLogger.constructed);

    public static KeyPair generateSet() {
        thisLogger.INFO("Generating new KeyPair...", 10);
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(1024);
            KeyPair pair = generator.generateKeyPair();
            thisLogger.INFO("New KeyPair generated", 0);
            return pair;
        } catch(Exception ex) {
            return null;
        }
    }

    public static PublicKey publicKeyFromBytes(byte[] bytes) {
        try {
            thisLogger.INFO("Parsing RSA Public Key from Bytes...", 10);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            return publicKey;
        } catch(Exception ex) {
            thisLogger.ERROREXEP("Failed to parse RSA Public Key from Bytes", ex, 0);
            return null;
        }
    }

    public static PrivateKey privateKeyFromBytes(byte[] bytes) {
        try {
            thisLogger.INFO("Parsing RSA Private Key from Bytes...", 10);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            return privateKey;
        } catch(Exception ex) {
            thisLogger.ERROREXEP("Failed to parse RSA Public Key from Bytes", ex, 0);
            return null;
        }
    }

    public static byte[] decryptBytes(byte[] bytes, PrivateKey key) {
        Cipher decryptCipher;
        try {
            decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, key);
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }

        byte[] encryptedMessageBytes = bytes;
        byte[] decryptedMessageBytes;
        try {
            decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return decryptedMessageBytes;
    }

    public static byte[] encryptBytes(byte[] bytes, PublicKey key) {
        Cipher decryptCipher;
        try {
            decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.ENCRYPT_MODE, key);
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }

        byte[] decryptedMessageBytes = bytes;
        byte[] encryptedMessageBytes;
        try {
            encryptedMessageBytes = decryptCipher.doFinal(decryptedMessageBytes);
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return encryptedMessageBytes;
    }
}
