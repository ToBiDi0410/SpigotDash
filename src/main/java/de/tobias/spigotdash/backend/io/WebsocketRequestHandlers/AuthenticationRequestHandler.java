package de.tobias.spigotdash.backend.io.WebsocketRequestHandlers;

import de.tobias.spigotdash.backend.io.socket.SocketDataStore;
import de.tobias.spigotdash.backend.io.socket.WebsocketRequestV1Handler;
import de.tobias.spigotdash.backend.storage.UserStore;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import de.tobias.spigotdash.models.User;

public class AuthenticationRequestHandler {

    public static final WebsocketRequestV1Handler.subHandler handler = (res, data) -> {
        if(!data.has("SUBMETHOD")) { res.setCode(400).setData("MISSING_FIELD_SUBMETHOD").send(); return; }
        String subMethod = data.get("SUBMETHOD").getAsString();
        User currentUser = SocketDataStore.getUser(res.getSocket());

        if(subMethod.equalsIgnoreCase("IS_LOGGED_IN")) {
            res.setCode(200).setData(currentUser != null).send();
            return;
        }

        if(subMethod.equalsIgnoreCase("LOGIN")) {
            if(!data.has("NAME")) { res.setCode(400).setData("MISSING_FIELD_NAME").send(); return; }
            if(!data.has("PASSWORD")) { res.setCode(400).setData("MISSING_FIELD_PASSWORD").send(); return; }
            String NAME = data.get("NAME").getAsString();
            User u = ((UserStore) GlobalVariableStore.userJSONStore.getObject()).getUserByName(NAME);

            if(u == null) { res.setCode(404).setData("USER_NOT_FOUND").send(); return; }
            if(!u.validPassword(data.get("PASSWORD").getAsString())) { res.setCode(400).setData("USER_INVALID_PASSWORD").send(); return; }

            SocketDataStore.setUserData(res.getSocket(), u);
            res.setCode(200).setData("LOGGED_IN").send();
            return;
        }

        if(currentUser == null) { res.setCode(401).setData("REQUIRE_LOGIN").send(); return; }
        //ALL METHODS BELOW REQUIRE LOGIN

        if(subMethod.equalsIgnoreCase("GET_PERMISSIONS")) {
            res.setCode(200).setData(currentUser.getPermissionSet()).send();
            return;
        }
    };
}
