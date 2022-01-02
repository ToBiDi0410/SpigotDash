package de.tobias.spigotdash.utils.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.tobias.spigotdash.utils.errorCatcher;
import de.tobias.spigotdash.utils.pluginConsole;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class usersFile {

    public String FILE_VER = "0.1";
    public ArrayList<User> users = new ArrayList<User>();

    public transient File f;

    public usersFile(File pF) {
        f = pF;
    }

    public boolean save() {
        try {
            FileUtils.write(f, gson.toJson(this), StandardCharsets.UTF_8);
            return true;
        } catch (Exception ex) {
            pluginConsole.sendMessage("&cFailed to save Users File: ");
            errorCatcher.catchException(ex, false);
        }
        return false;
    }

    public User getUserByName(String name) {
        for(User u : users) {
            if(u.name.equalsIgnoreCase(name)) return u;
        }
        return null;
    }

    public boolean userExists(String name) {
        return (getUserByName(name) != null);
    }

    public boolean add(User u) {
        if(!userExists(u.name)) {
            users.add(u);
            save();
            return true;
        }
        return false;
    }

    public boolean deleteUser(User u) {
        if(u != null) {
            users.remove(u);
            save();
            return true;
        }
        return false;
    }

    public ArrayList<HashMap<String, Object>> getUsersSave() {
        ArrayList<HashMap<String, Object>> safeUsers = new ArrayList<>();
        for(User u : users) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("name", u.name);
            data.put("perms", u.perms);
            data.put("roles", u.roles);
            data.put("pictureURL", u.pictureURL);
            data.put("passwordStarred", "*".repeat(u.passwordLength));
            safeUsers.add(data);
        }
        return safeUsers;
    }

    // *** STATIC ***
    public static Gson gson = (new GsonBuilder()).disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();

    public static usersFile getFromFile(File f) {
        try {
            usersFile obj;

            if(f.exists()) {
                obj = gson.fromJson(new FileReader(f.getAbsolutePath()), usersFile.class);
            } else {
                pluginConsole.sendMessage("&6User File was not found! Using empty Users File Object...");
                obj = new usersFile(f);
            }

            if(obj == null) throw new NullPointerException();
            obj.f = f;

            return obj;
        } catch (Exception ex) {
            pluginConsole.sendMessage("&cFailed to load Users File: ");
            errorCatcher.catchException(ex, false);
            pluginConsole.sendMessage("&6Using empty Users File Object...");
        }
        return new usersFile(f);
    }

    public void process() {
        for(User u : users) {
            u.sortSelfGroups();
        }
    }
}
