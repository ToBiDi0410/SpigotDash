package de.tobias.spigotdash.backend.io.WebsocketRequestHandlers;

import com.google.gson.JsonObject;
import de.tobias.simpsocserv.external.SimpleSocketRequestHandler;
import de.tobias.spigotdash.backend.storage.UserStore;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import de.tobias.spigotdash.models.User;

public class AuthenticationRequestHandler extends SimpleSocketRequestHandler {

    public AuthenticationRequestHandler() {
        super("AUTHENTICATION", "GET", (simpleSocketRequest, dataStorage) -> {
            JsonObject data = (JsonObject) simpleSocketRequest.getData();
            if (!data.has("SUBMETHOD")) {
                simpleSocketRequest.sendResponse(400, "MISSING_FIELD_SUBMETHOD");
                return true;
            }
            String subMethod = data.get("SUBMETHOD").getAsString();
            User currentUser = (User) dataStorage.get("userData");

            if (subMethod.equalsIgnoreCase("IS_LOGGED_IN")) {
                simpleSocketRequest.sendResponse(200, currentUser != null);
                return true;
            }

            if (subMethod.equalsIgnoreCase("GET_AVAILABLE_USERS")) {
                simpleSocketRequest.sendResponse(200, GlobalVariableStore.getUserStore().getUsernames());
                return true;
            }

            if (subMethod.equalsIgnoreCase("LOGIN")) {
                if (!data.has("NAME")) {
                    simpleSocketRequest.sendResponse(400, "MISSING_FIELD_NAME");
                    return true;
                }
                if (!data.has("PASSWORD")) {
                    simpleSocketRequest.sendResponse(400, "MISSING_FIELD_PASSWORD");
                    return true;
                }
                String NAME = data.get("NAME").getAsString();
                User u = ((UserStore) GlobalVariableStore.userJSONStore.getObject()).getUserByName(NAME);

                if (u == null) {
                    simpleSocketRequest.sendResponse(404, "USER_NOT_FOUND");
                    return true;
                }
                if (!u.validPassword(data.get("PASSWORD").getAsString())) {
                    simpleSocketRequest.sendResponse(400, "USER_INVALID_PASSWORD");
                    return true;
                }

                dataStorage.set("user", u);
                simpleSocketRequest.sendResponse(200, "LOGGED_IN");
                return true;
            }

            if (currentUser == null) {
                simpleSocketRequest.sendResponse(401, "REQUIRE_LOGIN");
                return true;
            }
            //ALL METHODS BELOW REQUIRE LOGIN

            if (subMethod.equalsIgnoreCase("GET_PERMISSIONS")) {
                simpleSocketRequest.sendResponse(200, currentUser.getPermissionSet());
                return true;
            }
            return false;
        });
    }
}
