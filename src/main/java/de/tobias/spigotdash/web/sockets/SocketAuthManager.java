package de.tobias.spigotdash.web.sockets;

import de.tobias.spigotdash.main;
import de.tobias.spigotdash.utils.SerialUtils;
import de.tobias.spigotdash.utils.files.Group;
import de.tobias.spigotdash.utils.files.User;
import de.tobias.spigotdash.utils.files.configuration;
import de.tobias.spigotdash.web.PermissionSet;
import io.socket.socketio.server.SocketIoSocket;
import org.apache.commons.lang.SerializationUtils;

import java.io.Serializable;
import java.util.HashMap;

public class SocketAuthManager {

    public static HashMap<String, HashMap<String, Object>> socketAuths = new HashMap<>();

    public static void authSocket(String name, String password, SocketIoSocket socket, SocketRequest socreq) {
        if(main.UsersFile.userExists(name)) {
            User u = main.UsersFile.getUserByName(name);
            if(u.validPassword(password)) {
                HashMap<String, Object> data = new HashMap<>();
                data.put("user", u);
                data.put("perms", u.getCalculatedPerms());
                socketAuths.put(socket.getId(), data);
                socreq.setResponse(200, "TEXT", "LOGGED_IN");
            } else {
                socreq.setResponse(400, "TEXT", "ERR_WRONG_PASSWORD");
            }
        } else {
            socreq.setResponse(404, "TEXT", "ERR_USER_NOT_FOUND");
        }

    }

    public static PermissionSet getPermissions(SocketIoSocket soc) {
        if(socketAuths.containsKey(soc.getId())) {
            if(socketAuths.get(soc.getId()) != null && socketAuths.get(soc.getId()).containsKey("perms")) {
                return (PermissionSet) socketAuths.get(soc.getId()).get("perms");
            }
        }

        return null;
    }

    public static boolean isAuthed(SocketIoSocket soc) {
        return socketAuths.containsKey(soc.getId());
    }
}
