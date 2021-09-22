package de.tobias.spigotdash.utils.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import de.tobias.spigotdash.utils.files.configuration;
import de.tobias.spigotdash.utils.errorCatcher;
import de.tobias.spigotdash.utils.pluginConsole;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.tobias.spigotdash.main;
import de.tobias.spigotdash.web.dataprocessing.dataFetcher;

public class pluginInstaller {
	
	public static String API_URL = "https://api.spiget.org/v2/";
	public static String LOCAL_PREFIX = "&7[&6PluginInstaller&7] &7";

	public static String installPlugin(String id) {
		pluginConsole.sendMessage(LOCAL_PREFIX + "&7Installing new Plugin with ID '&5" + id + "&7'...");
		try {
			//REQUESTING DETAILS
			JsonObject details = getDetailsById(id);
			if(details == null) {
				pluginConsole.sendMessage(LOCAL_PREFIX + "- &cFailed: Plugin not found on SpigetAPI (Offline?)");
				return "FAILED_RESSOURCE_NOT_FOUND";
			}
			
			//REQUEST CREATION AND EXECUTION
			URL download = new URL(API_URL + "resources/" + id + "/download");
			pluginConsole.sendMessage(LOCAL_PREFIX + "- &7Downloading Plugin from '" + download.toString() + "'...");
			HttpURLConnection con = (HttpURLConnection) download.openConnection();
			con.setInstanceFollowRedirects(true);
			con.setRequestMethod("GET");
			int status = con.getResponseCode();
			
			//VERIFY OF RESPONSE (CHECK WHETHER IT WORKED OR NOT)
			pluginConsole.sendMessage(LOCAL_PREFIX + "- &7Checking Response... (Code: " + status + ")");
			
			if(status != 200) {
				pluginConsole.sendMessage(LOCAL_PREFIX + "- &cFailed: SpigetAPI does not provide the required File");
				con.disconnect();
				return "FAILED_RESSOURCE_NOT_FOUND";
			}
			
			//WRITING OF FILE
			File dest = new File(main.pl.getDataFolder().getParentFile(), id + ".SpigotDashDownload" + details.get("file").getAsJsonObject().get("type").getAsString());
			pluginConsole.sendMessage(LOCAL_PREFIX + "- &7Writing Response to File (" + dest.getAbsolutePath().toString() + ")...");
			writeBytesFromInputStreamIntoFile(con.getInputStream(), dest);
			pluginConsole.sendMessage(LOCAL_PREFIX + "- &7Finished! Loading Plugin...");
			pluginManager.load(FilenameUtils.removeExtension(dest.getName()));

			con.disconnect();
			return "INSTALLED";
		
		} catch (Exception ex) {
			pluginConsole.sendMessage(LOCAL_PREFIX + "- &cFailed: ");
			errorCatcher.catchException(ex, false);
			return "INSTALL_FAILED_ERR_THROWN";
		}
		
	}
	
	public static String updatePlugin(Plugin pl, String id) {
		pluginConsole.sendMessage(LOCAL_PREFIX + "&7Updating Plugin '&5" + pl.getName() + "&7'...");
		try {
			//REQUESTING DETAILS
			JsonObject details = getDetailsById(id);
			if(details == null) {
				pluginConsole.sendMessage(LOCAL_PREFIX + "- &cFailed: Plugin not found on SpigetAPI (Offline?)");
				return "FAILED_RESSOURCE_NOT_FOUND";
			}
			
			//REQUEST CREATION AND EXECUTION
			URL download = new URL(API_URL + "resources/" + id + "/download");
			pluginConsole.sendMessage(LOCAL_PREFIX + "- &7Downloading Update from '" + download.toString() + "'...");
			HttpURLConnection con = (HttpURLConnection) download.openConnection();
			con.setInstanceFollowRedirects(true);
			con.setRequestMethod("GET");
			int status = con.getResponseCode();
			
			//VERIFY OF RESPONSE (CHECK WHETHER IT WORKED OR NOT)
			pluginConsole.sendMessage(LOCAL_PREFIX + "- &7Checking Response... (Code: " + status + ")");
			
			if(status != 200) {
				pluginConsole.sendMessage(LOCAL_PREFIX + "- &cFailed: SpigetAPI does not provide the required File");
				con.disconnect();
				return "FAILED_RESSOURCE_NOT_FOUND";
			}
			
			
			//WRITING OF FILE
			File dest = dataFetcher.getPluginFile(pl);
			pluginConsole.sendMessage(LOCAL_PREFIX + "- &7Overwriting current Version with Update (" + dest.getAbsolutePath().toString() + ")...");
			writeBytesFromInputStreamIntoFile(con.getInputStream(), dest);
			if(configuration.yaml_cfg.getBoolean("autoReloadOnUpdate")) {
				pluginConsole.sendMessage(LOCAL_PREFIX + "- &7Finished! Reloading Server to enable Update...");
				Bukkit.reload();
			}
			
			con.disconnect();
			return "UPDATED";
		
		} catch (Exception ex) {
			pluginConsole.sendMessage(LOCAL_PREFIX + "- &cFailed: ");
			errorCatcher.catchException(ex, false);
			return "UPDATE_FAILED_ERR_THROWN";
		}		
	}
	
	public static JsonObject getDetailsById(String id) {
		try {
			URL download = new URL(API_URL + "resources/" + id + "/");
			HttpURLConnection con = (HttpURLConnection) download.openConnection();
			con.setInstanceFollowRedirects(true);
			con.setRequestMethod("GET");
			int status = con.getResponseCode();
			
			if(status != 200) return null;
			
			String inline = "";
			Scanner sc = new Scanner(download.openStream());
			while(sc.hasNext())	{
				inline+=sc.nextLine();
			}
			sc.close();
			
			JsonParser parser = new JsonParser();
			JsonObject jsonTree = parser.parse(inline).getAsJsonObject();
			
			con.disconnect();
			return jsonTree;
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static boolean writeBytesFromInputStreamIntoFile(InputStream in, File f) {
		try {
		    OutputStream outStream = new FileOutputStream(f);
		    byte[] buffer = new byte[8 * 1024];
		    int bytesRead;
		    while ((bytesRead = in.read(buffer)) != -1) {
		        outStream.write(buffer, 0, bytesRead);
		    }
		    IOUtils.closeQuietly(in);
		    IOUtils.closeQuietly(outStream);
		    return true;
		} catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}

	}

}
