package de.tobias.spigotdash.backend.io;

import de.tobias.simpsocserv.serverManagement.SimpleSocServer;
import de.tobias.spigotdash.backend.io.WebsocketRequestHandlers.AuthenticationRequestHandler;

public class ServerManager {

    public SimpleSocServer simpServer;

    public ServerManager() {
        simpServer = new SimpleSocServer();
    }

    public void start() {
        simpServer.start();
        simpServer.addSimpleSocketRequestHandler(new AuthenticationRequestHandler());
    }

    public void stop() {
        simpServer.stop();
    }
}
