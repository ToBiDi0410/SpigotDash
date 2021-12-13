package de.tobias.spigotdash.web.sockets;

import de.tobias.spigotdash.utils.files.configuration;
import de.tobias.spigotdash.web.PermissionSet;
import io.socket.socketio.server.SocketIoSocket;

import java.util.HashMap;

public class SocketAuthManager {

    public static HashMap<String, HashMap<String, Object>> socketAuths = new HashMap<>();

    public static void authSocket(String name, String password, SocketIoSocket socket, SocketRequest socreq) {
        if(isValid(name, password)) {
            HashMap<String, Object> data = new HashMap<>();
            // TODO: 13.12.21 Load Permission Set from File
            PermissionSet perms = new PermissionSet();
            perms.setAllTo(true);
            data.put("permissions", perms);
            socketAuths.put(socket.getId(), data);
            socreq.setResponse(200, "TEXT", "LOGGED_IN");
        } else {
            socreq.setResponse(400, "TEXT", "ERR_WRONG_NAME_OR_PASSWORD");
        }
    }

    public static PermissionSet getPermissions(SocketIoSocket soc) {
        if(socketAuths.containsKey(soc.getId())) {
            if(socketAuths.get(soc.getId()) != null && socketAuths.get(soc.getId()).containsKey("permissions")) {
                return (PermissionSet) socketAuths.get(soc.getId()).get("permissions");
            }
        }

        return null;
    }

    public static boolean isAuthed(SocketIoSocket soc) {
        return socketAuths.containsKey(soc.getId());
    }

    public static boolean isValid(String username, String password) {
        username = username.toLowerCase();
        return username.equalsIgnoreCase("admin") && password.equals(configuration.CFG.get("WEB_PASSWORD").toString());
    }
}
