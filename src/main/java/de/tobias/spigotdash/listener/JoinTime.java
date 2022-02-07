package de.tobias.spigotdash.listener;

import java.util.HashMap;

import de.tobias.spigotdash.main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinTime implements Listener {

	public static HashMap<String, Long> joinTimes = new HashMap<>();  
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if(joinTimes.containsKey(e.getPlayer().getUniqueId().toString())) {
			joinTimes.replace(e.getPlayer().getUniqueId().toString(), System.currentTimeMillis());
		} else {
			joinTimes.put(e.getPlayer().getUniqueId().toString(), System.currentTimeMillis());
		}
		
		if(Bukkit.getOnlinePlayers().size() > main.config.PLAYER_RECORD) {
			main.config.PLAYER_RECORD = Bukkit.getOnlinePlayers().size();
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
