package de.tobias.spigotdash.listener;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.tobias.spigotdash.utils.configuration;

public class JoinTime implements Listener {

	public static HashMap<String, Long> joinTimes = new HashMap<>();  
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(joinTimes.containsKey(e.getPlayer().getUniqueId().toString())) {
			joinTimes.replace(e.getPlayer().getUniqueId().toString(), System.currentTimeMillis());
		} else {
			joinTimes.put(e.getPlayer().getUniqueId().toString(), System.currentTimeMillis());
		}
		
		if(Bukkit.getOnlinePlayers().size() > configuration.yaml_cfg.getInt("PLAYER_RECORD")) {
			configuration.yaml_cfg.set("PLAYER_RECORD", Bukkit.getOnlinePlayers().size());
		}
	}
	
	public static void enableSet() {
		for(Player p : Bukkit.getOnlinePlayers()) {
			if(!joinTimes.containsKey(p.getUniqueId().toString())) {
				joinTimes.put(p.getUniqueId().toString(), System.currentTimeMillis());
			}
		}
	}
}
