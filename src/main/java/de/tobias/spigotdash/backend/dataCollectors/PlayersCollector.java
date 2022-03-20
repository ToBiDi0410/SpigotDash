package de.tobias.spigotdash.backend.dataCollectors;

import com.google.gson.JsonObject;
import de.tobias.spigotdash.backend.io.WebsocketRequestHandlers.DataCollectionRequestHandler;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.models.DataPoint;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayersCollector implements DataCollectionRequestHandler.dataCollectionHandler {

    private final fieldLogger thisLogger;

    public PlayersCollector(fieldLogger fl) {
        thisLogger = fl.subFromParent("PLAYERS");
    }

    public PlayersCollector init() {
        thisLogger.INFO("Started Collection Service", 0);
        return this;
    }

    public Integer getPlayerCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    public ArrayList<String> getPlayerUUIDs() {
        ArrayList<String> uuids = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()) {
            uuids.add(p.getUniqueId().toString());
        }
        return uuids;
    }

    public DataPoint getCacheDataPoint() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("PlayerCount", getPlayerCount());
        data.put("PlayerUUIDs", getPlayerUUIDs());

        return new DataPoint(data);
    }

    @Override
    public Object REQUEST_DATA(String dataID, JsonObject data) {
        if(dataID.equalsIgnoreCase("COUNT")) return this.getPlayerCount();
        return null;
    }
}
