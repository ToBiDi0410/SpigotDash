package de.tobias.spigotdash.web;

import com.google.gson.JsonObject;
import de.tobias.spigotdash.utils.pluginConsole;

import java.lang.reflect.Field;

public class PermissionSet {

    public boolean LOGIN = false;

    //TAB
    public boolean TAB_OVERVIEW = false;

    public boolean TAB_GRAPHS = false;
    public boolean TAB_WORLDS = false;

    public boolean TAB_CONSOLE = false;
    public boolean TAB_CONTROLS = false;
    public boolean TAB_PLUGINS = false;

    public boolean TAB_PLAYERS = false;
    public boolean TAB_FILES = false;
    public boolean TAB_USERS = false;

    //CONSOLE
    public boolean CONSOLE_VIEW = false;
    public boolean CONSOLE_EXECUTE = false;

    //CONTROLS
    public boolean CONTROLS_WHITELIST_EDIT = false;
    public boolean CONTROLS_WHITELIST_TOGGLE = false;
    public boolean CONTROLS_OPS_EDIT = false;
    public boolean CONTROLS_RELOAD = false;
    public boolean CONTROLS_STOP = false;
    public boolean CONTROLS_WORLD_NETHER = false;
    public boolean CONTROLS_WORLD_END = false;

    //PLUGINS
    public boolean PLUGINS_TOGGLE = false;
    public boolean PLUGINS_INSTALL = false;

    //PLAYERS
    public boolean PLAYERS_KICK = false;
    public boolean PLAYERS_MESSAGE = false;
    public boolean PLAYERS_DETAILS = false;

    //FILES
    public boolean FILES_EDIT = false;
    public boolean FILES_UPLOAD = false;
    public boolean FILES_VIEW = false;

    //USERS
    public boolean USERS_VIEW = false;
    public boolean USERS_ADD = false;
    public boolean USERS_DELETE = false;
    public boolean USERS_EDIT = false;
    public boolean USERS_IS_ADMIN = false;

    public PermissionSet() {}

    public void setAllTo(boolean t) {
        this.TAB_OVERVIEW = t;
        this.TAB_GRAPHS = t;
        this.TAB_WORLDS = t;
        this.TAB_CONSOLE = t;
        this.TAB_CONTROLS = t;
        this.TAB_PLUGINS = t;
        this.TAB_PLAYERS = t;
        this.TAB_FILES = t;
        this.TAB_USERS = t;

        this.CONSOLE_VIEW = t;
        this.CONSOLE_EXECUTE = t;

        this.CONTROLS_WHITELIST_EDIT = t;
        this.CONTROLS_WHITELIST_TOGGLE = t;
        this.CONTROLS_OPS_EDIT = t;
        this.CONTROLS_RELOAD = t;
        this.CONTROLS_STOP = t;
        this.CONTROLS_WORLD_NETHER = t;
        this.CONTROLS_WORLD_END = t;

        this.PLUGINS_TOGGLE = t;
        this.PLUGINS_INSTALL = t;

        this.PLAYERS_KICK = t;
        this.PLAYERS_MESSAGE = t;
        this.PLAYERS_DETAILS = t;

        this.FILES_EDIT = t;
        this.FILES_UPLOAD = t;
        this.FILES_VIEW = t;

        this.USERS_ADD = t;
        this.USERS_DELETE = t;
        this.USERS_EDIT = t;
        this.USERS_VIEW = t;
        this.USERS_IS_ADMIN = t;
    }

    public static void loadIntoFromJsonObject(JsonObject permissions, PermissionSet set) {
        Field[] declaredFields = set.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            if(permissions != null && permissions.has(field.getName())) {
                try {
                    field.setBoolean(set, permissions.get(field.getName()).getAsBoolean());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else {
                pluginConsole.sendMessage(set + " Internal Violation: JsonObject is missing Permission '" + field.getName() + "'");
            }
        }
    }

    public static JsonObject getAsJsonObject(PermissionSet set) {
        JsonObject obj = new JsonObject();

        Field[] declaredFields = set.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                obj.addProperty(field.getName(), field.getBoolean(set));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return obj;
    }

    public static PermissionSet ADMIN() {
        PermissionSet set = new PermissionSet();
        set.setAllTo(true);
        return set;
    }
}
