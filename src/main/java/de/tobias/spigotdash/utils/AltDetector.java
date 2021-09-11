package de.tobias.spigotdash.utils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class AltDetector {

	public static HashMap<String, ArrayList<OfflinePlayer>> ips = new HashMap<>();
	
	public static ArrayList<OfflinePlayer> getAlts(Player p) {
		String IP = getAddress(p.getAddress());
		if(ips.containsKey(IP)) {
			ArrayList<OfflinePlayer> list = new ArrayList<>(ips.get(IP));
			list.remove((OfflinePlayer) p);
			return list;
		} else {
			return new ArrayList<>();
		}
	}
	
	public static void registerPlayer(Player p) {
		String IP = getAddress(p.getAddress());
		ArrayList<OfflinePlayer> array = new ArrayList<>();

		if(ips.containsKey(IP)) {
			array = ips.get(IP);
		}
		
		if(!array.contains((OfflinePlayer) p)) {
			array.add((OfflinePlayer) p);
		}
		ips.put(IP, array);
	}
	
	public static String getAddress(InetSocketAddress in) {
		return in.getHostString();
	}
}
