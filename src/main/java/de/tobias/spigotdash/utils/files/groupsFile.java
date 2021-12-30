package de.tobias.spigotdash.utils.files;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tobias.spigotdash.utils.errorCatcher;
import de.tobias.spigotdash.utils.pluginConsole;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class groupsFile {

    public String FILE_VER = "0.1";
    public ArrayList<Group> groups = new ArrayList<Group>();

    public transient File f;

    public groupsFile(File pF) {
        f = pF;
    }

    public boolean save() {
        try {
            FileUtils.write(f, gson.toJson(this), StandardCharsets.UTF_8);
            return true;
        } catch (Exception ex) {
            pluginConsole.sendMessage("&cFailed to save Groups File: ");
            errorCatcher.catchException(ex, false);
        }
        return false;
    }

    public Group getGroupByName(String name) {
        for(Group g : groups) {
            if(g.name.equals(name)) return g;
        }
        return null;
    }

    public Group getGroupByID(String id) {
        for(Group g : groups) {
            if(g.id.equals(id)) return g;
        }
        return null;
    }

    public boolean groupExists(String value) {
        return (getGroupByName(value) != null || getGroupByID(value) != null);
    }

    public boolean add(Group g) {
        if(!groupExists(g.id) && !groupExists(g.name)) {
            groups.add(g);
            save();
            return true;
        }
        return false;
    }

    public HashMap<String, HashMap<String, Object>> getGroupsSave() {
        HashMap<String, HashMap<String, Object>> safeGroups = new HashMap<>();

        for(Group g : groups) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("name", g.name);
            data.put("perms", g.permissions);
            data.put("html_color", g.html_color);
            data.put("LEVEL", g.LEVEL);
            safeGroups.put(g.id, data);
        }
        return safeGroups;
    }

    public ArrayList<Group> getGroupOrder() {
        Collections.sort(groups);
        return groups;
    }

    // *** STATIC ***
    public static Gson gson = (new GsonBuilder()).disableHtmlEscaping().serializeNulls().setPrettyPrinting().create();

    public static groupsFile getFromFile(File f) {
        try {
            groupsFile obj;

            if(f.exists()) {
                obj = gson.fromJson(new FileReader(f.getAbsolutePath()), groupsFile.class);
            } else {
                pluginConsole.sendMessage("&6Groups File was not found! Using empty Groups File Object...");
                obj = new groupsFile(f);
            }

            if(obj == null) throw new NullPointerException();
            obj.f = f;

            return obj;
        } catch (Exception ex) {
            pluginConsole.sendMessage("&cFailed to load Groups File: ");
            errorCatcher.catchException(ex, false);
            pluginConsole.sendMessage("&6Using empty Groups File Object...");
        }
        return new groupsFile(f);
    }
}
