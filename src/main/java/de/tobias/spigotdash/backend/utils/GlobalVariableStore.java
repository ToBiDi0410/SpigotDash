package de.tobias.spigotdash.backend.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tobias.spigotdash.backend.io.http.HttpServerManager;
import de.tobias.spigotdash.backend.io.socket.WebsocketServerManager;
import org.bukkit.plugin.Plugin;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class GlobalVariableStore {

    public static Plugin pl;
    public static final Charset CHARSET = StandardCharsets.UTF_8;
    public static final Gson GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

    public static HttpServerManager mainHttpServer;
    public static WebsocketServerManager mainSocketServer;
}
