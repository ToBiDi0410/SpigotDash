package de.tobias.spigotdash.backend.dataCollectors;

import com.google.gson.JsonObject;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import org.bukkit.Bukkit;

public class TPSCollector implements dataCollectionRequestHandler.dataCollectionHandler {

    private long LAST_TICK_TIME = 0;
    private float TPS = 20;
    private float TPS_AVG = 20;
    private float TPS_AVG_STORE = 0;
    private float TPS_AVG_PASSED = 0;

    private final fieldLogger thisLogger;

    public TPSCollector(fieldLogger fl) {
        thisLogger = fl.subFromParent("TPS");
    }

    public TPSCollector init() {
        //Start a Task that will execute every 20 Ticks (every in-game Second) for Data collection
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(GlobalVariableStore.pl, this::task, 20L, 20L);
        thisLogger.INFO("Started Collection Service", 0);
        return this;
    }

    public float getTPS() {
        return this.TPS;
    }

    public float getAverageTPS() {
        return this.TPS_AVG;
    }

    private void task() {
        long tickDiff = System.currentTimeMillis() - LAST_TICK_TIME;
        float tickDiffSeconds = (tickDiff / (1000.00f));

        //Calculate TPS based on the Seconds the last Ticks took
        TPS = 20 / tickDiffSeconds;

        //Modify Variables for the Average TPS
        TPS_AVG_STORE += TPS;
        TPS_AVG_PASSED++;

        //Calculate Average TPS from Last 5 Samples if persistent
        if(TPS_AVG_PASSED >= 5) {
            TPS_AVG = TPS_AVG_STORE / TPS_AVG_PASSED;
            TPS_AVG_PASSED = 0;
            TPS_AVG_STORE = 0;
        }

        LAST_TICK_TIME = System.currentTimeMillis();
    }

    public Object REQUEST_DATA(String dataID, JsonObject json) {
        if(dataID.equalsIgnoreCase("TPS")) return this.TPS;
        if(dataID.equalsIgnoreCase("TPS_AVG")) return this.TPS_AVG;
        return null;
    }
}
