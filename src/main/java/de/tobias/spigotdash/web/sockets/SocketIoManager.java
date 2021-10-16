package de.tobias.spigotdash.web.sockets;

import com.google.gson.Gson;
import de.tobias.spigotdash.main;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoSocket;

import java.util.ArrayList;
import java.util.Arrays;

public class SocketIoManager {

    public ArrayList<String> listenedEvents = new ArrayList<>(Arrays.asList("REQUEST", "AUTH"));
    public static ArrayList<SocketIoSocket> connectedSockets = new ArrayList<>();
    public static SocketIoNamespace space = main.jetty.mSocketIoServer.namespace("/");

    public SocketIoManager() {}

    public void init() {
        space.on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];

            for(String s: listenedEvents) {
                socket.on(s, arguments -> SocketEventHandler.handleSocketEvent(socket, s, arguments));
            }

            connectedSockets.add(socket);
            socket.on("disconnect", arguments -> connectedSockets.remove(socket));
        });
    }

    public void sendToAllSockets(String eventname, Object data) {
        String dataJSON = (new Gson()).toJson(data);
        for(SocketIoSocket socket : connectedSockets) {
            socket.send(eventname, dataJSON);
            //pluginConsole.sendMessage("Sending Data to Socket with ID " + socket.getId() + ": " + dataJSON);
        }
    }
}
