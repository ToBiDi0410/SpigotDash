package de.tobias.spigotdash.web.jetty;

import de.tobias.spigotdash.utils.errorCatcher;
import de.tobias.spigotdash.utils.pluginConsole;
import io.socket.engineio.server.EngineIoServer;
import io.socket.engineio.server.JettyWebSocketHandler;
import io.socket.socketio.server.SocketIoServer;
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

public class JettyServer {

    public Integer port;
    public Server server;
    public ServletContextHandler handler;
    public EngineIoServer mEngineIoServer;
    public SocketIoServer mSocketIoServer;

    public JettyServer(Integer port) {
        this.port = port;
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
    }

    public void init() {
        try {
            pluginConsole.sendMessage("Configuring Jetty Server...");
            this.server = new Server(this.port);
            mEngineIoServer = new EngineIoServer();
            mSocketIoServer = new SocketIoServer(mEngineIoServer);
            handler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            handler.setContextPath("/");

            registerSocketIoRoutes();
            registerRoutes();

            HandlerList handlerList = new HandlerList();
            handlerList.setHandlers(new Handler[] { handler });
            server.setHandler(handlerList);

            pluginConsole.sendMessage("Starting Jetty Server using Port: &6" + this.port);
            server.start();
            pluginConsole.sendMessage("&aJetty Server started!");
        } catch (Exception e) {
            pluginConsole.sendMessage("&cFailed to init Jetty Server: ");
            errorCatcher.catchException(e, false);
        }
    }

    public void registerRoutes() {
        handler.addServlet(FaviconServlet.class, "/favicon.ico");
        handler.addServlet(IndexServlet.class, "/");
        handler.addServlet(MainServlet.class, "/web/*");
    }

    public void registerSocketIoRoutes() {
        handler.addServlet(new ServletHolder(new HttpServlet() {
            @Override
            protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
                mEngineIoServer.handleRequest(new HttpServletRequestWrapper(request) {
                    @Override
                    public boolean isAsyncSupported() {
                        return false;
                    }
                }, response);
            }
        }), "/socket.io/*");

        try {
            WebSocketUpgradeFilter webSocketUpgradeFilter = WebSocketUpgradeFilter.configureContext(handler);
            webSocketUpgradeFilter.addMapping(
                    new ServletPathSpec("/socket.io/*"),
                    (servletUpgradeRequest, servletUpgradeResponse) -> new JettyWebSocketHandler(mEngineIoServer));
        } catch (ServletException ex) {
            ex.printStackTrace();
        }
    }

    public void destroy() {
        try {
            server.stop();
        } catch (Exception e) {
            pluginConsole.sendMessage("&cCloud not stop Jetty Server: ");
            errorCatcher.catchException(e, false);
        }
    }
}
