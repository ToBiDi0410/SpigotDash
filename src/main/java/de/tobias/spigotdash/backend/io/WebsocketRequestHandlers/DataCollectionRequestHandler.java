package de.tobias.spigotdash.backend.io.WebsocketRequestHandlers;

import com.google.gson.JsonObject;
import de.tobias.simpsocserv.external.SimpleSocketRequestHandler;
import de.tobias.spigotdash.backend.dataCollectors.DataCollectionManager;

public class DataCollectionRequestHandler extends SimpleSocketRequestHandler {

    public DataCollectionRequestHandler() {
        super("COLLECTOR_DATA", "GET", (simpleSocketRequest, dataStorage) -> {
            JsonObject data = simpleSocketRequest.getData().getAsJsonObject();
            if (!data.has("COLLECTOR")) {
                simpleSocketRequest.sendResponse(400, "MISSING_COLLECTOR");
                return true;
            }

            if (!data.has("DATAID")) {
                simpleSocketRequest.sendResponse(400, "MISSING_DATAID");
                return true;
            }

            String collectorName = data.get("COLLECTOR").getAsString();
            Object collector = DataCollectionManager.collectors.get(collectorName);

            if (collector == null) {
                simpleSocketRequest.sendResponse(404, "UNKNOWN_COLLECTOR");
                return true;
            }

            //if (res.respondWithPermissionError("COLLECTOR_" + collectorName.toUpperCase(Locale.ROOT).replace(" ", "_"))) return;

            Object responseDat = ((dataCollectionHandler) collector).REQUEST_DATA(data.get("DATAID").getAsString(), data);
            if(responseDat == null) {
                simpleSocketRequest.sendResponse(404, "EMPTY_DATA");
                return true;
            }

            simpleSocketRequest.sendResponse(200, responseDat);
            return true;
        });
    }

    public interface dataCollectionHandler {
        Object REQUEST_DATA(String dataID, JsonObject data);
    }
}
