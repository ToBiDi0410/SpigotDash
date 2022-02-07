package de.tobias.spigotdash;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.tobias.spigotdash.integrations.SkriptIntegration;
import de.tobias.spigotdash.utils.files.*;
import de.tobias.spigotdash.web.PermissionSet;
import de.tobias.spigotdash.web.jetty.WebServerFileRoot;
import de.tobias.spigotdash.web.sockets.SocketIoManager;
import de.tobias.spigotdash.web.jetty.JettyServer;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import de.tobias.spigotdash.commands.dashurl;
import de.tobias.spigotdash.listener.AltJoin;
import de.tobias.spigotdash.listener.JoinTime;
import de.tobias.spigotdash.utils.pluginConsole;
import de.tobias.spigotdash.utils.taskManager;
import de.tobias.spigotdash.web.NgrokManager;

public class main extends JavaPlugin {

	public static JettyServer jetty;
	public static SocketIoManager sockMan;
	public static jsonDatabase cacheFile;
	public static NgrokManager ngrok;
	public static Plugin pl;
	public static Metrics metrics;
	public static long latestStart = 0;
	public static WebServerFileRoot webroot;
	public static usersFile UsersFile;
	public static groupsFile GroupsFile;
	public static configurationFile config;
	public static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

	public static SkriptIntegration skriptIntegration;
	
	public void onEnable() {
		try {
			pl = this;
			pluginConsole.sendMessage("&7----------- [  " + pluginConsole.CONSOLE_PREFIX + "&7] -----------");
			pluginConsole.sendMessage("&7Author(s): &b" + this.getDescription().getAuthors());
			pluginConsole.sendMessage("&7Version: &6" + this.getDescription().getVersion()/* + " &7(API: &6" + this.getDescription().getAPIVersion() + "&7)"*/);
			pluginConsole.sendMessage("&cThank you for using this Plugin <3");
			pluginConsole.sendMessage("&7----------- [  " + pluginConsole.CONSOLE_PREFIX + "&7] -----------");

			pluginConsole.sendMessage("Processing Debug Files...");
			if(new File(this.getDataFolder(), "EXTWEB").exists()) {
				pluginConsole.sendMessage("&6[NOTE] Using External Folder because of EXTWEB File in Plugin Folder");
				webroot = new WebServerFileRoot("FILE", new File(this.getDataFolder(), "/www/"));
			} else {
				webroot = new WebServerFileRoot("RES", "/www");
			}

			pluginConsole.sendMessage("Starting Metrics...");
			metrics = new Metrics(this, 11869);
			pluginConsole.sendMessage("&aMetrics started!");

			//FILES
			if (!this.getDataFolder().exists()) getDataFolder().mkdir();
			config = configurationFile.getFromFile(new File(main.pl.getDataFolder(), "config.json"));
			translations.load();

			//GROUPS
			GroupsFile = groupsFile.getFromFile(new File(main.pl.getDataFolder(), "groups.json"));
			GroupsFile.save();

			Group adminGroup = GroupsFile.getAdminGroup();
			if(adminGroup == null) {
				adminGroup = new Group("Admin", new PermissionSet());
				adminGroup.permissions.setAllTo(true);
				adminGroup.html_color = "#c73f45";
				adminGroup.LEVEL = 100;
				adminGroup.IS_ADMIN_GROUP = true;
				adminGroup.permissions.USERS_IS_ADMIN = true;
				GroupsFile.addGroup(adminGroup);
			}

			if(GroupsFile.getDefaultGroup() == null) {
				Group defaultGroup = new Group("Default", new PermissionSet());
				defaultGroup.html_color = "#4a4a4a";
				defaultGroup.IS_DEFAULT_GROUP = true;
				GroupsFile.addGroup(defaultGroup);
			}

			//USERS
			UsersFile = usersFile.getFromFile(new File(main.pl.getDataFolder(), "users.json"));
			UsersFile.save();
			User adminUser = new User("ADMIN", config.ADMIN_PASSWORD);
			adminUser.roles.add(adminGroup.id);
			UsersFile.add(adminUser);
			UsersFile.process();

			//DATABASE
			pluginConsole.sendMessage("Loading Cache File...");
			File cache = new File(main.pl.getDataFolder(), "cache.json");
			if(!cache.exists()) { cache.getParentFile().mkdirs(); cache.createNewFile(); FileUtils.write(cache, "{PERFORMANCE_DATA: []}", StandardCharsets.UTF_8);}
			cacheFile = new jsonDatabase(cache);
			cacheFile.read("{\"PERFORMANCE_DATA\":[]}");
			pluginConsole.sendMessage("&aCache File loaded!");

			//JETTY SERVER
			jetty = new JettyServer(config.PORT);
			jetty.init();

			sockMan = new SocketIoManager();
			sockMan.init();

			//NGROK
			if(config.NGROK_ENABLED) {
				ngrok = new NgrokManager(jetty.port);
				ngrok.ngrokClient.setAuthToken(config.NGROK_AUTH);
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
			Objects.requireNonNull(Bukkit.getPluginCommand("dashurl")).setExecutor(new dashurl());

			//INTEGRATIONS
			skriptIntegration = new SkriptIntegration();

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
		config.save();
		UsersFile.save();
		cacheFile.save();
		jetty.destroy();
		taskManager.stopTasks();
	}
}
