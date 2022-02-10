package de.tobias.spigotdash;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin {

	public static Plugin pl;
	
	public void onEnable() {
		try {
			pl = this;
			//CONTENT WILL FOLLOW HERE
		} catch(Exception ex) {

		}
	}

	public void onDisable() {
	}
}
