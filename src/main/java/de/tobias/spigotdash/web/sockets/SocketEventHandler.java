package de.tobias.spigotdash.web.sockets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.tobias.spigotdash.main;
import de.tobias.spigotdash.utils.errorCatcher;
import de.tobias.spigotdash.utils.files.configuration;
import de.tobias.spigotdash.utils.files.translations;
import de.tobias.spigotdash.utils.notificationManager;
import de.tobias.spigotdash.utils.pluginConsole;
import de.tobias.spigotdash.utils.plugins.pluginInstaller;
import de.tobias.spigotdash.utils.plugins.pluginManager;
import de.tobias.spigotdash.web.dataprocessing.dataFetcher;
import de.tobias.spigotdash.web.dataprocessing.pageDataFetcher;
import de.tobias.spigotdash.web.dataprocessing.webBundler;
import de.tobias.spigotdash.web.jetty.FileUploadServlet;
import io.socket.socketio.server.SocketIoSocket;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.io.FileDeleteStrategy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class SocketEventHandler {

    public static void handleSocketEvent(SocketIoSocket soc, String eventName, Object[] args) {
        if(eventName.equalsIgnoreCase("AUTH")) {
            authRequest(soc, args);
        }

        if(SocketAuthManager.isAuthed(soc)) {
            if(eventName.equalsIgnoreCase("REQUEST")) {
                request(args, soc);
            }
        } else {
            try {
                SocketRequest req = new SocketRequest(new JsonObject());
                req.setResponse(HttpStatus.UNAUTHORIZED_401, "TEXT", "ERR_REQUIRE_AUTH");
                ((SocketIoSocket.ReceivedByLocalAcknowledgementCallback) args[1]).sendAcknowledgement(req.getResponseAsJson());
            } catch(Exception IGNORE) {}
        }
    }

    public static void request(Object[] args, SocketIoSocket soc) {
        JsonObject jsonobj = new JsonParser().parse(String.valueOf(args[0])).getAsJsonObject();
        SocketRequest req = new SocketRequest(jsonobj);
        String type = req.json.get("TYPE").getAsString();
        req.perms.setAllTo(true); //ALLOW EVERYTHING

        //SYNC OPERATIONS
        if(type.equalsIgnoreCase("EXECUTE") || type.equalsIgnoreCase("DATA") || type.equalsIgnoreCase("PAGEDATA")) {
            Bukkit.getScheduler().runTask(main.pl, () -> {
                try {
                    if(type.equalsIgnoreCase("EXECUTE") && hasMethod(jsonobj)) {
                        handleExecutionRequest(req);
                    } else if(type.equalsIgnoreCase("DATA") && hasMethod(jsonobj)) {
                        handleDataRequest(req);
                    } else if(type.equalsIgnoreCase("PAGEDATA")) {
                        handlePageDataRequest(req);
                    } else {
                        req.setResponse(404, "TEXT", "NOT_HANDLED");
                    }
                } catch(Exception ex) {
                    errorCatcher.catchException(ex, false);
                    req.setResponse(500, "TEXT", "INTERNAL_ERROR");
                }
                ((SocketIoSocket.ReceivedByLocalAcknowledgementCallback) args[1]).sendAcknowledgement(req.getResponseAsJson());
            });

        //ASYNC OPERATIONS
        } else {
            try {
                if(type.equalsIgnoreCase("PAGE")) {
                    handlePageRequest(req);
                } else if(type.equalsIgnoreCase("WEBFILE")) {
                    handleWebfileRequest(req);
                } else if(type.equalsIgnoreCase("SYSFILE") && hasMethod(jsonobj) && req.respondWithPermErrorIfFalse(req.perms.TAB_WORLDS)) {
                    handleSysfileRequest(req);
                }
            } catch(Exception ex) {
                errorCatcher.catchException(ex, false);
                req.setResponse(500, "TEXT", "INTERNAL_ERROR");
            }
            ((SocketIoSocket.ReceivedByLocalAcknowledgementCallback) args[1]).sendAcknowledgement(req.getResponseAsJson());
        }
    }

    public static boolean hasMethod(JsonObject json) {
        return json.has("METHOD");
    }

    public static void authRequest(SocketIoSocket soc, Object[] args) {
        JsonObject json = new JsonParser().parse(String.valueOf(args[0])).getAsJsonObject();
        SocketRequest req = new SocketRequest(json);

        String method = req.json.get("METHOD").getAsString();
        if(method.equalsIgnoreCase("STATE")) {
            req.setResponse(200, "BOOLEAN", SocketAuthManager.isAuthed(soc));
        } else if(method.equalsIgnoreCase("AUTHENTICATE")) {
            if (json.has("USERNAME") && json.has("PASSWORD")) {
                SocketAuthManager.authSocket(json.get("USERNAME").getAsString(), json.get("PASSWORD").getAsString(), soc, req);
            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_NAME_OR_PASSWORD");
            }
        }

        ((SocketIoSocket.ReceivedByLocalAcknowledgementCallback) args[1]).sendAcknowledgement(req.getResponseAsJson());
    }

    public static void handlePageRequest(SocketRequest req) {
        if(req.json.has("PAGE")) {
            req.setResponse(200, "TEXT", webBundler.getBundledPage("pages/" + req.json.get("PAGE").getAsString()));
        } else {
            req.setResponse(400, "TEXT", "ERR_MISSING_PAGE");
        }
    }

    public static void handleWebfileRequest(SocketRequest req) throws Exception {
        if(req.json.has("PATH")) {
            String file = req.json.get("PATH").getAsString();
            byte[] content = main.webroot.getBytesOfFile("/" + file);

            if(content == null) {
                req.setResponse(404, "TEXT", "ERR_FILE_NOT_FOUND");
                return;
            }

            if(file.toLowerCase().indexOf(".html") > 0 || file.toLowerCase().indexOf(".css") > 0 || file.toLowerCase().indexOf(".js") > 0) {
                req.setResponse(200, "TEXT", translations.replaceTranslationsInString(new String(content, StandardCharsets.UTF_8)));
            } else {
                req.setResponse(200, "BYTES", content);
            }
        } else {
            req.setResponse(400, "TEXT", "ERR_MISSING_FILE");
        }
    }

    public static void handleSysfileRequest(SocketRequest req) {
        JsonObject json = req.json;
        String method = json.get("METHOD").getAsString();

        if(req.json.has("PATH")) {
            String file = req.json.get("PATH").getAsString();
            File f = dataFetcher.getFileWithPath(file);

            if(dataFetcher.isFileInsideServerFolder(f)) {
                if (f.exists()) {
                    if (method.equalsIgnoreCase("GET") && req.respondWithPermErrorIfFalse(req.perms.FILES_VIEW)) {
                        if (f.isFile()) {
                            req.setResponse(200, "FILE", f);
                        } else {
                            req.setResponse(400, "TEXT", "ERR_FILE_IS_DIR");
                        }
                    }

                    if(method.equalsIgnoreCase("GET_TYPE") && req.respondWithPermErrorIfFalse(req.perms.FILES_VIEW)) {
                        if(f.isFile()) {
                            req.setResponse(200, "TEXT", dataFetcher.getMimeType(f));
                        } else {
                            req.setResponse(400, "TEXT", "ERR_FILE_IS_DIR");
                        }
                    }

                    if (method.equalsIgnoreCase("DELETE") && req.respondWithPermErrorIfFalse(req.perms.FILES_EDIT)) {
                        if (f.isFile()) {
                            try {
                                FileDeleteStrategy.FORCE.delete(f);
                                req.setResponse(200, "TEXT", "DELETED");
                            } catch (IOException e) {
                                req.setResponse(500, "TEXT", "ERR_FILE_DELETE_FAILED");
                            }
                        }
                    }

                    if (method.equalsIgnoreCase("RENAME") && req.respondWithPermErrorIfFalse(req.perms.FILES_EDIT)) {
                        if (req.json.has("NEWNAME")) {
                            File newF = new File(f.getParentFile(), req.json.get("NEWNAME").getAsString());
                            if (!newF.exists()) {
                                f.renameTo(newF);
                                req.setResponse(200, "TEXT", "RENAMED");
                            } else {
                                req.setResponse(400, "TEXT", "ERR_FILE_ALREADY_EXISTS");
                            }
                        } else {
                            req.setResponse(400, "TEXT", "ERR_MISSING_NEWNAME");
                        }
                    }

                    if (method.equalsIgnoreCase("MOVE") && req.respondWithPermErrorIfFalse(req.perms.FILES_EDIT)) {
                        if (req.json.has("NEWPATH")) {
                            File newF = dataFetcher.getFileWithPath(req.json.get("NEWPATH").getAsString());
                            if (!newF.exists()) {
                                f.renameTo(newF);
                                req.setResponse(200, "TEXT", "MOVED");
                            } else {
                                req.setResponse(400, "TEXT", "ERR_FILE_ALREADY_EXISTS");
                            }
                        } else {
                            req.setResponse(400, "TEXT", "ERR_MISSING_NEWNAME");
                        }
                    }

                    if (method.equalsIgnoreCase("COPY") && req.respondWithPermErrorIfFalse(req.perms.FILES_EDIT)) {
                        if (req.json.has("NEWPATH")) {
                            File newF = dataFetcher.getFileWithPath(req.json.get("NEWPATH").getAsString());
                            if (!newF.exists()) {
                                try {
                                    Files.copy(Paths.get(f.getAbsolutePath()), Paths.get(newF.getAbsolutePath()));
                                    req.setResponse(200, "TEXT", "COPIED");
                                } catch (IOException e) {
                                    req.setResponse(500, "TEXT", "ERR_COPY_FAILED");
                                }
                            } else {
                                req.setResponse(400, "TEXT", "ERR_FILE_ALREADY_EXISTS");
                            }
                        } else {
                            req.setResponse(400, "TEXT", "ERR_MISSING_NEWNAME");
                        }
                    }

                    if (method.equalsIgnoreCase("UPLOAD") && req.respondWithPermErrorIfFalse(req.perms.FILES_UPLOAD)) {
                        if (req.json.has("ID")) {
                            String id = req.json.get("ID").getAsString();

                            if (FileUploadServlet.cachedFiles.containsKey(id)) {
                                Part cachedFile = FileUploadServlet.cachedFiles.get(id);
                                FileUploadServlet.cachedFiles.remove(id);

                                if (f.isDirectory()) {
                                    File output = new File(f, cachedFile.getSubmittedFileName());
                                    try {
                                        Files.copy(cachedFile.getInputStream(), Paths.get(output.getAbsolutePath()));
                                        req.setResponse(200, "TEXT", "WRITTEN");
                                    } catch (IOException e) {
                                        req.setResponse(500, "TEXT", "ERR_FILE_WRITE_FAIL");
                                    }
                                } else {
                                    req.setResponse(400, "TEXT", "ERR_FILE_NODIR");
                                }
                            } else {
                                req.setResponse(404, "TEXT", "ERR_FILE_NOT_IN_CACHE");
                            }
                        } else {
                            req.setResponse(400, "TEXT", "ERR_MISSING_ID");
                        }
                    }
                } else {
                    req.setResponse(404, "TEXT", "ERR_FILE_NOT_FOUND");
                }
            } else {
                req.setResponse(400, "TEXT", "ERR_FILE_INSECURE");
            }
        } else {
            req.setResponse(400, "TEXT", "ERR_MISSING_PATH");
        }
    }

    public static void handleExecutionRequest(SocketRequest req) {
        JsonObject json = req.json;
        String method = json.get("METHOD").getAsString();

        if (method.equalsIgnoreCase("EXEC_COMMAND") && req.respondWithPermErrorIfFalse(req.perms.CONSOLE_EXECUTE)) {
            if (json.has("COMMAND")) {
                try {
                    pluginConsole.sendMessage("Executing: &6/" + json.get("COMMAND").getAsString());
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), json.get("COMMAND").getAsString());
                    req.setResponse(200, "TEXT", "EXECUTED");
                    return;
                } catch (Exception ex) {
                    req.setResponse(500, "TEXT", "ERR_EXEC_FAILED");
                    return;
                }
            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_COMMAND");
                return;
            }
        }

        if (method.equalsIgnoreCase("TOGGLE_PLUGIN") && req.respondWithPermErrorIfFalse(req.perms.PLUGINS_TOGGLE)) {
            if (json.has("PLUGIN")) {
                Plugin pl = pluginManager.getPlugin(json.get("PLUGIN").getAsString());
                if (pl.isEnabled()) {
                    boolean suc = pluginManager.disablePlugin(pl);
                    req.setResponse(suc ? 200 : 500, "TEXT", suc ? "SUCCESS" : "ERROR");
                    return;
                } else {
                    boolean suc = pluginManager.load(json.get("PLUGIN").getAsString());
                    req.setResponse(suc ? 200 : 500, "TEXT", suc ? "SUCCESS" : "ERROR");
                    return;
                }
            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_PLUGIN");
                return;
            }
        }

        if(method.equalsIgnoreCase("CONTROL") && req.respondWithPermErrorIfFalse(req.perms.TAB_CONTROLS)) {
            if(json.has("ACTION")) {
                String action = json.get("ACTION").getAsString();
                if(action.equalsIgnoreCase("STOP") && req.respondWithPermErrorIfFalse(req.perms.CONTROLS_STOP)) {
                    req.setResponse(200, "TEXT", "SUCCESS");
                    Bukkit.shutdown();
                    return;
                }

                if(action.equalsIgnoreCase("RELOAD") && req.respondWithPermErrorIfFalse(req.perms.CONTROLS_RELOAD)) {
                    req.setResponse(200, "TEXT", "SUCCESS");
                    Bukkit.reload();
                    return;
                }

                if(action.equalsIgnoreCase("TOGGLE_NETHER")  && req.respondWithPermErrorIfFalse(req.perms.CONTROLS_WORLD_NETHER)) {
                    boolean current = Boolean.parseBoolean(dataFetcher.getServerPropertie("allow-nether"));
                    dataFetcher.setServerPropertie("allow-nether", String.valueOf(!current));
                    notificationManager.setNeedReload(true);
                    req.setResponse(200, "TEXT", "SUCCESS");
                    return;
                }

                if(action.equalsIgnoreCase("TOGGLE_WHITELIST")  && req.respondWithPermErrorIfFalse(req.perms.CONTROLS_WHITELIST_TOGGLE)) {
                    boolean current = Bukkit.hasWhitelist();

                    Bukkit.setWhitelist(!current);
                    dataFetcher.setServerPropertie("white-list", String.valueOf(!current));
                    Bukkit.reloadWhitelist();
                    req.setResponse(200, "TEXT", "SUCCESS");
                    return;
                }

                if(action.equalsIgnoreCase("WHITELIST_ADD") && req.respondWithPermErrorIfFalse(req.perms.CONTROLS_WHITELIST_EDIT)) {
                    if (json.has("PLAYER")) {
                        String uuid = json.get("PLAYER").getAsString();
                        Bukkit.getOfflinePlayer(uuid).setWhitelisted(true);
                        req.setResponse(200, "TEXT", "SUCCESS");
                        return;
                    } else {
                        req.setResponse(400, "TEXT", "ERR_MISSING_PLAYER");
                        return;
                    }
                }

                if(action.equalsIgnoreCase("WHITELIST_REMOVE") && req.respondWithPermErrorIfFalse(req.perms.CONTROLS_WHITELIST_EDIT)) {
                    if (json.has("PLAYER")) {
                        String uuid = json.get("PLAYER").getAsString();
                        UUID uuidObj = dataFetcher.uuidFromUUIDWithoutDashes(uuid.replaceAll("-", ""));
                        Bukkit.getOfflinePlayer(uuidObj).setWhitelisted(false);
                        req.setResponse(200, "TEXT", "SUCCESS");
                        return;
                    } else {
                        req.setResponse(400, "TEXT", "ERR_MISSING_PLAYER");
                        return;
                    }
                }

                if(action.equalsIgnoreCase("OPERATOR_ADD") && req.respondWithPermErrorIfFalse(req.perms.CONTROLS_OPS_EDIT)) {
                    if (json.has("PLAYER")) {
                        String uuid = json.get("PLAYER").getAsString();
                        Bukkit.getOfflinePlayer(uuid).setOp(true);
                        req.setResponse(200, "TEXT", "SUCCESS");
                        return;
                    } else {
                        req.setResponse(400, "TEXT", "ERR_MISSING_PLAYER");
                        return;
                    }
                }

                if(action.equalsIgnoreCase("OPERATOR_REMOVE") && req.respondWithPermErrorIfFalse(req.perms.CONTROLS_OPS_EDIT)) {
                    if (json.has("PLAYER")) {
                        String uuid = json.get("PLAYER").getAsString();
                        UUID uuidObj = dataFetcher.uuidFromUUIDWithoutDashes(uuid.replaceAll("-", ""));
                        Bukkit.getOfflinePlayer(uuidObj).setOp(false);
                        req.setResponse(200, "TEXT","SUCCESS");
                        return;
                    } else {
                        req.setResponse(400, "TEXT", "ERR_MISSING_PLAYER");
                        return;
                    }
                }

                if(action.equalsIgnoreCase("TOGGLE_END") && req.respondWithPermErrorIfFalse(req.perms.CONTROLS_WORLD_END)) {
                    boolean current = (Bukkit.getWorld("world_the_end") != null);
                    boolean suc = dataFetcher.modifyBukkitPropertie("settings.allow-end", !current);
                    notificationManager.setNeedReload(true);
                    req.setResponse(suc ? 200 : 500, "TEXT", suc ? "SUCCESS" : "ERROR");
                    return;
                }

            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_ACTION");
                return;
            }
        }

        // TODO: 06.11.2021 Add Permissions
        if(method.equalsIgnoreCase("CONTROL_WORLD")) {
            if(json.has("WORLD")) {
                if(json.has("ACTION")) {
                    String action = json.get("ACTION").getAsString();
                    World w = Bukkit.getWorld(json.get("WORLD").getAsString());
                    if(w != null) {
                        if(action.equalsIgnoreCase("WEATHER")) {
                            if(json.has("WEATHER")) {
                                String weather = json.get("WEATHER").getAsString();
                                final boolean thundering = weather.equalsIgnoreCase("Thunder");
                                final boolean storming = weather.equalsIgnoreCase("Rain") || weather.equalsIgnoreCase("Thunder");

                                w.setStorm(storming);
                                w.setThundering(thundering);

                                if(thundering) {
                                    w.setThunderDuration(generateRandom(20*60*13, 20*60*3 + 1));
                                }

                                if(storming) {
                                    w.setThunderDuration((int) generateRandom(24000*0.5, 24000*0.75 + 1));
                                }
                                req.setResponse(200, "TEXT", "SUCCESS");
                                return;
                            } else {
                                req.setResponse(400, "TEXT", "ERR_MISSING_WEATHER");
                                return;
                            }
                        }

                        if(action.equalsIgnoreCase("TIME")) {
                            if(json.has("TIME")) {
                                final long time = json.get("TIME").getAsLong();

                                w.setTime(time);
                                req.setResponse(200, "TEXT", "SUCCESS");
                                return;
                            } else {
                                req.setResponse(400, "TEXT", "ERR_MISSING_TIME");
                                return;
                            }
                        }

                        if(action.equalsIgnoreCase("KILL_ENTITY_TYPE")) {
                            if(json.has("ENTTYPE")) {
                                EntityType entType = EntityType.valueOf(json.get("ENTTYPE").getAsString());
                                for (Entity e : w.getEntities()) {
                                    if (e.getType() == entType)
                                        e.remove();
                                }
                                req.setResponse(200, "TEXT", "KILLED");
                                return;
                            } else {
                                req.setResponse(400, "TEXT", "ERR_MISSING_ENTTYPE");
                                return;
                            }
                        }

                    } else {
                        req.setResponse(400, "TEXT", "ERR_NOTFOUND_WORLD");
                        return;
                    }
                } else {
                    req.setResponse(400, "TEXT", "ERR_MISSING_ACTION");
                    return;
                }
            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_WORLD");
                return;
            }
        }

        if (method.equalsIgnoreCase("PLAYER_ACTION")  && req.respondWithPermErrorIfFalse(req.perms.TAB_PLAYERS)) {
            if (json.has("PLAYER")) {
                String uuid = json.get("PLAYER").getAsString();
                Player p = Bukkit.getPlayer(UUID.fromString(uuid));

                if (p != null && p.isOnline()) {
                    if (json.has("ACTION")) {
                        String action = json.get("ACTION").getAsString();

                        if (action.equalsIgnoreCase("MESSAGE") && req.respondWithPermErrorIfFalse(req.perms.PLAYERS_MESSAGE)) {
                            if (json.has("MESSAGE")) {
                                String message = json.get("MESSAGE").getAsString();
                                message = "&cServer &a--> &6You: &7" + message;
                                message = ChatColor.translateAlternateColorCodes('&', message);
                                p.sendMessage(message);

                                req.setResponse(200, "TEXT", "SUCCESS");
                                return;
                            } else {
                                req.setResponse(400, "TEXT", "ERR_MISSING_MESSAGE");
                                return;
                            }
                        }

                        if (action.equalsIgnoreCase("KICK") && req.respondWithPermErrorIfFalse(req.perms.PLAYERS_KICK)) {
                            if (json.has("MESSAGE")) {
                                String message = json.get("MESSAGE").getAsString();
                                message = "&cKicked from the Server:\n&b" + message;
                                message = ChatColor.translateAlternateColorCodes('&', message);
                                p.kickPlayer(message);

                                req.setResponse(200, "TEXT", "SUCCESS");
                                return;
                            } else {
                                req.setResponse(400, "TEXT", "ERR_MISSING_MESSAGE");
                                return;
                            }
                        }
                    } else {
                        req.setResponse(400, "TEXT", "ERR_MISSING_ACTION");
                        return;
                    }
                } else {
                    req.setResponse(400, "TEXT", "ERR_PLAYER_NOT_FOUND");
                    return;
                }
            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_PLAYER");
                return;
            }
        }

        if (method.equalsIgnoreCase("INSTALL_PLUGIN") && req.respondWithPermErrorIfFalse(req.perms.PLUGINS_INSTALL)) {
            if (json.has("ID")) {
                String install_state = pluginInstaller.installPlugin(json.get("ID").getAsString());
                int code = install_state.equalsIgnoreCase("INSTALLED") ? 200 : 500;
                req.setResponse(code, "TEXT", install_state);
                return;
            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_NOTIFICATION_UUID");
                return;
            }
        }

        // TODO: 06.11.2021 Add Permission
        if (method.equalsIgnoreCase("NOTIFICATION_CLOSED")) {
            if (json.has("UUID")) {
                notificationManager.closeNotification(json.get("UUID").getAsString());
                req.setResponse(200, "TEXT", "REMOVED");
                return;
            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_NOTIFICATION_UUID");
                return;
            }
        }

        if (method.equalsIgnoreCase("TOGGLE_PLUGIN") && req.respondWithPermErrorIfFalse(req.perms.PLUGINS_TOGGLE)) {
            if (json.has("PLUGIN")) {
                Plugin pl = pluginManager.getPlugin(json.get("PLUGIN").getAsString());
                if (pl.isEnabled()) {
                    boolean suc = pluginManager.disablePlugin(pl);
                    req.setResponse(suc ? 200 : 500, "TEXT", suc ? "SUCCESS" : "ERROR");
                } else {
                    boolean suc = pluginManager.load(json.get("plugin").getAsString());
                    req.setResponse(suc ? 200 : 500, "TEXT", suc ? "SUCCESS" : "ERROR");
                }
            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_PLUGIN");
            }
        }
    }

    public static void handleDataRequest(SocketRequest req) {
        JsonObject json = req.json;
        String method = json.get("METHOD").getAsString();

        // TODO: 06.11.2021 Add Permission 
        if(method.equalsIgnoreCase("GET_INTEGRATIONS")) {
            HashMap<String, Object> integrationObjects = new HashMap<>();
            integrationObjects.put("SKRIPT", main.skriptIntegration.getIntegrationObject());

            req.setResponse(200, "TEXT", integrationObjects);
            return;
        }

        if (method.equalsIgnoreCase("GET_FILES_IN_PATH") && req.respondWithPermErrorIfFalse(req.perms.TAB_FILES)) {
            if (json.has("PATH")) {
                String path = json.get("PATH").getAsString();
                req.setResponse(200, "TEXT", dataFetcher.getFilesInPath(path));
                return;
            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_PATH");
                return;
            }
        }

        if(method.equalsIgnoreCase("GET_WORLD") && req.respondWithPermErrorIfFalse(req.perms.TAB_WORLDS)) {
            if (json.has("WORLD")) {
                if(Bukkit.getWorld(json.get("WORLD").getAsString()) == null) {
                    req.setResponse(200,"TEXT", "ERR_NOTFOUND_WORLD");
                    return;
                }

                req.setResponse(200, "TEXT", dataFetcher.getWorldForWeb(Objects.requireNonNull(Bukkit.getWorld(json.get("WORLD").getAsString()))));
                return;
            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_WORLD");
                return;
            }
        }

        if(method.equalsIgnoreCase("GET_NOTIFICATIONS")) {
            req.setResponse(200, "TEXT", notificationManager.notifications);
            return;
        }

        if(method.equalsIgnoreCase("THEME"))  {
            req.setResponse(200, "TEXT", (configuration.yaml_cfg.getBoolean("darkMode") ? "dark" : "light"));
        }
    }

    public static void handlePageDataRequest(SocketRequest req) {
        if(req.json.has("PAGE")) {
            String page = req.json.get("PAGE").getAsString();
            if(page.equalsIgnoreCase("OVERVIEW") && req.respondWithPermErrorIfFalse(req.perms.TAB_OVERVIEW)) {
                req.setResponse(200, "TEXT", pageDataFetcher.GET_PAGE_OVERVIEW());
                return;
            }

            if(page.equalsIgnoreCase("GRAPHS") && req.respondWithPermErrorIfFalse(req.perms.TAB_GRAPHS)) {
                req.setResponse(200, "TEXT", pageDataFetcher.GET_PAGE_GRAPHS());
                return;
            }

            if(page.equalsIgnoreCase("WORLDS") && req.respondWithPermErrorIfFalse(req.perms.TAB_WORLDS)) {
                req.setResponse(200, "TEXT", pageDataFetcher.GET_PAGE_WORLDS());
                return;
            }

            if(page.equalsIgnoreCase("CONSOLE") && req.respondWithPermErrorIfFalse(req.perms.TAB_CONSOLE)) {
                req.setResponse(200, "TEXT", dataFetcher.getLog(100));
                return;
            }

            if(page.equalsIgnoreCase("CONTROLS") && req.respondWithPermErrorIfFalse(req.perms.TAB_CONTROLS)) {
                req.setResponse(200, "TEXT", pageDataFetcher.GET_PAGE_CONTROLS());
                return;
            }

            if(page.equalsIgnoreCase("PLUGINS") && req.respondWithPermErrorIfFalse(req.perms.TAB_PLUGINS)) {
                req.setResponse(200, "TEXT", pageDataFetcher.GET_PAGE_PLUGINS());
                return;
            }

            if(page.equalsIgnoreCase("PLAYERS") && req.respondWithPermErrorIfFalse(req.perms.TAB_PLAYERS)) {
                req.setResponse(200, "TEXT", pageDataFetcher.GET_PAGE_PLAYERS());
            }
        } else {
            req.setResponse(400, "TEXT", "ERR_MISSING_PAGE");
        }
    }

    public static int generateRandom(int max, int min)  {
        int r = (int) ((int) (Math.random() * (max - min)) + min);
        return r;
    }

    public static double generateRandom(double max, double min)  {
        double r = ((int) (Math.random() * (max - min)) + min);
        return r;
    }
}
