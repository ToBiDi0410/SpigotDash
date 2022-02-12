package de.tobias.spigotdash;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class main extends JavaPlugin {

	public static Plugin pl;
	public static Charset GLOBAL_CHARSET = StandardCharsets.UTF_8;
	public static Gson GLOBAL_GSON = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();

	public void onEnable() {

		try {
			pl = this;
			new globalLogger(null);
			globalLogger.constructed.activateDevDebug(); //TODO REMOVE FOR PRODUCTION
			fieldLogger thisLogger = new fieldLogger("INIT", globalLogger.constructed);

			thisLogger.INFO("Starting up Plugin...", 0);

			thisLogger.WARNING("It seems you have enabled the Debug Log!", 1);
			thisLogger.WARNING("Please never share information from here to someone unknown!", 1);

		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public void onDisable() {
	}
}
