package de.tobias.spigotdash.backend.dataCollectors;

import com.google.gson.JsonObject;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import org.bukkit.Bukkit;

public class PluginsCollector implements dataCollectionRequestHandler.dataCollectionHandler {

    private Integer ACTIVE_PLUGINS = 0;

    private final fieldLogger thisLogger;

    public PluginsCollector(fieldLogger fl) {
        thisLogger = fl.subFromParent("PLUGINS");
    }

    public PluginsCollector init() {
        //Start a Task that will execute every 100 Ticks (every 5 in-game Second) for Data collection
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(GlobalVariableStore.pl, this::task, 100L, 20L);
        thisLogger.INFO("Started Collection Service", 0);
        return this;
    }

    private void task() {
        this.ACTIVE_PLUGINS = Bukkit.getPluginManager().getPlugins().length;
    }

    private Integer getActivePlugins() {
        return this.ACTIVE_PLUGINS;
    }

    @Override
    public Object REQUEST_DATA(String dataID, JsonObject data) {
        if(dataID.equalsIgnoreCase("ACTIVE_PLUGINS")) return this.ACTIVE_PLUGINS;
        return null;
    }
}
