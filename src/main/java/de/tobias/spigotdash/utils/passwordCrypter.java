package de.tobias.spigotdash.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

public class passwordCrypter {

    public static String encryptPassword(String password, byte[] salt) {
        try {
            String algorithm = "PBKDF2WithHmacSHA1";
            int derivedKeyLength = 160;
            int iterations = 20000;

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);

            SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);

            return Base64.getEncoder().encodeToString(f.generateSecret(spec).getEncoded());
        } catch(Exception ex) {
            pluginConsole.sendMessage("&cFailed to encrypt Password! &bReturning Garbage for protection");
            errorCatcher.catchException(ex, false);
            return "WHYDIDTHISWENTWRONGSOFATALLY?PLEASETELLME!";
        }
    }

    public static boolean isSame(String password, String encrypted, byte[] salt) {
        String encryptedInput = encryptPassword(password, salt);
        return encryptedInput.equals(encrypted);
    }

    public static byte[] generateSalt() {
        try {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            byte[] salt = new byte[8];
            random.nextBytes(salt);

            return salt;
        } catch(Exception ex) {
            pluginConsole.sendMessage("&cFailed to generate Salt for password store!");
            errorCatcher.catchException(ex, false);
        }

        return (new byte[8]);
    }
}
