package de.tobias.spigotdash.backend.dataCollectors;

import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import de.tobias.spigotdash.backend.storage.CacheStore;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class DataCollectionManager {

    public static final fieldLogger thisLogger = new fieldLogger("DATACOLLECT", globalLogger.constructed);
    public static final HashMap<String, Object> collectors = new HashMap<>();

    public static void initAllCollectors() {
        collectors.put("TPS", new TPSCollector(thisLogger).init());
        collectors.put("PLUGINS", new PluginsCollector(thisLogger).init());
        collectors.put("PLAYERS", new PlayersCollector(thisLogger).init());
        collectors.put("HARDWARE", new HardwareCollector(thisLogger).init());
    }

    public static void initCacheTask() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(GlobalVariableStore.pl, new Runnable() {
            @Override
            public void run() {
                CacheStore currentCache = GlobalVariableStore.getCacheStore();
                currentCache.collectData();
                thisLogger.INFO("Fetched new Data Points!", 20);
            }
        }, 20L, 20L * 5);
    }
}
