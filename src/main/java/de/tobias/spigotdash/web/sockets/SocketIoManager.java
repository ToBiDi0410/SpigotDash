package de.tobias.spigotdash.web.sockets;

import de.tobias.spigotdash.main;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoSocket;

import java.util.ArrayList;
import java.util.Arrays;

public class SocketIoManager {

    public ArrayList<String> listenedEvents = new ArrayList<>(Arrays.asList("REQUEST", "AUTH"));

    public SocketIoManager() {}

    public void init() {
        SocketIoNamespace space = main.jetty.mSocketIoServer.namespace("/");
        space.on("connection", args -> {
            final SocketIoSocket socket = (SocketIoSocket) args[0];

            for(String s: listenedEvents) {
                socket.on(s, arguments -> {
                   SocketEventHandler.handleSocketEvent(socket, s, arguments);
                });
            }
        });
    }
}
