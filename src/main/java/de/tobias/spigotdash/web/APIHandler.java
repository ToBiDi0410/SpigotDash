package de.tobias.spigotdash.web;

import java.io.File;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import de.tobias.spigotdash.main;
import de.tobias.spigotdash.utils.configuration;
import de.tobias.spigotdash.utils.notificationManager;
import de.tobias.spigotdash.utils.pluginConsole;
import de.tobias.spigotdash.utils.pluginInstaller;
import de.tobias.spigotdash.utils.pluginManager;
import io.netty.util.internal.ThreadLocalRandom;
import net.md_5.bungee.api.ChatColor;

public class APIHandler {
	
	public static File deleteTemp = new File(main.pl.getDataFolder().getParentFile(), ".deleted");

	public static void handle(HttpExchange he, JsonObject json) {
		if (json.has("method")) {
			String method = json.get("method").getAsString();
			
			//PAGE
			if(method.equalsIgnoreCase("GET_OVERVIEW")) {
				MainRequestHandler.sendJSONResponse(he, 200, pageDataFetcher.GET_PAGE_OVERVIEW());
				return;
			}
			
			if(method.equalsIgnoreCase("GET_GRAPHS")) {
				MainRequestHandler.sendJSONResponse(he, 200, pageDataFetcher.GET_PAGE_GRAPHS());
				return;
			}
			
			if(method.equalsIgnoreCase("GET_WORLDS")) {
				Bukkit.getScheduler().runTask(main.pl, new Runnable() {
					@Override
					public void run() {
						MainRequestHandler.sendJSONResponse(he, 200, pageDataFetcher.GET_PAGE_WORLDS());
						return;
					}
				});
				return;
			}

			if (method.equalsIgnoreCase("GET_LOG")) {
				MainRequestHandler.sendJSONResponse(he, 200, dataFetcher.getLog(200));
				return;
			}
			
			if(method.equalsIgnoreCase("GET_PLAYERS")) {
				MainRequestHandler.sendJSONResponse(he, 200, pageDataFetcher.GET_PAGE_PLAYERS());
				return;
			}		
			
			if (method.equalsIgnoreCase("GET_PLUGINS")) {
				MainRequestHandler.sendJSONResponse(he, 200, pageDataFetcher.GET_PAGE_PLUGINS());
				return;
			}
			
			if(method.equalsIgnoreCase("GET_CONTROLS")) {
				MainRequestHandler.sendJSONResponse(he, 200, pageDataFetcher.GET_PAGE_CONTROLS());
				return;
			}
			
			//DATA
			if(method.equalsIgnoreCase("THEME"))  {
				MainRequestHandler.sendJSONResponse(he, 200, configuration.yaml_cfg.getBoolean("darkMode") ? "dark" : "light");
				return;
			}
			
			if(method.equalsIgnoreCase("GET_NOTIFICATIONS")) {
				MainRequestHandler.sendJSONResponse(he, 200, notificationManager.notifications);
				return;
			}
			
			if(method.equalsIgnoreCase("GET_WORLD")) {
				if (json.has("world")) {
					if(Bukkit.getWorld(json.get("world").getAsString()) == null) {
						MainRequestHandler.sendJSONResponse(he, 400, "ERR_NOTFOUND_WORLD");
						return;
					}
					
					Bukkit.getScheduler().runTask(main.pl, new Runnable() {
						@Override
						public void run() {
							MainRequestHandler.sendJSONResponse(he, 200, dataFetcher.getWorldForWeb(Bukkit.getWorld(json.get("world").getAsString())));
							return;
						}
					});
					
					return;
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_WORLD");
					return;
				}
			}

			//EXECUTION
			if (method.equalsIgnoreCase("EXEC_COMMAND")) {
				if (json.has("command")) {
					try {
						pluginConsole.sendMessage("Executing: &6/" + json.get("command").getAsString());
						Bukkit.getScheduler().callSyncMethod(main.pl, () -> Bukkit
								.dispatchCommand(Bukkit.getConsoleSender(), json.get("command").getAsString())).get();
						MainRequestHandler.sendJSONResponse(he, 200, "EXECUTED");
						return;
					} catch (Exception ex) {
						MainRequestHandler.sendJSONResponse(he, 500, "ERR_EXEC_FAILED");
						return;
					}
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_COMMAND");
					return;
				}
			}

			
			if (method.equalsIgnoreCase("GET_FILES_IN_PATH")) {
				if (json.has("path")) {
					String path = json.get("path").getAsString();
					MainRequestHandler.sendJSONResponse(he, 200, dataFetcher.getFilesInPath(path));
					return;
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_PATH");
					return;
				}
			}

			if (method.equalsIgnoreCase("GET_FILE_WITH_PATH")) {
				if (json.has("path")) {
					String path = json.get("path").getAsString();
					File f = dataFetcher.getFileWithPath(path);
					if (f.exists()) {
						if (f.isFile()) {
							MainRequestHandler.sendFileResponse(he, f, 200);
							return;
						} else {
							MainRequestHandler.sendJSONResponse(he, 400, "ERR_FILE_IS_DIR");
							return;
						}
					} else {
						MainRequestHandler.sendJSONResponse(he, 404, "ERR_FILE_NOT_FOUND");
						return;
					}
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_PATH");
					return;
				}
			}

			if (method.equalsIgnoreCase("TOGGLE_PLUGIN")) {
				if (json.has("plugin")) {
					Plugin pl = pluginManager.getPlugin(json.get("plugin").getAsString());
					if (pl.isEnabled()) {
						Bukkit.getScheduler().runTask(main.pl, new Runnable() {
							public void run() {
								boolean suc = pluginManager.disablePlugin(pl);
								MainRequestHandler.sendJSONResponse(he, suc ? 200 : 500, suc ? "SUCCESS" : "ERROR");
								return;
							}
						});
					} else {
						Bukkit.getScheduler().runTask(main.pl, new Runnable() {
							public void run() {
								boolean suc = pluginManager.load(json.get("plugin").getAsString());
								MainRequestHandler.sendJSONResponse(he, suc ? 200 : 500, suc ? "SUCCESS" : "ERROR");
								return;
							}
						});
					}
					return;
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_PLUGIN");
					return;
				}
			}
			
			if(method.equalsIgnoreCase("CONTROL")) {
				if(json.has("action")) {
					String action = json.get("action").getAsString();
					if(action.equalsIgnoreCase("STOP")) {
						Bukkit.getScheduler().runTask(main.pl, new Runnable() {
							@Override
							public void run() {
								Bukkit.shutdown();
							}
						});
						MainRequestHandler.sendJSONResponse(he, 200, "SUCCESS");
						return;
					}
					
					if(action.equalsIgnoreCase("RELOAD")) {
						Bukkit.getScheduler().runTask(main.pl, new Runnable() {
							@Override
							public void run() {
								Bukkit.reload();
							}
						});
						MainRequestHandler.sendJSONResponse(he, 200, "SUCCESS");
						return;
					}
					
					if(action.equalsIgnoreCase("TOGGLE_NETHER")) {
						boolean current = Boolean.parseBoolean(dataFetcher.getServerPropertie("allow-nether"));
						dataFetcher.setServerPropertie("allow-nether", String.valueOf(!current));
						boolean suc = true;
						notificationManager.setNeedReload(true);
						MainRequestHandler.sendJSONResponse(he, suc ? 200 : 500, suc ? "SUCCESS" : "ERROR");
						return;
					}
					
					if(action.equalsIgnoreCase("TOGGLE_WHITELIST")) {
						boolean current = Bukkit.hasWhitelist();
						Bukkit.getScheduler().runTask(main.pl, new Runnable() {
							@Override
							public void run() {
								Bukkit.setWhitelist(!current);
								dataFetcher.setServerPropertie("white-list", String.valueOf(!current));
								boolean suc = true;
								Bukkit.reloadWhitelist();
								MainRequestHandler.sendJSONResponse(he, suc ? 200 : 500, suc ? "SUCCESS" : "ERROR");
								return;
							}
							
						});
						return;
					}
					
					if(action.equalsIgnoreCase("WHITELIST_ADD")) {
						if (json.has("player")) {
							String uuid = json.get("player").getAsString();
							Bukkit.getOfflinePlayer(uuid).setWhitelisted(true);
							MainRequestHandler.sendJSONResponse(he, 200, "SUCCESS");
							return;
						} else {
							MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_PLAYER");
							return;
						}
					}
					
					if(action.equalsIgnoreCase("WHITELIST_REMOVE")) {
						if (json.has("player")) {
							String uuid = json.get("player").getAsString();
							UUID uuidObj = dataFetcher.uuidFromUUIDWithoutDashes(uuid.replaceAll("-", ""));
							Bukkit.getOfflinePlayer(uuidObj).setWhitelisted(false);
							MainRequestHandler.sendJSONResponse(he, 200, "SUCCESS");
							return;
						} else {
							MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_PLAYER");
							return;
						}
					}
					
					if(action.equalsIgnoreCase("OPERATOR_ADD")) {
						if (json.has("player")) {
							String uuid = json.get("player").getAsString();
							Bukkit.getOfflinePlayer(uuid).setOp(true);
							MainRequestHandler.sendJSONResponse(he, 200, "SUCCESS");
							return;
						} else {
							MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_PLAYER");
							return;
						}
					}
					
					if(action.equalsIgnoreCase("OPERATOR_REMOVE")) {
						if (json.has("player")) {
							String uuid = json.get("player").getAsString();
							UUID uuidObj = dataFetcher.uuidFromUUIDWithoutDashes(uuid.replaceAll("-", ""));
							Bukkit.getOfflinePlayer(uuidObj).setOp(false);
							MainRequestHandler.sendJSONResponse(he, 200, "SUCCESS");
							return;
						} else {
							MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_PLAYER");
							return;
						}
					}
					
					if(action.equalsIgnoreCase("TOGGLE_END")) {
						boolean current = (Bukkit.getWorld("world_the_end") != null);
						boolean suc = dataFetcher.modifyBukkitPropertie("settings.allow-end", !current);
						notificationManager.setNeedReload(true);
						MainRequestHandler.sendJSONResponse(he, suc ? 200 : 500, suc ? "SUCCESS" : "ERROR");
						return;
					}
					
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_ACTION");
					return;
				}
			}
			
			if(method.equalsIgnoreCase("CONTROL_WORLD")) {
				if(json.has("world")) {
					if(json.has("action")) {
						String action = json.get("action").getAsString();
						World w = Bukkit.getWorld(json.get("world").getAsString());
						if(w != null) {
							
							if(action.equalsIgnoreCase("WEATHER")) {
								if(json.has("weather")) {
									String weather = json.get("weather").getAsString();
									final boolean thundering = weather.equalsIgnoreCase("Thunder") ? true : false;
									final boolean storming = weather.equalsIgnoreCase("Rain") || weather.equalsIgnoreCase("Thunder") ? true : false;
									
									Bukkit.getScheduler().runTask(main.pl, new Runnable() {
										@Override
										public void run() {
											w.setStorm(storming);
											w.setThundering(thundering);
											
											if(thundering) {
												w.setThunderDuration(ThreadLocalRandom.current().nextInt(20*60*3, 20*60*13 + 1));
											}
											
											if(storming) {
												w.setThunderDuration((int) ThreadLocalRandom.current().nextDouble(24000*0.5, 24000*0.75 + 1));
											}
											MainRequestHandler.sendJSONResponse(he, 200, "SUCCESS");
											return;											
										}
										
									});
									return;
								} else {
									MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_WEATHER");
									return;
								}
							}
							
							if(action.equalsIgnoreCase("TIME")) {
								if(json.has("time")) {
									final Long time = json.get("time").getAsLong();
									
									Bukkit.getScheduler().runTask(main.pl, new Runnable() {
										@Override
										public void run() {
											w.setTime(time);
											MainRequestHandler.sendJSONResponse(he, 200, "SUCCESS");
											return;		
										}
									});
									
									return;
								} else {
									MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_TIME");
									return;
								}
							}
							
							if(action.equalsIgnoreCase("KILL_ENTITY_TYPE")) {
								if(json.has("type")) {
									EntityType entType = EntityType.valueOf(json.get("type").getAsString());
									
									if (entType != null) {
										Bukkit.getScheduler().runTask(main.pl, new Runnable() {
											@Override
											public void run() {
												for (Entity e : w.getEntities()) {
													if (e.getType() == entType)
														e.remove();
												}
												MainRequestHandler.sendJSONResponse(he, 200, "KILLED");
												return;
											}
										});
										
										return;
									} else {
										MainRequestHandler.sendJSONResponse(he, 400, "ERR_INVALID_ENTITYTYPE");
										return;
									}
								} else {
									MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_TYPE");
									return;
								}
							}
							
						} else {
							MainRequestHandler.sendJSONResponse(he, 400, "ERR_NOTFOUND_WORLD");
							return;
						}
					} else {
						MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_ACTION");
						return;
					}
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_WORLD");
					return;
				}
			}
			
			if (method.equalsIgnoreCase("PLAYER_ACTION")) {
				if (json.has("player")) {
					String uuid = json.get("player").getAsString();
					Player p = Bukkit.getPlayer(UUID.fromString(uuid));
					System.out.println(uuid);
					
					if (p != null && p.isOnline()) {
						if (json.has("action")) {
							String action = json.get("action").getAsString();
							
							if (action.equalsIgnoreCase("MESSAGE")) {
								if (json.has("message")) {
									String message = json.get("message").getAsString();
									message = "&cServer &a--> &6You: &7" + message;
							
									message = ChatColor.translateAlternateColorCodes('&', message);
									p.sendMessage(message);
									
									MainRequestHandler.sendJSONResponse(he, 200, "SUCCESS");
									return;
								} else {
									MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_MESSAGE");
									return;
								}
							}
							
							if (action.equalsIgnoreCase("KICK")) {
								if (json.has("message")) {
									String message = json.get("message").getAsString();
									message = "&cKicked from the Server:\n&b" + message;
							
									message = ChatColor.translateAlternateColorCodes('&', message);
									
									final String fmessage = message;
									
									Bukkit.getScheduler().runTask(main.pl, new Runnable() {

										@Override
										public void run() {
											p.kickPlayer(fmessage);
											MainRequestHandler.sendJSONResponse(he, 200, "SUCCESS");
											return;
										}
										
									});
									return;
								} else {
									MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_MESSAGE");
									return;
								}
							}
						} else {
							MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_ACTION");
							return;
						}
					} else {
						MainRequestHandler.sendJSONResponse(he, 400, "ERR_PLAYER_NOT_FOUND");
						return;
					}
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_PLAYER");
					return;
				}
			}
			
			if (method.equalsIgnoreCase("INSTALL_PLUGIN")) {
				if (json.has("id")) {
					String install_state = pluginInstaller.installPlugin(json.get("id").getAsString());
					int code = install_state.equalsIgnoreCase("INSTALLED") ? 200 : 500;
					//if(code == 200) notificationManager.setNeedReload(true); NOT NEEDED ANYMORE: LOADED AUTOMATICALLY
					MainRequestHandler.sendJSONResponse(he, code, install_state);
					return;
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_NOTIFICATION_UUID");
					return;
				}
			}

			/*if (method.equalsIgnoreCase("DELETE_PLUGIN")) {
				if (json.has("plugin")) {
					Plugin pl = Bukkit.getPluginManager().getPlugin(json.get("plugin").getAsString());
					if (pl != null) {
						File plfile = new java.io.File(
								pl.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
						File folder = pl.getDataFolder();
						Bukkit.getScheduler().runTask(main.pl, new Runnable() {
							public void run() {
								if (pl.isEnabled())
									Bukkit.getPluginManager().disablePlugin(pl);

								if (!plfile.delete() || (folder.exists() && !folder.delete())) {
									MainRequestHandler.sendJSONResponse(he, 500, "ERR_DEL_FAILED");
									return;
								}

								MainRequestHandler.sendJSONResponse(he, 200, "SUCCESS");
								return;
							}
						});
						return;
					} else {
						MainRequestHandler.sendJSONResponse(he, 400, "ERR_NOT_FOUND");
						return;
					}
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_PLUGIN");
					return;
				}
			}

			if (method.equalsIgnoreCase("GET_PLUGIN_FILES")) {
				MainRequestHandler.sendJSONResponse(he, 200, dataFetcher.getPluginFileNames());
				return;
			}*/
			
			if (method.equalsIgnoreCase("NOTIFICATION_CLOSED")) {
				if (json.has("uuid")) {
					notificationManager.closeNotification(json.get("uuid").getAsString());
					MainRequestHandler.sendJSONResponse(he, 200, "REMOVED");
					return;
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_NOTIFICATION_UUID");
					return;
				}
			}

			MainRequestHandler.sendJSONResponse(he, 500, "ERR_NOT_HANDLED");

		} else {
			MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_METHOD");
		}
	}
}
