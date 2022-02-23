package de.tobias.spigotdash.backend.io.socket;

import de.tobias.spigotdash.models.User;
import io.socket.socketio.server.SocketIoSocket;

import java.util.HashMap;

public class SocketDataStore {

    public static HashMap<String, HashMap<String, Object>> data = new HashMap<>();

    public static boolean hasData(SocketIoSocket soc) {
        return data.containsKey(soc.getId());
    }

    public static User getUser(SocketIoSocket soc) {
        if(!hasData(soc)) return null;
        return (User) data.get(soc.getId()).get("USER");
    }

    public static boolean hasUserData(SocketIoSocket soc) {
        return getUser(soc) != null;
    }

    public static void initIfNotPresent(SocketIoSocket soc) {
        if(!hasData(soc)) {
            data.put(soc.getId(), new HashMap<>());
        }
    }

    public static void setUserData(SocketIoSocket soc, User setdata) {
        initIfNotPresent(soc);
        data.get(soc.getId()).put("USER", setdata);
    }
}
