package de.tobias.spigotdash.models;

import de.tobias.spigotdash.backend.utils.PBKDF2WithHmacSHA1Cryptor;

import java.util.ArrayList;
import java.util.HashMap;

public class User {

    private String name = "ERROR";
    private String uuid = null;
    private Integer weight = 100;

    private String PASSWORD_HASH;
    private boolean requiresPasswordChange = true;

    private boolean isBanned = false;
    private boolean hasSeenWelcomeScreen = false;

    private ArrayList<UserPermissionSetEntry> permissions = new ArrayList<>();

    public User(String pName, String pUUID, Integer pWeight) {
        this.name = pName;
        this.uuid = pUUID;
        this.weight = pWeight;
        this.permissions.add(new UserPermissionSetEntry("COLLECTOR_TPS", "This Permission allows the User access to TPS Data"));
        this.permissions.add(new UserPermissionSetEntry("COLLECTOR_PLUGINS", "This Permission allows the User access to statistical Plugin Data"));
        this.permissions.add(new UserPermissionSetEntry("COLLECTOR_PLAYERS", "This Permission allows the User access to Players Data"));
    }

    public HashMap<String, Boolean> getPermissionSet() {
        HashMap<String, Boolean> tempPerms = new HashMap<>();
        for(UserPermissionSetEntry entry : permissions) {
            if(entry.isSet()) tempPerms.put(entry.getName(), entry.hasPermission());
        }
        return tempPerms;
    }

    public String getName() { return this.name; }

    public boolean validPassword(String toCheck) {
        return PBKDF2WithHmacSHA1Cryptor.validatePassword(toCheck, PASSWORD_HASH);
    }

    public void setPassword(String newPass) {
        this.PASSWORD_HASH = PBKDF2WithHmacSHA1Cryptor.generatePasswordHash(newPass);
    }
}
