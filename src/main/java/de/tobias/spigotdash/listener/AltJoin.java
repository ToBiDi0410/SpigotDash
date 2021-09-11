package de.tobias.spigotdash.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import de.tobias.spigotdash.utils.AltDetector;

public class AltJoin implements Listener {

	@EventHandler
	public static void onJoin(PlayerJoinEvent e) {
		AltDetector.registerPlayer(e.getPlayer());
	}
}
