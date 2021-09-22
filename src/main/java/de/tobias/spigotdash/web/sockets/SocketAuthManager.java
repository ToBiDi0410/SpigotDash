package de.tobias.spigotdash.web.sockets;

import de.tobias.spigotdash.utils.files.configuration;
import io.socket.socketio.server.SocketIoSocket;

import java.util.HashMap;

public class SocketAuthManager {

    public static HashMap<String, HashMap<String, Object>> socketAuths = new HashMap<>();

    public static void authSocket(String name, String password, SocketIoSocket socket, SocketRequest socreq) {
        if(isValid(name, password)) {
            socketAuths.put(socket.getId(), new HashMap<>());
            socreq.setResponse(200, "TEXT", "LOGGED_IN");
            return;
        } else {
            socreq.setResponse(400, "TEXT", "ERR_WRONG_NAME_OR_PASSWORD");
            return;
        }
    }

    public static boolean isAuthed(SocketIoSocket soc) {
        if(socketAuths.containsKey(soc.getId())) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValid(String username, String password) {
        username = username.toLowerCase();
        if(username.equalsIgnoreCase("admin") && password.equals(configuration.CFG.get("WEB_PASSWORD").toString())) {
            return true;
        }
        return false;
    }
}
