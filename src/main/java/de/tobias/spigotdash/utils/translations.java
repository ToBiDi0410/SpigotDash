package de.tobias.spigotdash.utils;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.bukkit.configuration.file.YamlConfiguration;

import de.tobias.spigotdash.main;

public class translations {

	public static Map<String, Object> currentTranslations = new HashMap<String, Object>();
	public static String translationBrackets = "%T%";
	public static YamlConfiguration yaml_cfg = null;
	public static File loadedFile = null;
	
	public static String replaceTranslationsInString(String input) {
		for(Entry<String, Object> translation : currentTranslations.entrySet()) {
			input = input.replaceAll(translationBrackets + translation.getKey().toUpperCase() + translationBrackets, translation.getValue().toString());
		}
		return input;
	}
	
	public static boolean load() {
		pluginConsole.sendMessage("Loading Translations...");
		loadDefaultTranslations();
		loadInternalTranslation(configuration.yaml_cfg.getString("translations"));
		
		if(loadedFile != null) {
			yaml_cfg.addDefaults(currentTranslations);
			yaml_cfg.options().copyDefaults(true);
			try {
				yaml_cfg.save(loadedFile);
			} catch(Exception ex) {
				pluginConsole.sendMessage("&6WARN: Cannot save Translations File with Defaults!");
			}
		}
		
		for(String key : currentTranslations.keySet()) {
			if(yaml_cfg.contains(key)) {
				currentTranslations.replace(key, yaml_cfg.getString(key));
			} else {
				pluginConsole.sendMessage("&6WARN: The Translations loaded are missing: &b" + key);
			}
		}

		pluginConsole.sendMessage("&aTranslations loaded!");
		return true;
	}
	
	public static void loadInternalTranslation(String name) {
		try {
			String resname = "/translations/" + name.toUpperCase() + ".yml";
			InputStream stream = main.pl.getClass().getResourceAsStream(resname);
			
			if(stream == null) {
				File f = new File(main.pl.getDataFolder(), "translations.yml");
				
				if(f.exists()) {
					pluginConsole.sendMessage("&6WARN: The Translations for &b'" + name + "' &6could not be found!");
					pluginConsole.sendMessage("&7Loading Translations from &6translations.yml&7...");
					stream = FileUtils.openInputStream(f);
					loadedFile = f;
				} else {
					pluginConsole.sendMessage("&cThe Translations for &6'" + name + "' &ccould not be found, using &6'EN'&c!");
					stream = main.pl.getClass().getResourceAsStream("/translations/EN.yml");
					loadedFile = null;
				}
			} else {
				pluginConsole.sendMessage("&7Loading Internal Translations &6'" + name + "'&7...");
				loadedFile = null;
			}
			
			Reader r = new InputStreamReader(stream, StandardCharsets.UTF_8);
			yaml_cfg = YamlConfiguration.loadConfiguration(r);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void loadDefaultTranslations() {
		InputStream stream = main.pl.getClass().getResourceAsStream("/translations/EN.yml");
		YamlConfiguration temp = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
		
		for(String key : temp.getKeys(true)) {
			currentTranslations.put(key, temp.getString(key));
		}
	}
}
