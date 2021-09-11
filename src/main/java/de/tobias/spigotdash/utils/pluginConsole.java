package de.tobias.spigotdash.utils;

import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;

import net.md_5.bungee.api.ChatColor;

public class pluginConsole {

	public static String CONSOLE_PREFIX = "&7[&bSpigotDash&7] &7";
	public static ConsoleCommandSender cs = Bukkit.getConsoleSender();
	
	
	public static void sendMessage(String message) {
		message = CONSOLE_PREFIX + message;
		sendMessageWithoutPrefix(message);
	}
	
	public static void sendMessageWithoutPrefix(String message) {
		message = message.replace("§", "&");
		message = ChatColor.translateAlternateColorCodes('&', message);
		cs.sendMessage(message);
	}
}
