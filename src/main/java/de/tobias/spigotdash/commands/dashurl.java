package de.tobias.spigotdash.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import de.tobias.spigotdash.main;
import net.md_5.bungee.api.ChatColor;

public class dashurl implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender cs, Command cmd, String arg2, String[] args) {
		if(cs.hasPermission("spigotdash.dashurl")) {
			if(main.ngrok != null && main.ngrok.httpTunnel != null) {
				cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&aI&8] &7SpigotDash is available under: &6" + main.ngrok.httpTunnel.getPublicUrl()));
			} else {
				cs.sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&c!&8] &cSpigotDash is not configured to use NGrok!"));
			}
		}
		return false;
	}

}
