package de.tobias.spigotdash.utils.files;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.spigotdash.utils.passwordCrypter;
import de.tobias.spigotdash.utils.pluginConsole;
import de.tobias.spigotdash.web.PermissionSet;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class users {

    File file;
    jsonDatabase db = null;
    String defaultJSON = "{\"users\": []}";

    public users(File f) {
        this.file = f;
        this.db = null;
    }

    public void prepare() {
        if(file != null) {
            if(!file.exists()) {
                try {
                    pluginConsole.sendMessage("Creating new Users File...");
                    FileUtils.write(file, defaultJSON);
                } catch (IOException e) {
                    pluginConsole.sendMessage("&cFailed to load Users File: &6Write Failed");
                    return;
                }
            }

            this.db = new jsonDatabase(file);
            db.read(defaultJSON);
            pluginConsole.sendMessage("&aUsers File read!");
        } else {
            pluginConsole.sendMessage("&cFailed to load Users File: &6No File provided (this is not your fault!)");
            return;
        }
    }

    public String createUser(String name, String password, PermissionSet set) {
        if(!usersExists(name)) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", name);
            byte[] salt = passwordCrypter.generateSalt();
            jsonObject.addProperty("salt", Base64.getEncoder().encodeToString(salt));
            jsonObject.addProperty("password", passwordCrypter.encryptPassword(password, salt));
            jsonObject.add("permissions", PermissionSet.getAsJsonObject(set));
            db.jsonTree.get("users").getAsJsonArray().add(jsonObject);
            db.save();
            return "CREATED";
        } else {
            return "ERR_NAME_ALREADY_TAKEN";
        }
    }

    public boolean validPassword(String name, String password) {
        JsonObject obj = getJsonByName(name);
        if(obj != null) {
            String salt = obj.get("salt").getAsString();
            byte[] saltb = Base64.getDecoder().decode(salt);
            return passwordCrypter.isSame(password, obj.get("password").getAsString(), saltb);
        }
        return false;
    }

    public JsonObject getJsonByName(String name) {
        if(db != null && name != null) {
            for(JsonElement obj : db.jsonTree.get("users").getAsJsonArray()) {
                if(obj.getAsJsonObject().has("name") &&
                        obj.getAsJsonObject().get("name").getAsString().equalsIgnoreCase(name)) {
                    return obj.getAsJsonObject();
                }
            }
        }
        return null;
    }

    public boolean usersExists(String name) {
        return getJsonByName(name) != null;
    }

    public PermissionSet getPermissionsByName(String name) {
        JsonObject json = getJsonByName(name);
        if(json != null && json.has("permissions")) {
            JsonObject perms = json.get("permissions").getAsJsonObject();
            PermissionSet newSet = new PermissionSet();
            PermissionSet.loadIntoFromJsonObject(perms, newSet);
            return newSet;
        }

        return null;
    }
}
