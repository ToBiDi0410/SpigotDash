package de.tobias.spigotdash;

import java.io.File;
import java.nio.charset.StandardCharsets;

import de.tobias.spigotdash.web.sockets.SocketIoManager;
import de.tobias.spigotdash.web.jetty.JettyServer;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import de.tobias.spigotdash.commands.dashurl;
import de.tobias.spigotdash.listener.AltJoin;
import de.tobias.spigotdash.listener.JoinTime;
import de.tobias.spigotdash.utils.files.configuration;
import de.tobias.spigotdash.utils.files.jsonDatabase;
import de.tobias.spigotdash.utils.pluginConsole;
import de.tobias.spigotdash.utils.taskManager;
import de.tobias.spigotdash.utils.files.translations;
import de.tobias.spigotdash.web.NgrokManager;

public class main extends JavaPlugin {

	public static JettyServer jetty;
	public static SocketIoManager sockMan;
	public static jsonDatabase cacheFile;
	public static NgrokManager ngrok;
	public static Plugin pl;
	public static Metrics metrics;
	public static long latestStart = 0;
	
	public void onEnable() {
		try {
			pl = this;
			pluginConsole.sendMessage("&7----------- [  " + pluginConsole.CONSOLE_PREFIX + "&7] -----------");
			pluginConsole.sendMessage("&7Author(s): &b" + this.getDescription().getAuthors());
			pluginConsole.sendMessage("&7Version: &6" + this.getDescription().getVersion() + " &7(API: &6" + this.getDescription().getAPIVersion() + "&7)");
			pluginConsole.sendMessage("&cThank you for using this Plugin <3");
			pluginConsole.sendMessage("&7----------- [  " + pluginConsole.CONSOLE_PREFIX + "&7] -----------");

			pluginConsole.sendMessage("Starting Metrics...");
			metrics = new Metrics(this, 11869);
			pluginConsole.sendMessage("&aMetrics started!");

			//FILES
			if (!this.getDataFolder().exists()) getDataFolder().mkdir();
			configuration.init();
			translations.load();

			//DATABASE
			pluginConsole.sendMessage("Loading Cache File...");
			File cache = new File(main.pl.getDataFolder(), "cache.json");
			if(!cache.exists()) { cache.getParentFile().mkdirs(); cache.createNewFile(); FileUtils.write(cache, "{PERFORMANCE_DATA: []}", StandardCharsets.UTF_8);}
			cacheFile = new jsonDatabase(cache);
			cacheFile.read();
			pluginConsole.sendMessage("&aCache File loaded!");

			//JETTY SERVER
			jetty = new JettyServer(configuration.yaml_cfg.getInt("PORT"));
			jetty.init();

			sockMan = new SocketIoManager();
			sockMan.init();

			//NGROK
			if(configuration.yaml_cfg.getBoolean("USE_NGROK")) {
				ngrok = new NgrokManager(jetty.port);
				ngrok.ngrokClient.setAuthToken(configuration.yaml_cfg.getString("NGROK_AUTH"));
				ngrok.connect();
				taskManager.lastNgrokUpdate = System.currentTimeMillis();
			}
			
			//TASKS
			taskManager.startTasks();

			//EVENT LISTENERS
			Bukkit.getPluginManager().registerEvents(new JoinTime(), main.pl);
			JoinTime.enableSet();
			Bukkit.getPluginManager().registerEvents(new AltJoin(), main.pl);

			//COMMANDS
			Bukkit.getPluginCommand("dashurl").setExecutor(new dashurl());

			pluginConsole.sendMessage("&5Everything (seems to be) done!");
			latestStart = System.currentTimeMillis();
		} catch (Exception ex) {
			pluginConsole.sendMessage("&7----------- [  " + pluginConsole.CONSOLE_PREFIX + "&7] -----------");
			pluginConsole.sendMessage("&cINIT FAILURE! This error is currently unrecoverable!");
			ex.printStackTrace();
			pluginConsole.sendMessage("&7----------- [  " + pluginConsole.CONSOLE_PREFIX + "&7] -----------");

			try {
				taskManager.stopTasks();
			} catch(Exception ignored) {}
			
			try {
				ngrok.destroy();
			} catch(Exception ignored) {}

			try {
				jetty.destroy();
			} catch(Exception ignored) {}

		}

	}

	public void onDisable() {
		cacheFile.save();
		jetty.destroy();
		taskManager.stopTasks();
	}
}
