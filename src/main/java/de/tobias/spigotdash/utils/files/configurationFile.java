package de.tobias.spigotdash.utils.files;

import com.google.gson.Gson;
import de.tobias.spigotdash.main;
import de.tobias.spigotdash.utils.errorCatcher;
import de.tobias.spigotdash.utils.pluginConsole;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class configurationFile {

    public String FILE_VER = null;
    public Integer PORT = 9000;
    public Integer PLAYER_RECORD = 0;

    public Boolean UPDATE_CHECK = true;
    public Boolean UPDATE_DOWNLOAD = true;
    public Boolean UPDATE_RELOAD = true;
    public Integer UPDATE_CHECK_TIMEOUT = 10;

    public String LANGUAGE = "EN";
    public Boolean DEFAULT_DARKMODE = true;
    public Boolean RECREATE_ADMIN_USER_ON_DELETION = true;
    public String ADMIN_PASSWORD = "PleaseChangeThis2022";


    public Boolean NGROK_ENABLED = false;
    public String NGROK_AUTH = "";

    public Boolean NGROK_PUSH_UPDATES_WITH_GET_FETCH = false;
    public String NGROK_GET_FETCH_URL = "https://www.ddnss.de/upd.php?key=<YOUR_KEY>&host=<YOUR_HOST>&ip=%HOST%";

    public transient File f;

    public configurationFile(File pF) {
        f = pF;
    }

    // *** STATIC ***
    public static Gson gson = main.gson;


    public static configurationFile getFromFile(File f) {
        configurationFile obj;
        try {

            if(f.exists()) {
                obj = gson.fromJson(new FileReader(f.getAbsolutePath()), configurationFile.class);
                pluginConsole.sendMessage("&aConfiguration loaded from File");
            } else {
                pluginConsole.sendMessage("&6Configuration File was not found! Using empty Users File Object...");
                obj = new configurationFile(f);
                obj.upgrade();
            }

            if(obj == null) throw new NullPointerException();
            obj.f = f;

            return obj;
        } catch (Exception ex) {
            pluginConsole.sendMessage("&cFailed to load Configuration File: ");
            errorCatcher.catchException(ex, false);
            pluginConsole.sendMessage("&6Using empty Configuration File Object...");
        }

        obj = new configurationFile(f);
        obj.upgrade();
        return obj;
    }

    public boolean save() {
        try {
            FileUtils.write(f, gson.toJson(this), StandardCharsets.UTF_8);
            return true;
        } catch (Exception ex) {
            pluginConsole.sendMessage("&cFailed to save Configuration File: ");
            errorCatcher.catchException(ex, false);
        }
        return false;
    }

    public boolean upgrade() {
        if(this.FILE_VER == null) {
            pluginConsole.sendMessage("No Config File found! Checking migration possibilities...");
            if(!migrateFromOld()) {
                pluginConsole.sendMessage("Creating new Config File...");
                this.FILE_VER = "0.1";
            }
        }

        return true;
    }

    public boolean migrateFromOld() {
        File OLD_CONFIG = new File(main.pl.getDataFolder(), "config.yml");
        if(OLD_CONFIG.exists()) {
            pluginConsole.sendMessage("[CFG-MIG] Migrating old Config File...");
            YamlConfiguration OLD_CONFIG_YAML = YamlConfiguration.loadConfiguration(OLD_CONFIG);

            int OLD_CONFIG_VER = Integer.parseInt(OLD_CONFIG_YAML.getString("FILE_VERSION").replace(".", ""));
            pluginConsole.sendMessage("[CFG-MIG] Old File is Version: " + OLD_CONFIG_VER);

            this.FILE_VER = "0.1";

            if(OLD_CONFIG_VER >= 2) {
                this.ADMIN_PASSWORD = OLD_CONFIG_YAML.getString("WEB_PASSWORD");
                this.PORT = OLD_CONFIG_YAML.getInt("PORT");
                pluginConsole.sendMessage("[CFG-MIG] Loaded Data from Version 0.2 (2)");
            }

            if(OLD_CONFIG_VER >= 3) {
                this.UPDATE_CHECK_TIMEOUT = OLD_CONFIG_YAML.getInt("UPDATE_REFRESH_TIME");
                pluginConsole.sendMessage("[CFG-MIG] Loaded Data from Version 0.3 (3)");

            }

            if(OLD_CONFIG_VER >= 4) {
                this.PLAYER_RECORD = OLD_CONFIG_YAML.getInt("PLAYER_RECORD");
                this.DEFAULT_DARKMODE = OLD_CONFIG_YAML.getBoolean("darkMode");
                pluginConsole.sendMessage("[CFG-MIG] Loaded Data from Version 0.4 (4)");
            }

            if(OLD_CONFIG_VER >= 5) {
                this.UPDATE_DOWNLOAD = OLD_CONFIG_YAML.getBoolean("autoUpdate");
                this.UPDATE_RELOAD = OLD_CONFIG_YAML.getBoolean("autoReloadOnUpdate");
                pluginConsole.sendMessage("[CFG-MIG] Loaded Data from Version 0.5 (5)");
            }

            if(OLD_CONFIG_VER >= 6) {
                this.LANGUAGE = OLD_CONFIG_YAML.getString("translations");
                pluginConsole.sendMessage("[CFG-MIG] Loaded Data from Version 0.6 (6)");
            }

            if(OLD_CONFIG_VER >= 7) {
                this.NGROK_ENABLED = OLD_CONFIG_YAML.getBoolean("USE_NGROK");
                this.NGROK_AUTH = OLD_CONFIG_YAML.getString("NGROK_AUTH");
                pluginConsole.sendMessage("[CFG-MIG] Loaded Data from Version 0.7 (7)");
            }

            if(OLD_CONFIG_VER >= 8) {
                this.NGROK_PUSH_UPDATES_WITH_GET_FETCH = OLD_CONFIG_YAML.getBoolean("NGROK_GET_UPDATES");
                this.NGROK_GET_FETCH_URL = OLD_CONFIG_YAML.getString("NGROK_GET_URL");
                pluginConsole.sendMessage("[CFG-MIG] Loaded Data from Version 0.8 (8)");
            }

            this.save();
            pluginConsole.sendMessage("[CFG-MIG] &aMigration successfully! Cleaning up...");

            try {
                FileDeleteStrategy.FORCE.delete(OLD_CONFIG);
                pluginConsole.sendMessage("[CFG-MIG] Old file deleted!");
            } catch (IOException e) {
                pluginConsole.sendMessage("[CFG-MIG] Could not delete old file! Please do it manually!");
            }

            return true;
        }
        return false;
    }
}
