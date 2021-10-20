package de.tobias.spigotdash.web.dataprocessing;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import de.tobias.spigotdash.listener.JoinTime;
import de.tobias.spigotdash.main;
import de.tobias.spigotdash.utils.AltDetector;
import de.tobias.spigotdash.utils.errorCatcher;
import de.tobias.spigotdash.utils.files.translations;
import de.tobias.spigotdash.utils.pluginConsole;
import de.tobias.spigotdash.utils.plugins.pluginManager;
import org.apache.commons.io.FileSystem;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

public class dataFetcher {

	public static File serverDir = Bukkit.getWorldContainer();
	public static File serverPropFile = new File(serverDir, "server.properties");
	public static File bukkitPropFile = new File(serverDir, "bukkit.yml");

	public static Runtime runtime = Runtime.getRuntime();
	public static MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

	public static long last_tick_time = 0;
	public static float tps = 0;
	public static float tps_avg = 0;
	public static float tps_avg_gen = 0;
	public static float tps_passed = 0;
	
	// ** CONTROLS **

	public static boolean modifyBukkitPropertie(String key, Object val) {
		try {
			YamlConfiguration yamlProps = YamlConfiguration.loadConfiguration(bukkitPropFile);		
			yamlProps.set(key, val);
			yamlProps.save(bukkitPropFile);
			return true;
		} catch (IOException ex) {
			pluginConsole.sendMessage("&cFailed to Modify Server Properties: ");
			errorCatcher.catchException(ex, false);
			return false;
		}
	}
	
	public static Object getBukkitPropertie(String key) {
		YamlConfiguration yamlProps = YamlConfiguration.loadConfiguration(bukkitPropFile);
		return yamlProps.get(key);
	}
	
	public static String getServerPropertie(String s) {
		Properties pr = new Properties();
		try {
			FileInputStream in = new FileInputStream(serverPropFile);
			pr.load(in);
			return pr.getProperty(s);
		} catch (IOException ignored) {}
		return "";
	}
	
	public static void setServerPropertie(String s, String value) {
		Properties pr = new Properties();
		try {
			FileInputStream in = new FileInputStream(serverPropFile);
			pr.load(in);
			pr.setProperty(s, value);
			pr.store(new FileOutputStream(serverPropFile), null);
		} catch (IOException ignored) {}
	}
	
	// ** PLUGINS **
	public static ArrayList<HashMap<String, Object>> getPluginsForWeb() {
		ArrayList<HashMap<String, Object>> plugins = new ArrayList<>();
		
		for(Plugin pl : pluginManager.getAllPluginsWithDisabled()) {
			HashMap<String, Object> plugin_info = new HashMap<>();
			plugin_info.put("enabled", pl.isEnabled());
			plugin_info.put("name", pl.getName());
			plugin_info.put("description", pl.getDescription().getDescription());
			plugin_info.put("version", pl.getDescription().getVersion());
			plugin_info.put("authors", pl.getDescription().getAuthors());
			plugin_info.put("website", pl.getDescription().getWebsite());	
			plugin_info.put("apiversion", pl.getDescription().getAPIVersion());
			plugin_info.put("known", false);
			plugin_info.put("file", getPluginFile(pl).getName());
			
			if(pl == main.pl) {
				plugin_info.replace("file", "93710.SpigotDashDownload");
			}
			
			plugins.add(plugin_info);
		}
		return plugins;
	}
	
	public static File getPluginFile(Plugin pl) {
		try {
			return new File(pl.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		} catch (URISyntaxException e) {
			return null;
		}
	}
	
	
	public static ArrayList<String> getPluginFileNames() {
		ArrayList<String> files = new ArrayList<>();

		for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
			files.add(getPluginFile(pl).getName());
		}

		return files;	
	}
	
	// ** FILES **
	public static ArrayList<HashMap<String, Object>> getFilesInPath(String path) {
		File dir = new File(main.pl.getDataFolder().getParentFile().getParent(), path);
		ArrayList<HashMap<String, Object>> files = new ArrayList<>();
		 
		if(dir.exists()) {
			if(!dir.isFile()) {
				File[] files_obj = dir.listFiles();
				for(File f : files_obj) {
					files.add(fileToWebHashMap(f));
				}
				return files;
			} else {
				return null;
			}
		} else {
			return null;
		}
		
	}
	
	public static HashMap<String, Object> fileToWebHashMap(File file) {
		HashMap<String, Object> details = new HashMap<>();
		
		details.put("DIR", file.isDirectory());
		details.put("EXISTS", file.exists());
		details.put("NAME", file.getName());
		details.put("LAST_CHANGED", file.lastModified());
		
		return details;
	}
	
	public static File getFileWithPath(String path) {
		return new File(main.pl.getDataFolder().getParentFile().getParent(), path);
	}
	
	// ** TPS MEASUREMENT

	public static float getTPS() {
		return tps_avg;
	}

	public static Runnable getTPSRunnable() {
		return () -> {
			long diff = System.currentTimeMillis() - last_tick_time;
			float seconds = (diff / (1000.00f));

			tps = 20 / seconds;
			tps_avg_gen += tps;
			tps_passed += 1;

			if (tps_passed >= 5) {
				tps_avg = tps_avg_gen / tps_passed;
				tps_avg_gen = 0;
				tps_passed = 0;
			}
			// Bukkit.getConsoleSender().sendMessage("TPS: " + tps);
			// Bukkit.getConsoleSender().sendMessage("AVG TPS: " + tps_avg);

			last_tick_time = System.currentTimeMillis();
		};
	}

	// ** PLAYER **
	public static ArrayList<HashMap<String, Object>> getPlayersForWeb() {
		ArrayList<HashMap<String, Object>> players = new ArrayList<>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			players.add(getPlayerForWeb(p));
		}
		return players;
	}
	
	public static HashMap<String, Object> getPlayerForWeb(Player p) {
		HashMap<String, Object> playerinfo = new HashMap<>();
		playerinfo.put("UUID", p.getUniqueId());
		playerinfo.put("Name", p.getName());
		playerinfo.put("Displayname", p.getDisplayName());
		playerinfo.put("Location", locationToHashMap(p.getLocation()));
		playerinfo.put("Health", p.getHealth());
		playerinfo.put("Health_Max", p.getHealthScale());
		playerinfo.put("Food", p.getFoodLevel());
		playerinfo.put("Gamemode", translations.replaceTranslationsInString("%T%GAMEMODE_" + p.getGameMode().name() + "%T%"));
		playerinfo.put("Jointime", JoinTime.joinTimes.get(p.getUniqueId().toString()));
		playerinfo.put("XPLevel", p.getLevel());
		playerinfo.put("XP", p.getTotalExperience());
		playerinfo.put("XPForNextLevel", getExpFromLevel(p.getLevel() + 1) - getExpFromLevel(p.getLevel()));
		playerinfo.put("XPMissingForNextLevel", (getExpFromLevel(p.getLevel() + 1) - p.getTotalExperience()));
		playerinfo.put("XPHasForNextLevel", p.getTotalExperience() - getExpFromLevel(p.getLevel()));
		playerinfo.put("ALTS", dataFetcher.getOfflinePlayersAsString(AltDetector.getAlts(p)));
		
		return playerinfo;
	}

	public static HashMap<String, Object> locationToHashMap(Location l) {
		HashMap<String, Object> loc = new HashMap<>();
		loc.put("X", l.getBlockX());
		loc.put("Y", l.getBlockY());
		loc.put("Z", l.getBlockZ());
		loc.put("PITCH", l.getPitch());
		loc.put("YAW", l.getYaw());
		loc.put("WORLD", l.getWorld().getName());
		return loc;
	}
	
	public static String getOfflinePlayersAsString(ArrayList<OfflinePlayer> players) {
		StringBuilder s = new StringBuilder();
		for(OfflinePlayer p : players) {
			if(!s.toString().equalsIgnoreCase("")) { s.append(", "); }
			s.append(p.getName());
		}
		return s.toString();
	}

	// ** LOG **
	public static List<String> getLog(Integer linecount) {
		File logfile = new File(Bukkit.getServer().getWorldContainer().getAbsolutePath() + "/logs/", "latest.log");
		try {
			List<String> lines = new ArrayList<>();
			int counter = 0;
			try (ReversedLinesFileReader reader = new ReversedLinesFileReader(logfile, Charset.defaultCharset())) {
				while (counter < linecount) {
					String line = reader.readLine();
					if(line == null) break;
					lines.add(line);
					counter++;
				}
			}
			lines = Lists.reverse(lines);	
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	// ** WARNINGS **
	public static boolean pluginsDisabled() {
		for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
			if (!pl.isEnabled()) {
				return true;
			}
		}
		return false;
	}

	public static boolean unusedJARFiles() {
		File pluginsFolder = main.pl.getDataFolder().getParentFile();
		File[] jarFiles = pluginsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
		return (jarFiles.length != Bukkit.getPluginManager().getPlugins().length);
	}
	
	// ** PERFORMANCE GENERAL **
	public static Object getPerformanceDataForWeb() {
		JsonArray entrys = main.cacheFile.jsonTree.get("PERFORMANCE_DATA").getAsJsonArray();
		clearWithTime(entrys, (1000 * 60 * 10));
		return entrys;

	}
	
	public static void clearWithTime(JsonArray a, Integer maxtime) {
		if(a != null) {
			int loop = a.size() - 1;
			ArrayList<JsonElement> remove = new ArrayList<>();
			while(loop >= 0) {
				JsonElement e = a.get(loop);
				long time = e.getAsJsonObject().get("DATETIME").getAsLong();
				if((System.currentTimeMillis() - time) > maxtime) {
					remove.add(e);
				}
				loop--;
			}
			
			remove.forEach(a::remove);
		}
	}

	// ** WORLD FUNCTIONS **
	public static Integer getPlayerCount() {
		return Bukkit.getOnlinePlayers().size();
	}
	
	public static Integer getPluginCount() {
		return Bukkit.getPluginManager().getPlugins().length;
	}
	
	public static long getOntime() {
		return System.currentTimeMillis() - main.latestStart;
	}

	public static Integer getTotalEntities() {
		int ent = 0;
		for (World w : Bukkit.getWorlds()) {
			ent += w.getEntities().size();
		}
		return ent;
	}

	public static Integer getTotalChunks() {
		int ch = 0;
		for (World w : Bukkit.getWorlds()) {
			ch += w.getLoadedChunks().length;
		}
		return ch;
	}

	public static Integer getWorldCount() {
		return Bukkit.getWorlds().size();
	}
	
	public static HashMap<String, Object> getWorldForWebBasic(World w) {
		HashMap<String, Object> values = new HashMap<>();
		values.put("playerCount", w.getPlayers().size());
		values.put("entitieCount", w.getEntities().size());
		values.put("chunkCount", w.getLoadedChunks().length);
		values.put("name", w.getName());
		values.put("type", w.getEnvironment().name());
		
		return values;
	}
	
	@SuppressWarnings("unchecked")
	public static HashMap<String, Object> getWorldForWeb(World w) {
		HashMap<String, Object> values = new HashMap<>();
				
		HashMap<Object, Integer> entityCountsWorld = new HashMap<>();
		ArrayList<HashMap<String, Object>> playersWorld = new ArrayList<>();
		
		//DATAPACKS
		File dataPackFolder = new File(w.getWorldFolder(), "datapacks");
		ArrayList<String> dataPacks = new ArrayList<>();
		
		if(dataPackFolder.exists() && dataPackFolder.isDirectory()) {
			for(File datapack : dataPackFolder.listFiles()) {
				dataPacks.add(datapack.getName());
			}
		}
		
		values.put("Datapacks", dataPacks);
		
		//CHUNKS
		ArrayList<HashMap<String, Object>> chunks = new ArrayList<>();
		
		for(Chunk chunk : w.getLoadedChunks()) {
			HashMap<String, Object> chunkValues = new HashMap<>();

			//ENTITES PER CHUNK
			HashMap<Object, Integer> entityCounts = new HashMap<>();
			for(Entity ent : chunk.getEntities()) {
				String type = ent.getType().name();
				if(entityCounts.containsKey(type)) {
					entityCounts.replace(type, entityCounts.get(type) + 1);
				} else {
					entityCounts.put(type, 1);
				}
				
				if(entityCountsWorld.containsKey(type)) {
					entityCountsWorld.replace(type, entityCountsWorld.get(type) + 1);
				} else {
					entityCountsWorld.put(type, 1);
				}
			}
			entityCounts = sortByValue(entityCounts);
			chunkValues.put("Entities", entityCounts);
			chunkValues.put("X", chunk.getX());
			chunkValues.put("Z", chunk.getZ());
			chunkValues.put("ID", chunk.getX() + " " + chunk.getZ());
			
			//PLAYERS PER CHUNK
			ArrayList<HashMap<String, Object>> players = new ArrayList<>();
			for(Entity ent : chunk.getEntities()) {
				if(ent.getType() == EntityType.PLAYER) {
					Player p = (Player) ent;
					players.add(getPlayerForWeb(p));
					playersWorld.add(getPlayerForWeb(p));
				}
			}
			chunkValues.put("Players", players);
			
			chunks.add(chunkValues);
		}

		Comparator<HashMap<String, Object>> valComp = (o1, o2) -> {
			Integer worth1 = ((HashMap<Object, Integer>) o1.get("Entities")).size() + ((ArrayList<HashMap<String, Object>>)o1.get("Players")).size();
			Integer worth2 = ((HashMap<Object, Integer>) o2.get("Entities")).size() + ((ArrayList<HashMap<String, Object>>)o2.get("Players")).size();
			return worth2.compareTo(worth1);
		};
		
		chunks.sort(valComp);
		
		values.put("Chunks", chunks);

		//ENTITES
		entityCountsWorld = sortByValue(entityCountsWorld);
		values.put("Entities", entityCountsWorld);
		
		//PLAYERS
		values.put("Players", playersWorld);
		
		//INFOS
		values.put("weather", w.isThundering() ? "Thunder" : (w.hasStorm() ? "Rain" : "Normal"));
		values.put("difficulty", w.getDifficulty().toString());
		values.put("seed", w.getSeed());
		values.put("time", w.getFullTime());
		values.put("name", w.getName());
		values.put("daytime", w.getTime());
		values.put("days", w.getFullTime()/24000);

		return values;
	}

	// ** CPU LOAD FUNCTIONS **
	public static Double getProcessCPULoad() {
		try {
			ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
			AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

			return Optional.ofNullable(list)
					.map(l -> l.isEmpty() ? null : l)
					.map(List::iterator)
					.map(Iterator::next)
					.map(Attribute.class::cast)
					.map(Attribute::getValue)
					.map(Double.class::cast)
					.orElse(null);
		} catch (Exception ex) {
			return null;
		}
	}

	public static Double getSystemCPULoad() {
		try {
			ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
			AttributeList list = mbs.getAttributes(name, new String[]{"SystemCpuLoad"});

			return Optional.ofNullable(list)
					.map(l -> l.isEmpty() ? null : l)
					.map(List::iterator)
					.map(Iterator::next)
					.map(Attribute.class::cast)
					.map(Attribute::getValue)
					.map(Double.class::cast)
					.orElse(null);
		} catch (Exception ex) {
			return null;
		}
	}

	// ** STORAGE FUNCTION ** //
	public static long getFreeSpace() {
		return new File("/").getFreeSpace();
	}

	public static long getTotalSpace() {
		return new File("/").getTotalSpace();
	}

	public static long getUsedSpace() {
		return getTotalSpace() - getFreeSpace();
	}

	// ** MEMORY FUNCTIONS ** //

	public static long getFreeMemory() {
		long usedMemory = runtime.freeMemory();
		return bytesToMB(usedMemory);
	}

	public static long getAllocatedMemory() {
		long alloctedMemory = runtime.totalMemory();
		return bytesToMB(alloctedMemory);
	}

	public static long getMaxMemory() {
		long maxMemory = runtime.maxMemory();
		return bytesToMB(maxMemory);
	}

	public static long getUsedMemory() {
		long allocated = getAllocatedMemory();
		long free = getFreeMemory();
		return allocated - free;
	}

	// ** GENERAL HELPERS **

	public static long bytesToMB(long bytes) {
		return bytes / 1048576;
	}
	
	public static HashMap<String, Object> resultSetToHashMap(ResultSet rs, ArrayList<String> columns) {
		HashMap<String, Object> hs = new HashMap<>();
		try {
			for (String s : columns) {
				hs.put(s, rs.getObject(s));
			}
		} catch (Exception ex) {
			return hs;
		}

		return hs;
	}

	public static ArrayList<String> getColumsFromResultSet(ResultSet rs) {
		ArrayList<String> columns = new ArrayList<>();
		try {
			ResultSetMetaData rsdm = rs.getMetaData();
			int columnCount = rsdm.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				String name = rsdm.getColumnName(i);
				columns.add(name);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return columns;
	}
	
	public static int coutNumberOfOccurences(char Char, String str) {
		int count = 0;
		 
		for (int i = 0; i < str.length(); i++) {
		    if (str.charAt(i) == Char) {
		        count++;
		    }
		}
		return count;
	}
	
	public static String replaceLast(String string, String toReplace, String replacement) {
	    int pos = string.lastIndexOf(toReplace);
	    if (pos > -1) {
	        return string.substring(0, pos)
	             + replacement
	             + string.substring(pos + toReplace.length());
	    } else {
	        return string;
	    }
	}

	public static UUID uuidFromUUIDWithoutDashes(String dashless) {
		return UUID.fromString(insertDashUUID(dashless));
	}
	
	public static String insertDashUUID(String uuid) {
		//FROM: https://bukkit.org/threads/java-adding-dashes-back-to-minecrafts-uuids.272746/
		StringBuilder sb = new StringBuilder(uuid);
		sb.insert(8, "-");
		 
		sb = new StringBuilder(sb.toString());
		sb.insert(13, "-");
		 
		sb = new StringBuilder(sb.toString());
		sb.insert(18, "-");
		 
		sb = new StringBuilder(sb.toString());
		sb.insert(23, "-");
		 
		return sb.toString();
	}
	
	public static HashMap<Object, Integer> sortByValue(HashMap<Object, Integer> hm) {
		//CREATE BASIC COMPARATOR
		Comparator<Entry<Object, Integer>> valComp = (o1, o2) -> o2.getValue().compareTo(o1.getValue());
		
		//GET ENTRIES AS LIST AND COMPARE
		List<Entry<Object, Integer>> entryList = new ArrayList<>(hm.entrySet());
		entryList.sort(valComp);
		
		//CREATE NEW HASHMAP AND SORT
		LinkedHashMap<Object, Integer> sortedByValue = new LinkedHashMap<>(entryList.size());
		for(Entry<Object, Integer> entry : entryList){
            sortedByValue.put(entry.getKey(), entry.getValue());
        }
		
		//RETURN THE SORTED HASHMAP
		return sortedByValue;
    }
	
	public static int getExpFromLevel(int level) {
		if (level > 30) {
			return (int) (4.5 * level * level - 162.5 * level + 2220);
		}
		if (level > 15) {
			return (int) (2.5 * level * level - 40.5 * level + 360);
		}
		return level * level + 6 * level;
	}

	public static double roundDouble(Double val, Integer places) {
		DecimalFormat df = new DecimalFormat("#." + "#".repeat(places));
		return Double.parseDouble(df.format(val).replace(",", "."));
	}
}
