package de.tobias.spigotdash.backend.io.http;

import de.tobias.spigotdash.backend.io.socket.WebsocketServerManager;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

public class HttpServerManager {

    Integer port;

    private final fieldLogger thisLogger = new fieldLogger("HTTPSRV", globalLogger.constructed);
    private Server server;
    private WebsocketServerManager wsServer;

    public static HttpServerManager constructed;

    public HttpServerManager(Integer port) {
        this.port = port;
        constructed = this;
        System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.StdErrLog");
        System.setProperty("org.eclipse.jetty.LEVEL", "OFF");
    }

    public void init() {
        try {
            server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.setPort(this.port);
            server.setConnectors(new Connector[] { connector });

            ServletHandler servletHandler = new ServletHandler();
            servletHandler.addServletWithMapping(WebresourceServlet.class, "/*");
            server.setHandler(servletHandler);
            server.start();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean start() {
        thisLogger.INFO("Starting Server...", 0);
        try {
            server.start();
            thisLogger.INFO("Server started with Port: " + this.port, 0);
            return true;
        } catch (Exception ex) {
            thisLogger.ERROREXEP("Failed to start Server: ", ex, 0);
            return false;
        }
    }

    public boolean stop() {
        thisLogger.INFO("Stopping Server...", 0);
        try {
            server.stop();
            thisLogger.INFO("Server stopped", 0);
            return true;
        } catch (Exception ex) {
            thisLogger.ERROREXEP("Failed to stop Server: ", ex, 0);
            return false;
        }
    }

    public fieldLogger getFieldLogger() {
        return thisLogger;
    }
}
