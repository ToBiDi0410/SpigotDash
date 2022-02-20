package de.tobias.spigotdash.backend.dataCollectors;

import com.google.gson.JsonObject;
import de.tobias.spigotdash.backend.io.socket.WebsocketRequestV1Handler;

public class dataCollectionRequestHandler {

    public static final WebsocketRequestV1Handler.subHandler handler = (res, data) -> {
        if (!data.has("COLLECTOR")) {
            res.setData("MISSING_COLLECTOR").setCode(400).send();
            return;
        }

        if (!data.has("DATAID")) {
            res.setData("MISSING_DATAID").setCode(400).send();
            return;
        }

        String collectorName = data.get("COLLECTOR").getAsString();
        Object collector = dataCollectionManager.collectors.get(collectorName);

        if (collector == null) {
            res.setData("UNKNOWN_COLLECTOR").setCode(404).send();
            return;
        }

        Object responseDat = ((dataCollectionHandler) collector).REQUEST_DATA(data.get("DATAID").getAsString(), data);
        if(responseDat == null) {
            res.setData("EMPTY_DATA").setCode(404).send();
            return;
        }

        res.setData(responseDat).setCode(200).send();
    };

    public interface dataCollectionHandler {
        Object REQUEST_DATA(String dataID, JsonObject data);
    }
}
