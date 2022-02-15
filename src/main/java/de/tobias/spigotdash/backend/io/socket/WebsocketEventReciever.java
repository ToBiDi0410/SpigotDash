package de.tobias.spigotdash.backend.io.socket;

import io.socket.socketio.server.SocketIoSocket;

public interface WebsocketEventReciever {
    boolean handle(String eventName, SocketIoSocket soc, Object... args);
}
