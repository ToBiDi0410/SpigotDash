package de.tobias.spigotdash.utils.files;

import de.tobias.spigotdash.main;
import de.tobias.spigotdash.utils.pluginConsole;
import de.tobias.spigotdash.web.PermissionSet;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class Group implements Comparable<Group> {

    public String name;
    public String id;
    public String html_color;

    public Integer LEVEL;

    public boolean IS_DEFAULT_GROUP = false;
    public boolean IS_ADMIN_GROUP = false;

    public PermissionSet permissions;

    public Group(String name, PermissionSet perms) {
        this.LEVEL = 0;
        this.name = name;
        this.id = UUID.randomUUID().toString();
        this.permissions = perms;
        this.html_color = "#2965bd";
    }

    public PermissionSet addGrantedPermissionsToSet(PermissionSet input) {
        PermissionSet output = input;
        for(Map.Entry<String, Boolean> permEntry : PermissionSet.getAsMap(permissions).entrySet()) {
            if(permEntry.getValue()) {
                PermissionSet.setPermissionByName(permEntry.getKey(), permEntry.getValue(), output);
                //pluginConsole.sendMessage("Inherited Permission from Group: " + permEntry.getKey() + " --> " + permEntry.getValue());
            }
        }

        return output;
    }

    public ArrayList<User> getMembers() {
        ArrayList<User> members = new ArrayList<>();

        for(User u : main.UsersFile.users) {
            if(u.roles.contains(id)) members.add(u);
        }

        return members;
    }

    @Override
    public int compareTo(Group o) {
        return o.LEVEL.compareTo(this.LEVEL);
    }
}
