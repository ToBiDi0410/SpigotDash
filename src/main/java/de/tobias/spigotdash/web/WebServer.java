package de.tobias.spigotdash.web;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import de.tobias.spigotdash.utils.errorCatcher;
import de.tobias.spigotdash.utils.pluginConsole;

public class WebServer {

	public Integer port;
	public HttpServer server;

	public WebServer(Integer port) {
		this.port = port;
	}

	public boolean setup() {
		try {
			pluginConsole.sendMessage("Starting Webserver under Port " + this.port + "...");
			this.server = HttpServer.create(new InetSocketAddress(port), 0);
			server.createContext("/", new MainRequestHandler());
			server.setExecutor(null);
			server.start();
			pluginConsole.sendMessage("&aWebserver started!");
			return true;
		} catch (IOException e) {
			errorCatcher.catchException(e, false);
			return false;
		}

	}
	
	public void destroy() {
		server.stop(1);
	}
}
