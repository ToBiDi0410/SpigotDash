package de.tobias.spigotdash.backend.storage;

import de.tobias.spigotdash.backend.dataCollectors.HardwareCollector;
import de.tobias.spigotdash.backend.dataCollectors.PlayersCollector;
import de.tobias.spigotdash.backend.dataCollectors.TPSCollector;
import de.tobias.spigotdash.backend.dataCollectors.DataCollectionManager;
import de.tobias.spigotdash.models.DataPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class CacheStore {

    private long MAX_AGE = 0;
    public Date LAST_SAVED = new Date();

    public ArrayList<DataPoint> HISTORY_HARDWARE = new ArrayList<>();
    public ArrayList<DataPoint> HISTORY_PLAYERS = new ArrayList<>();
    public ArrayList<DataPoint> HISTORY_TPS = new ArrayList<>();

    public CacheStore() {
        MAX_AGE = 1000 * 60;
    }

    public void collectData() {
        this.HISTORY_HARDWARE.add(((HardwareCollector) DataCollectionManager.collectors.get("HARDWARE")).getCacheDataPoint());
        this.HISTORY_PLAYERS.add(((PlayersCollector) DataCollectionManager.collectors.get("PLAYERS")).getCacheDataPoint());
        this.HISTORY_TPS.add(((TPSCollector) DataCollectionManager.collectors.get("TPS")).getCacheDataPoint());

        this.cleanup();
    }

    public void cleanup() {
        cleanupIterator(HISTORY_HARDWARE.iterator());
        cleanupIterator(HISTORY_PLAYERS.iterator());
        cleanupIterator(HISTORY_TPS.iterator());
    }

    private void cleanupIterator(Iterator<DataPoint> it) {
        while(it.hasNext()) {
            DataPoint p = it.next();
            if(pointIsDeprecated(p)) it.remove();
        }
    }

    public boolean pointIsDeprecated(DataPoint p) {
        return (System.currentTimeMillis() - p.TIME.getTime() > MAX_AGE);
    }
}
