package de.tobias.spigotdash.backend.dataCollectors;

import com.google.gson.JsonObject;
import com.sun.management.OperatingSystemMXBean;
import de.tobias.spigotdash.backend.io.WebsocketRequestHandlers.DataCollectionRequestHandler;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.models.DataPoint;

import java.lang.management.ManagementFactory;
import java.util.HashMap;

public class HardwareCollector implements DataCollectionRequestHandler.dataCollectionHandler {

    private final fieldLogger thisLogger;

    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    public HardwareCollector(fieldLogger fl) {
        thisLogger = fl.subFromParent("HARDWARE");
    }

    public HardwareCollector init() {
        thisLogger.INFO("Started Collection Service", 0);
        return this;
    }

    public double getHostCpuLoad() {
        return osBean.getSystemCpuLoad();
    }

    public double getProcessLoad() {
        return osBean.getProcessCpuLoad();
    }

    public double getMemoryFree(){
        return osBean.getFreePhysicalMemorySize();
    }

    public double getMemoryInstalled() {
        return osBean.getTotalPhysicalMemorySize();
    }

    public double getMemoryUsed() {
        return getMemoryInstalled() - getMemoryFree();
    }

    public DataPoint getCacheDataPoint() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("HostCpuLoad", getHostCpuLoad());
        data.put("ProcessLoad", getProcessLoad());
        data.put("MemoryFree", getMemoryFree());
        data.put("MemoryInstalled", getMemoryInstalled());
        data.put("MemoryUsed", getMemoryUsed());

        return new DataPoint(data);
    }

    @Override
    public Object REQUEST_DATA(String dataID, JsonObject data) {
        return null;
    }
}
