package de.tobias.spigotdash.backend.logging;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class globalLogger {

    public static globalLogger constructed = null;

    private boolean DEBUG = false;
    private File DEBUG_FILE = null;
    private Integer DEBUG_LEVEL = 0;
    private ArrayList<String> DEBUG_FIELDS = new ArrayList<>();

    private String PREFIX = "&c[SpigotDash] &r";
    private boolean translateColors = true;
    private boolean showColors = true;

    private final ConsoleCommandSender cs = Bukkit.getConsoleSender();

    public globalLogger(HashMap<String, Object> options) {
        sendRaw("Global Logger initializing");
        if(options != null) {
            for(Map.Entry<String, Object> entry : options.entrySet()) {
                if(entry.getKey().equalsIgnoreCase("DEBUG")) this.DEBUG = (boolean) entry.getValue();
                if(entry.getKey().equalsIgnoreCase("DEBUG_FILE")) this.DEBUG_FILE = new File((String) entry.getValue());
                if(entry.getKey().equalsIgnoreCase("DEBUG_LEVEL")) this.DEBUG_LEVEL = (Integer) entry.getValue();
                if(entry.getKey().equalsIgnoreCase("DEBUG_FIELDS")) this.DEBUG_FIELDS = (ArrayList<String>) entry.getValue();

                if(entry.getKey().equalsIgnoreCase("TRANSLATE_COLORS")) this.translateColors = (boolean) entry.getValue();
                if(entry.getKey().equalsIgnoreCase("SHOW_COLORS")) this.showColors = (boolean) entry.getValue();
            }
        } else {
         sendRaw("Logger starting without Options");
        }

        this.DEBUG_FIELDS.add("INIT");

        constructed = this;
        INFO("Logger started");
    }

    public void activateDevDebug() {
        this.DEBUG = true;
        this.DEBUG_FIELDS.add("*");
        this.DEBUG_LEVEL = 1000;
        this.DEBUG_FILE = null;
    }

    public void sendRawNoPrefix(String msg) {
        if(this.translateColors)  msg = ChatColor.translateAlternateColorCodes('&', msg);
        if(!this.showColors) msg = ChatColor.stripColor(msg);

        cs.sendMessage(msg);
    }

    public void sendRaw(String msg) {
        msg = PREFIX + msg;

        if(this.translateColors)  msg = ChatColor.translateAlternateColorCodes('&', msg);
        if(!this.showColors) msg = ChatColor.stripColor(msg);

        cs.sendMessage(msg);
    }

    public void INFO(String msg) {
        sendRaw("&8[&6INFO&8] &7" + msg);
    }

    public void WARNING(String msg) {
        sendRaw("&8[&eWARNING&8] &e" + msg);
    }

    public void ERROR(String msg) {
        sendRaw("&8[&4ERROR&8] &c" + msg);
    }

    public ArrayList<String> getDebugFields() {
        return DEBUG_FIELDS;
    }

    public Integer getDebugLevel() {
        return DEBUG_LEVEL;
    }

    public boolean shouldDebug() {
        return DEBUG;
    }

    public void setPrefix(String pref) {
        this.PREFIX = pref;
    }
}
