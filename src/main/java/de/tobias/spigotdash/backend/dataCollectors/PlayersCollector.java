package de.tobias.spigotdash.backend.dataCollectors;

import com.google.gson.JsonObject;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import org.bukkit.Bukkit;

public class PlayersCollector implements dataCollectionRequestHandler.dataCollectionHandler {

    private final fieldLogger thisLogger;

    public PlayersCollector(fieldLogger fl) {
        thisLogger = fl.subFromParent("PLAYERS");
    }

    public PlayersCollector init() {
        //Start a Task that will execute every 100 Ticks (every 5 in-game Second) for Data collection
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(GlobalVariableStore.pl, this::task, 100L, 20L);
        thisLogger.INFO("Started Collection Service", 0);
        return this;
    }

    private void task() {}

    public Integer getCurrentPlayercount() {
        return Bukkit.getOnlinePlayers().size();
    }

    @Override
    public Object REQUEST_DATA(String dataID, JsonObject data) {
        if(dataID.equalsIgnoreCase("COUNT")) return this.getCurrentPlayercount();
        return null;
    }
}
