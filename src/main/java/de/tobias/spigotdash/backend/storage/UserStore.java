package de.tobias.spigotdash.backend.storage;

import de.tobias.spigotdash.models.User;

import java.util.ArrayList;
import java.util.UUID;

public class UserStore {

    private final ArrayList<User> users = new ArrayList<>();

    public UserStore() {
        User adminUser = new User("ADMIN", UUID.randomUUID().toString(), 100);
        adminUser.setPassword("WELCOME");
        users.add(adminUser);
    }

    public User getUserByName(String name) {
        for(User u : users) {
            if(u.getName().equalsIgnoreCase(name)) return u;
        }
        return null;
    }

    public ArrayList<String> getUsernames() {
        ArrayList<String> tempNames = new ArrayList<>();
        for(User u : users) {
            tempNames.add(u.getName());
        }
        return tempNames;
    }
}
