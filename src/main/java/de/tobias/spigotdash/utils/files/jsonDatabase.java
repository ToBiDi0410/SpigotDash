package de.tobias.spigotdash.utils.files;

import java.io.File;
import java.nio.charset.StandardCharsets;

import com.google.gson.GsonBuilder;
import de.tobias.spigotdash.utils.pluginConsole;
import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class jsonDatabase {
	
	public File file;
	public Gson gson;
	public JsonObject jsonTree;
	public JsonParser jpar;
	
	public jsonDatabase(File f) {
		this.file = f;
		this.jpar = new JsonParser();
		this.gson = new GsonBuilder().setPrettyPrinting().create();
	}
	
	public boolean read(String fallbackJSON) {
		if(file.exists() && file.canRead()) {
			try {
				String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				this.jsonTree = jpar.parse(content).getAsJsonObject();
				return true;
			} catch(Exception ex) {
				this.jsonTree = jpar.parse(fallbackJSON).getAsJsonObject();
				ex.printStackTrace();
				pluginConsole.sendMessage("&cFailed to read JSON File '" + this.file.getName() + "'! Using provided Fallback!");
			}

		}
		return false;
	}
	
	public boolean save() {
		if(file.exists() && file.canWrite()) {
			try {
				if(jsonTree != null) {
					String json = gson.toJson(jsonTree);
					FileUtils.write(file, json, StandardCharsets.UTF_8);
					return true;
				}
			} catch(Exception ex) { ex.printStackTrace(); }
		}
		return false;
	}
	

}
