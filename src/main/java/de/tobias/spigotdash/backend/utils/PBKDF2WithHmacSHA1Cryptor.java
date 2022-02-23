package de.tobias.spigotdash.backend.utils;

import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PBKDF2WithHmacSHA1Cryptor {

    //Inspiration from: https://howtodoinjava.com/java/java-security/how-to-generate-secure-password-hash-md5-sha-pbkdf2-bcrypt-examples/

    public static final fieldLogger thisLogger = new fieldLogger("CRYPTO-PBKDF2", globalLogger.constructed);

    public static String generatePasswordHash(String password) {
        try {
            int iterations = 1000;
            char[] chars = password.toCharArray();
            byte[] salt = getSalt();

            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

            byte[] hash = skf.generateSecret(spec).getEncoded();
            return "DONOTMODIFY:" + iterations + ":" + HexUtils.toHex(salt) + ":" + HexUtils.toHex(hash);
        } catch(Exception ex) {
            thisLogger.ERROREXEP("Cannot generate a secure Password Hash: ", ex, 0);
            return "Something went wrong! Please try to fix this as soon as possible as this will generate further errors!";
        }
    }

    public static boolean validatePassword(String originalPassword, String storedPassword) {
        try {
            String[] parts = storedPassword.split(":");
            int iterations = Integer.parseInt(parts[1]);

            byte[] salt = HexUtils.fromHex(parts[2]);
            byte[] hash = HexUtils.fromHex(parts[3]);

            PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] testHash = skf.generateSecret(spec).getEncoded();

            int diff = hash.length ^ testHash.length;
            for(int i = 0; i < hash.length && i < testHash.length; i++) {
                diff |= hash[i] ^ testHash[i];
            }
            return diff == 0;
        } catch(Exception ex) {
            thisLogger.ERROREXEP("Cannot validate Password Hash: ", ex, 0);
            return false;
        }
    }

    public static byte[] getSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);
        return salt;
    }

}
