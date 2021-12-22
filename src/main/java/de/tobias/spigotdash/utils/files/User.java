package de.tobias.spigotdash.utils.files;

import de.tobias.spigotdash.utils.passwordCrypter;
import de.tobias.spigotdash.web.PermissionSet;

import java.util.Base64;

public class User {

    public String name;
    public PermissionSet perms;

    public String password;
    public String salt;

    public User() {}

    public User(String name, String password) {
        this.name = name;
        this.perms = new PermissionSet();
        changePassword(password);
    }

    public void changePassword(String newPass) {
        byte[] iSalt = passwordCrypter.generateSalt();
        password = passwordCrypter.encryptPassword(newPass, iSalt);
        salt = Base64.getEncoder().encodeToString(iSalt);
    }

    public boolean validPassword(String pass) {
        byte[] iSalt = Base64.getDecoder().decode(salt);
        return passwordCrypter.isSame(pass, password, iSalt);
    }
}
