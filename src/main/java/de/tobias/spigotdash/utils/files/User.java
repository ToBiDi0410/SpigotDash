package de.tobias.spigotdash.utils.files;

import de.tobias.spigotdash.main;
import de.tobias.spigotdash.utils.passwordCrypter;
import de.tobias.spigotdash.web.PermissionSet;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;

public class User {

    public String name;
    public PermissionSet perms;

    public String password;
    public String salt;

    public String pictureURL = "global/icons/default_profile.png";
    public ArrayList<String> roles = new ArrayList<>();

    public User() {}

    public User(String name, String password) {
        this.name = name;
        this.perms = new PermissionSet();
        this.pictureURL = "global/icons/default_profile.png";
        this.roles.add(main.GroupsFile.getDefaultGroup().id);
        changePassword(password);
    }

    public void sortSelfGroups() {
        ArrayList<Group> groupOrder = main.GroupsFile.getGroupOrder();

        Collections.sort(this.roles, new Comparator<String>() {
            @Override
            public int compare(String g1, String g2) {
                Group gr1 = main.GroupsFile.getGroupByID(g1);
                Group gr2 = main.GroupsFile.getGroupByID(g2);
                Integer gr1i = groupOrder.indexOf(gr1);
                Integer gr2i = groupOrder.indexOf(gr2);
                return gr1i.compareTo(gr2i);
            }
        });
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
