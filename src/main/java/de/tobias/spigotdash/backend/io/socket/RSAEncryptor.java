package de.tobias.spigotdash.backend.io.socket;

import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import de.tobias.spigotdash.main;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

public class RSAEncryptor {

    public static fieldLogger thisLogger = new fieldLogger("CRYPTO-RSA", globalLogger.constructed);

    public static HashMap<String, KeyPair> OWN_CRYPTO_SETS = new HashMap<>();
    public static HashMap<String, KeyPair> OTHER_CRYPTO_SETS = new HashMap<>();

    public static String generateOwnSet() {
        thisLogger.INFO("Generating new KeyPair...", 10);
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();

            String setID = UUID.randomUUID().toString();
            OWN_CRYPTO_SETS.put(setID, pair);
            thisLogger.INFO("New KeyPair generated: " + setID, 0);
            return setID;
        } catch(Exception ex) {
            return null;
        }
    }

    public static boolean addOtherSetWithPublicKey(String setID, String base64PublicKey) {
        KeyPair newPair = new KeyPair(getPublicKeyFromBase64(base64PublicKey), null);
        OTHER_CRYPTO_SETS.put(setID, newPair);
        thisLogger.INFO("New KeyPair accepted: " + setID, 0);
        return true;
    }

    public static PublicKey getPublicKeyFromBase64(String base64PublicKey) {
        thisLogger.INFO("Parsing PublicKey from Base64 Encoded String...", 10);
        try {
            byte[] encoded = Base64.getDecoder().decode(base64PublicKey);
            X509EncodedKeySpec  keySpec = new X509EncodedKeySpec(encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);
            thisLogger.INFO("Parsed PublicKey successfully", 10);
            return publicKey;
        } catch (Exception ex) {
            thisLogger.ERROREXEP("Illegal Key provided:", ex, 0);
            return null;
        }
    }

    public static PublicKey getSetPublicKey(String setID) {
        if(OWN_CRYPTO_SETS.containsKey(setID)) return OWN_CRYPTO_SETS.get(setID).getPublic();
        if(OTHER_CRYPTO_SETS.containsKey(setID)) return OWN_CRYPTO_SETS.get(setID).getPublic();
        return null;
    }

    public static PrivateKey getSetPrivateKey(String setID) {
        if(OWN_CRYPTO_SETS.containsKey(setID)) return OWN_CRYPTO_SETS.get(setID).getPrivate();
        if(OTHER_CRYPTO_SETS.containsKey(setID)) return OTHER_CRYPTO_SETS.get(setID).getPrivate();
        return null;
    }

    public static String decodeString(String setID, String s) {
        if(!OWN_CRYPTO_SETS.containsKey(setID)) return null;
        Cipher decryptCipher;
        try {
            decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, getSetPrivateKey(setID));
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }

        byte[] encryptedMessageBytes = s.getBytes(StandardCharsets.UTF_8);
        byte[] decryptedMessageBytes;
        try {
            decryptedMessageBytes = decryptCipher.doFinal(encryptedMessageBytes);
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return new String(decryptedMessageBytes, StandardCharsets.UTF_8);
    }

    public static String encodeString(String setID, String s) {
        if(!OTHER_CRYPTO_SETS.containsKey(setID)) return null;
        Cipher decryptCipher;
        try {
            decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.ENCRYPT_MODE, getSetPublicKey(setID));
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }

        byte[] decryptedMessageBytes = s.getBytes(main.GLOBAL_CHARSET);
        byte[] encryptedMessageBytes;
        try {
            encryptedMessageBytes = decryptCipher.doFinal(decryptedMessageBytes);
        } catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }

        return  new String(encryptedMessageBytes, main.GLOBAL_CHARSET);
    }
}
