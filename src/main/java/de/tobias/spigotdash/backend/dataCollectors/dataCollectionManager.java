package de.tobias.spigotdash.backend.dataCollectors;

import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;

import java.util.HashMap;

public class dataCollectionManager {

    public static fieldLogger thisLogger = new fieldLogger("DATACOLLECT", globalLogger.constructed);
    public static HashMap<String, Object> collectors = new HashMap<>();

    public static void initAllCollectors() {
        collectors.put("TPS", new TPSCollector(thisLogger).init());
    }
}
