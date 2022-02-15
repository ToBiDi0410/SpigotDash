package de.tobias.spigotdash.backend.io.socket;

import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import de.tobias.spigotdash.main;
import io.socket.emitter.Emitter;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.JettyWebSocketHandler;
import io.socket.socketio.server.SocketIoNamespace;
import io.socket.socketio.server.SocketIoServer;
import io.socket.socketio.server.SocketIoSocket;
import org.bukkit.Bukkit;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WebsocketServerManager {

    private Server server;
    private EngineIoServer engineIoServer;
    private SocketIoServer socketIoServer;

    private fieldLogger thisLogger = new fieldLogger("SOCSRV", globalLogger.constructed);

    private Integer port;
    private HashMap<String, WebsocketEventReciever> events = new HashMap<>();

    public WebsocketServerManager(Integer port) {
        this.port = port;
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
    }

    public void init() {
        thisLogger.INFO("Setting up Socket Server...", 0);
        server = new Server(this.port);
        engineIoServer = new EngineIoServer();
        socketIoServer = new SocketIoServer(engineIoServer);
        thisLogger.INFO("New Objects constructed", 10);

        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        servletContextHandler.setContextPath("/");

        servletContextHandler.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
                try {
                    engineIoServer.handleRequest(new HttpServletRequestWrapper(request) {
                        @Override
                        public boolean isAsyncSupported() {
                            return true;
                        }
                    }, response);
                } catch(Exception ex) {
                    response.sendError(500);
                    thisLogger.ERROREXEP("Failed to resolve Request: ", ex, 10);
                }
            }
        }), "/socket.io/*");

        try {
            WebSocketUpgradeFilter webSocketUpgradeFilter = WebSocketUpgradeFilter.configureContext(servletContextHandler);
            webSocketUpgradeFilter.addMapping(
                    new ServletPathSpec("/socket.io/*"),
                    (servletUpgradeRequest, servletUpgradeResponse) -> new JettyWebSocketHandler(engineIoServer)
            );
        } catch (ServletException ex) {
            ex.printStackTrace();
        }

        thisLogger.INFO("Paths added to handler", 10);

        HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[] { servletContextHandler });
        server.setHandler(handlerList);
        thisLogger.INFO("Handler registered", 10);

        events.put("WEBREQ", new WebsocketRequestV1Handler());
        registerNamespace();
        thisLogger.INFO("Namespaces registered", 10);

        try {
            server.start();
            thisLogger.INFO("Server started with Port: " + this.port, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void registerEventReciever(String eventName, WebsocketEventReciever reciever) {
        events.put(eventName, reciever);
    }

    private void registerNamespace() {
        SocketIoNamespace namespace = socketIoServer.namespace("/");
        namespace.on("connection", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                SocketIoSocket socket = (SocketIoSocket) args[0];
                thisLogger.INFO("New Socket '" + socket.getId() + "' connected", 10);

                for(Map.Entry<String, WebsocketEventReciever> event : events.entrySet()) {
                    socket.on(event.getKey(), new Emitter.Listener() {
                        @Override
                        public void call(Object... args) {
                            Bukkit.getScheduler().runTask(main.pl, new Runnable() {
                                @Override
                                public void run() {
                                    thisLogger.INFO("Running Event for Socket '" + socket.getId() + "': " + event.getKey(), 20);
                                    event.getValue().handle(event.getKey(), socket, args);
                                }
                            });
                        }
                    });
                    thisLogger.INFO("Registered Event for Socket '" + socket.getId() + "': " + event.getKey(), 20);
                }
            }
        });
    }

}

