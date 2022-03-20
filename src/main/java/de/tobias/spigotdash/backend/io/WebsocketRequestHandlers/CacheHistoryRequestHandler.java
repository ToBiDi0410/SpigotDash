package de.tobias.spigotdash.backend.io.WebsocketRequestHandlers;

import com.google.gson.JsonObject;
import de.tobias.simpsocserv.external.SimpleSocketRequestHandler;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;

public class CacheHistoryRequestHandler extends SimpleSocketRequestHandler {

    public CacheHistoryRequestHandler() {
        super("CACHE", "GET", (simpleSocketRequest, dataStorage) -> {
            JsonObject data = simpleSocketRequest.getData().getAsJsonObject();
            if(!data.has("ID")) { simpleSocketRequest.sendResponse(400, "MISSING_FIELD_ID"); return true; }
            String ID = data.get("ID").getAsString();

            // TODO: 25.02.2022 Add Permissions
            if(ID.equalsIgnoreCase("TPS")) {
                simpleSocketRequest.sendResponse(200, GlobalVariableStore.getCacheStore().HISTORY_TPS);
                return true;
            }

            if(ID.equalsIgnoreCase("HARDWARE")) {
                simpleSocketRequest.sendResponse(200, GlobalVariableStore.getCacheStore().HISTORY_HARDWARE);
                return true;
            }

            if(ID.equalsIgnoreCase("PLAYERS")) {
                simpleSocketRequest.sendResponse(200, GlobalVariableStore.getCacheStore().HISTORY_PLAYERS);
                return true;
            }

            simpleSocketRequest.sendResponse(400, "INVALID_ID");
            return true;
        });
    }
}
