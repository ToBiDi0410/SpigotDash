package de.tobias.spigotdash.integrations;

import ch.njol.skript.Skript;
import de.tobias.spigotdash.utils.pluginConsole;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class SkriptIntegration {

    Plugin pl;
    boolean enabled = false;
    File pluginFolder;
    File scriptsFolder;

    public SkriptIntegration() {
        if(Bukkit.getPluginManager().getPlugin("Skript") != null) {
            pluginConsole.sendMessage("§bSkript found! Hooking into it...");
            this.enabled = true;
            pl = Bukkit.getPluginManager().getPlugin("Skript");
            this.pluginFolder = pl.getDataFolder();
            this.scriptsFolder = new File(this.pluginFolder, Skript.getInstance().SCRIPTSFOLDER);
        }
    }

    public Object getIntegrationObject() {
        HashMap<String, Object> objects = new HashMap<>();

        objects.put("ENABLED", enabled);
        if(this.enabled) {
            objects.put("VERSION", this.pl.getDescription().getVersion());
            objects.put("SCRIPTS", getScripts());
        }

        return objects;
    }

    public ArrayList<String> getScripts() {
        ArrayList<String> files = new ArrayList<>();
        if(this.scriptsFolder != null && this.scriptsFolder.exists()) {
            for(File f : this.scriptsFolder.listFiles()) {
                files.add(f.getName());
            }
        }

        return files;
    }

}
