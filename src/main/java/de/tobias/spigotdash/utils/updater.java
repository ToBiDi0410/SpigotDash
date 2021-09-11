package de.tobias.spigotdash.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.tobias.spigotdash.main;

public class updater {

	public static String current_version = main.pl.getDescription().getVersion().split(" ")[0];
	public static boolean update_available = false;
	public static String LOCAL_PREFIX = "&7[&5Updater&7] &7";
	public static String THIS_SPIGOT_ID = "93710";

	public static void checkForUpdates() {
		try {
			pluginConsole.sendMessage(LOCAL_PREFIX + "&7Checking for Updates...");
			URL url = new URL("https://api.spiget.org/v2/resources/" + THIS_SPIGOT_ID + "/versions?size=1&sort=-id&fields=name");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setDoOutput(false);
			con.setConnectTimeout(5000);
			con.setReadTimeout(5000);

			int status = con.getResponseCode();
			if (status == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();
				con.disconnect();
				
				JsonParser parser = new JsonParser();
				JsonElement jsonTree = parser.parse(content.toString());
				JsonArray array = jsonTree.getAsJsonArray();
				JsonObject newest = array.get(0).getAsJsonObject();
				
				String newest_version = newest.get("name").getAsString().split(" ")[0];
				
				Version newestv = new Version(newest_version);
				Version current = new Version(current_version);
				
				Integer update = current.compareTo(newestv);
				
				if(update == -1) {
					update_available = true;
					pluginConsole.sendMessage(LOCAL_PREFIX + "&7New Update &aavailable&7 (&6" + current_version + " &7--> &b" + newest_version + "&7)! Please take a look at &6SpigotMC&7!");
					if(configuration.yaml_cfg.getBoolean("autoUpdate")) {
						pluginInstaller.updatePlugin(main.pl, THIS_SPIGOT_ID);
					}
				} else if(update == 0) {
					update_available = false;
					pluginConsole.sendMessage(LOCAL_PREFIX + "&aYou are running the newest Version!");
				} else if(update == 1) {
					update_available = false;
					pluginConsole.sendMessage(LOCAL_PREFIX + "&6You are running a Version before?");
					pluginConsole.sendMessage(LOCAL_PREFIX + "&bDon´t worry. This can happen if you download the Plugin shortly after the release or this is a Pre-Release Version.");
				}
			} else {
				pluginConsole.sendMessage(LOCAL_PREFIX + "&cCheck for Updates failed! You won't recieve notifications!");
				return;
			}

		} catch (Exception ex) {
			pluginConsole.sendMessage(LOCAL_PREFIX + "&cCheck for Updates failed! You won't recieve notifications!");
		}
	}
	
	public static Runnable getUpdateRunnable() {
		return new Runnable() {
			@Override
			public void run() {
				if(update_available != true) {
					checkForUpdates();
				}
			}

		};
	}

}
