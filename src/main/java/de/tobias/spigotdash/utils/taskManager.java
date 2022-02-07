package de.tobias.spigotdash.utils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Objects;

import de.tobias.spigotdash.utils.files.translations;
import de.tobias.spigotdash.utils.plugins.updater;
import org.bukkit.Bukkit;

import de.tobias.spigotdash.main;
import de.tobias.spigotdash.web.dataprocessing.dataFetcher;

public class taskManager {

	public static int DATA_taskID = 0;
	public static int TPS_taskID = 0;
	public static int UPDATE_taskID = 0;

	public static long lastUpdate = 0;
	public static long lastClearUpdate = 0;
	public static long lastNgrokUpdate = 0;
	
	public static void startTasks() {
		DATA_taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(main.pl, () -> {
			if(lastUpdate + 1000*15 <= System.currentTimeMillis()) {
				DecimalFormat df = new DecimalFormat("#.##");

				HashMap<String, Object> currentPerformanceData = new HashMap<>();
				currentPerformanceData.put("DATETIME", System.currentTimeMillis());
				currentPerformanceData.put("CPU_LOAD_SYSTEM", dataFetcher.roundDouble(dataFetcher.getSystemCPULoad() * 100, 2));
				currentPerformanceData.put("CPU_LOAD_PROCESS", dataFetcher.roundDouble(dataFetcher.getProcessCPULoad() * 100, 2));
				currentPerformanceData.put("MEMORY_MAX", dataFetcher.getMaxMemory());
				currentPerformanceData.put("MEMORY_USED", dataFetcher.getUsedMemory());
				currentPerformanceData.put("MEMORY_FREE", dataFetcher.getFreeMemory());
				currentPerformanceData.put("MEMORY_ALLOCATED", dataFetcher.getAllocatedMemory());
				currentPerformanceData.put("TPS", dataFetcher.getTPS());
				currentPerformanceData.put("WORLD_CHUNKS", dataFetcher.getTotalChunks());
				currentPerformanceData.put("WORLD_ENTITIES", dataFetcher.getTotalEntities());
				currentPerformanceData.put("WORLD_PLAYERS", dataFetcher.getPlayerCount());
				currentPerformanceData.put("WORLD_COUNT", dataFetcher.getWorldCount());
				currentPerformanceData.put("TOTAL_SPACE", dataFetcher.getTotalSpace());
				currentPerformanceData.put("USED_SPACE", dataFetcher.getUsedSpace());
				main.cacheFile.jsonTree.get("PERFORMANCE_DATA").getAsJsonArray().add(main.cacheFile.gson.toJsonTree(currentPerformanceData).getAsJsonObject());
				main.cacheFile.save();

				//** NOTIFICATIONS **
				notificationManager.manageNotifications();
				if(dataFetcher.getTPS() < 17.0f) {
					notificationManager.addNotification("LOW_TPS_WARN", "WARNING", "SpigotDash", translations.replaceTranslationsInString("%T%NOTIFICATION_LOWTPS_TITLE%T%"), translations.replaceTranslationsInString("%T%NOTIFICATION_LOWTPS_CONTENT%T%"), 1);
				}

				if(dataFetcher.pluginsDisabled()) {
					notificationManager.addNotification("PLUGINS_DISABLED_WARN", "WARNING", "SpigotDash", translations.replaceTranslationsInString("%T%NOTIFICATION_DISABLEDPLUGINS_TITLE%T%"), translations.replaceTranslationsInString("%T%NOTIFICATION_DISABLEDPLUGINS_CONTENT%T%"), -1);
				}

				if(dataFetcher.unusedJARFiles()) {
					notificationManager.addNotification("PLUGINS_JARUNLOADED_WARN", "DANGER", "SpigotDash", translations.replaceTranslationsInString("%T%NOTIFICATION_UNLOADEDJARS_TITLE%T%"), translations.replaceTranslationsInString("%T%NOTIFICATION_UNLOADEDJARS_CONTENT%T%"), -1);
				}

				if(updater.update_available) {
					notificationManager.addNotification("UPDATE_AVAILABLE", "INFO", "SpigotDash", translations.replaceTranslationsInString("%T%NOTIFICATION_UPDATE_TITLE%T%"), translations.replaceTranslationsInString("%T%NOTIFICATION_UPDATE_CONTENT%T%"), -1);
				}

				if(dataFetcher.getFreeSpace() < 1000 * 1000 * 1000 * 2) {
					if(dataFetcher.getFreeSpace() < 1000 * 1000 * 1000 * 1) {
						notificationManager.addNotification("VERY_LOW_STORAGE_CAPACITY", "DANGER", "SpigotDash", translations.replaceTranslationsInString("%T%VERY_LOW_STORAGE_CAPACITY_TITLE%T%"), translations.replaceTranslationsInString("%T%VERY_LOW_STORAGE_CAPACITY_CONTENT%T%"), 30);
					} else {
						notificationManager.addNotification("LOW_STORAGE_CAPACITY", "WARNING", "SpigotDash", translations.replaceTranslationsInString("%T%LOW_STORAGE_CAPACITY_TITLE%T%"), translations.replaceTranslationsInString("%T%LOW_STORAGE_CAPACITY_CONTENT%T%"), 30);
					}
				}

				lastUpdate = System.currentTimeMillis();
			}

			if(lastClearUpdate + 1000*120 <= System.currentTimeMillis()) {
				dataFetcher.clearWithTime(main.cacheFile.jsonTree.get("PERFORMANCE_DATA").getAsJsonArray(), (1000 * 60 * 10));
				lastClearUpdate = System.currentTimeMillis();
			}

			if(lastNgrokUpdate + 1000*60*60*1.5 <= System.currentTimeMillis()) {
				if(main.ngrok != null && Objects.requireNonNull(main.config.NGROK_AUTH).replace(" ", "").equalsIgnoreCase("")) {
					main.ngrok.reopen();
				}
				lastNgrokUpdate = System.currentTimeMillis();
			}


		}, 20L, 10L);
		
	    TPS_taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(main.pl, dataFetcher.getTPSRunnable(), 20L, 20L);
	    
	    int updateTime = main.config.UPDATE_CHECK_TIMEOUT;
	    pluginConsole.sendMessage("&7Set Autoupdater time to: &6" + updateTime + " &7Minutes");
	    UPDATE_taskID = Bukkit.getServer().getScheduler().scheduleAsyncRepeatingTask(main.pl, updater.getUpdateRunnable(), 20L, 20L * 60 * updateTime);
	}
	
	public static void stopTasks() {
		stopTask(DATA_taskID);
		stopTask(TPS_taskID);
		stopTask(UPDATE_taskID);
	}
	
	public static void stopTask(int id) {
		if(id != 0) {
			Bukkit.getScheduler().cancelTask(id);
		}
	}
}
