package de.tobias.spigotdash.web.sockets;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.tobias.spigotdash.main;
import de.tobias.spigotdash.utils.files.Group;
import de.tobias.spigotdash.utils.files.User;
import de.tobias.spigotdash.utils.files.configuration;
import de.tobias.spigotdash.utils.files.translations;
import de.tobias.spigotdash.utils.notificationManager;
import de.tobias.spigotdash.utils.pluginConsole;
import de.tobias.spigotdash.utils.plugins.pluginInstaller;
import de.tobias.spigotdash.utils.plugins.pluginManager;
import de.tobias.spigotdash.web.PermissionSet;
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

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SocketEventHandler {

    public static void handleSocketEvent(SocketIoSocket soc, String eventName, Object[] args) {
        //ONLY ALLOW REQUESTS
        if(!eventName.equalsIgnoreCase("REQUEST")) ((SocketIoSocket.ReceivedByLocalAcknowledgementCallback) args[1]).sendAcknowledgement("INVALID_METHOD");

        //CONSTRUCT REQUEST AND SET DEFAULT RESPONSE
        SocketRequest req;
        JsonObject json = new JsonParser().parse(String.valueOf(args[0])).getAsJsonObject();
        req = new SocketRequest(soc, json);

        //LOAD PERMISSIONS IF LOGGED IN
        if(SocketAuthManager.isAuthed(soc)) {
            req.perms = SocketAuthManager.getPermissions(soc);
        }

        String type = req.type;
        if(req.perms != null) {
            if(needsSync(req)) {
                Bukkit.getScheduler().runTask(main.pl, () -> {
                    if(type.equalsIgnoreCase("EXECUTE") && req.method != null) handleExecutionRequest(req);
                    if(type.equalsIgnoreCase("DATA") && req.method != null) handleDataRequest(req);
                    if(type.equalsIgnoreCase("PAGEDATA")) handlePageDataRequest(req);

                    ((SocketIoSocket.ReceivedByLocalAcknowledgementCallback) args[1]).sendAcknowledgement(req.getResponseAsJson());
                });
                return; //NEED TO ABORT BECAUSE ACKNOWLEDGEMENT IS SENT BY TASK
            } else {
                if(type.equalsIgnoreCase("ACCOUNT")) { accountRequest(req); }
                if(type.equalsIgnoreCase("PAGE")) handlePageRequest(req);
                if(type.equalsIgnoreCase("WEBFILE")) handleWebfileRequest(req);
                if(type.equalsIgnoreCase("SYSFILE")) handleSysfileRequest(req);
            }

        } else {
            if(type.equalsIgnoreCase("ACCOUNT")) { accountRequest(req); }
            else { req.setResponse(401, "TEXT", "ERR_REQUIRE_AUTH"); }
        }

        ((SocketIoSocket.ReceivedByLocalAcknowledgementCallback) args[1]).sendAcknowledgement(req.getResponseAsJson());

    }

    public static boolean needsSync(SocketRequest req) {
        if(req.type == null) return true;
        if(req.type.equalsIgnoreCase("EXECUTE") || req.type.equalsIgnoreCase("DATA") || req.type.equalsIgnoreCase("PAGEDATA")) return true;
        return false;
    }

    public static void accountRequest(SocketRequest req) {
        String method = req.method;
        SocketIoSocket soc = req.socket;

        if (method.equalsIgnoreCase("LOGGED_IN")) {
            req.setResponse(200, "BOOLEAN", SocketAuthManager.isAuthed(soc));
            return;
        }

        if(method.equalsIgnoreCase("PERMISSIONS")) {
            if(SocketAuthManager.isAuthed(soc)) {
                req.setResponse(400, "TEXT", PermissionSet.getAsJsonObject(SocketAuthManager.getPermissions(soc)));
                return;
            } else {
                req.setResponse(400, "TEXT", "ERR_AUTH_REQUIRED");
                return;
            }
        }

        if(method.equalsIgnoreCase("LOGIN")) {
            if (req.json.has("USERNAME") && req.json.has("PASSWORD")) {
                SocketAuthManager.authSocket(req.json.get("USERNAME").getAsString(), req.json.get("PASSWORD").getAsString(), soc, req);
                return;
            } else {
                req.setResponse(400, "TEXT", "ERR_MISSING_NAME_OR_PASSWORD");
                return;
            }
        }

        if(method.toUpperCase(Locale.ROOT).indexOf("GROUP") != -1) {
            if(req.respondWithPermErrorIfFalse(req.perms.TAB_GROUPS)) {
                if(method.equalsIgnoreCase("GET_GROUPS") && req.respondWithPermErrorIfFalse(req.perms.USERS_VIEW)) {
                    req.setResponse(400, "TEXT", main.GroupsFile.getGroupsSave());
                    return;
                }

                if(method.equalsIgnoreCase("UPDATE_GROUP") && req.respondWithPermErrorIfFalse(req.perms.GROUPS_EDIT)) {
                    if(req.json.has("GROUP")) {
                        Group g = main.GroupsFile.getGroupByID(req.json.get("GROUP").getAsString());
                        if(g != null) {
                            if(req.json.has("NAME")) {
                                String newName = req.json.get("NAME").getAsString();
                                if(!g.name.equals(newName)) {
                                    if(!main.GroupsFile.groupExists(newName)) {
                                        g.name = newName;
                                    } else {
                                        req.setResponse(400, "TEXT", "ERR_NAME_ALREADY_TAKEN");
                                        return;
                                    }
                                }
                            }

                            if(req.json.has("LEVEL")) {
                                g.LEVEL = req.json.get("LEVEL").getAsInt();
                            }

                            if(req.json.has("HTML_COLOR")) {
                                g.html_color = req.json.get("HTML_COLOR").getAsString();
                            }

                            if(req.json.has("PERMS")) {
                                PermissionSet newPerms = main.gson.fromJson(req.json.get("PERMS"), PermissionSet.class);
                                if(g.IS_ADMIN_GROUP) newPerms.setAllTo(true); //PREVENT OVERWRITING ADMIN GROUP
                                if(newPerms != null) {
                                    g.permissions = newPerms;
                                }
                            }

                            main.GroupsFile.save();
                            req.setResponse(200, "TEXT", "FIELDS_UPDATED");
                        } else {
                            req.setResponse(404, "TEXT", "ERR_GROUP_NOT_FOUND");
                        }
                    } else {
                        req.setResponse(400, "TEXT", "ERR_MISSING_GROUP");
                    }
                    return;
                }

                if(method.equalsIgnoreCase("CREATE_GROUP") && req.respondWithPermErrorIfFalse(req.perms.GROUPS_ADD)) {
                    if(req.json.has("NAME")) {
                        if(req.json.has("HTML_COLOR")) {
                            if(req.json.has("PERMS")) {
                                // TODO: 30.12.21 Users should only be possible, to set permissions they have
                                if(!main.GroupsFile.groupExists(req.json.get("NAME").getAsString())) {
                                    PermissionSet newPerms = main.gson.fromJson(req.json.get("PERMS"), PermissionSet.class);
                                    Group g = new Group(req.json.get("NAME").getAsString(), newPerms);
                                    g.html_color = req.json.get("HTML_COLOR").getAsString();
                                    main.GroupsFile.addGroup(g);

                                    req.setResponse(200, "TEXT", "CREATED: " + g.id);
                                } else {
                                    req.setResponse(400, "TEXT", "ERR_NAME_TAKEN");
                                }
                            } else {
                                req.setResponse(400, "TEXT", "ERR_MISSING_PERMS");
                            }
                        } else {
                            req.setResponse(400, "TEXT", "ERR_MISSING_HTML_COLOR");
                        }
                    } else {
                        req.setResponse(400, "TEXT", "ERR_MISSING_NAME");
                    }
                    return;
                }

                if(method.equalsIgnoreCase("DELETE_GROUP") && req.respondWithPermErrorIfFalse(req.perms.GROUPS_DELETE)) {
                    if (req.json.has("GROUP")) {
                        Group g = main.GroupsFile.getGroupByID(req.json.get("GROUP").getAsString());
                        if (g != null) {
                            if (!g.IS_DEFAULT_GROUP && !g.IS_ADMIN_GROUP) {
                                if (main.GroupsFile.deleteGroup(g)) {
                                    req.setResponse(200, "TEXT", "DELETED");
                                } else {
                                    req.setResponse(500, "TEXT", "ERR_DELETE_FAILED");
                                }
                            } else {
                                req.setResponse(401, "TEXT", "ERR_GROUP_IS_INTERNAL_GROUP");
                            }
                        } else {
                            req.setResponse(404, "TEXT", "ERR_GROUP_NOT_FOUND");
                        }
                    } else {
                        req.setResponse(400, "TEXT", "ERR_MISSING_GROUP");
                    }
                    return;
                }
            }
        }

        if(method.toUpperCase(Locale.ROOT).indexOf("USER") != -1) {
            if (req.respondWithPermErrorIfFalse(req.perms.TAB_USERS)) {
                if(method.equalsIgnoreCase("GET_USERS") && req.respondWithPermErrorIfFalse(req.perms.USERS_VIEW)) {
                    req.setResponse(200, "TEXT", main.UsersFile.getUsersSave());
                    return;
                }

                if(method.equalsIgnoreCase("CREATE_USER") && req.respondWithPermErrorIfFalse(req.perms.USERS_ADD)) {
                    if(req.json.has("NAME")) {
                        if (req.json.has("PASSWORD")) {
                            String name = req.json.get("NAME").getAsString();
                            String password = req.json.get("PASSWORD").getAsString();

                            if(main.UsersFile.getUserByName(name) == null) {
                                User u = new User(name, password);
                                main.UsersFile.add(u);

                                req.setResponse(200, "TEXT", "CREATED: " + u.name);
                            } else {
                                req.setResponse(400, "TEXT", "ERR_NAME_ALREADY_TAKEN");
                            }
                        } else {
                            req.setResponse(400, "TEXT", "ERR_MISSING_PASSWORD");
                        }
                    } else {
                        req.setResponse(400, "TEXT", "ERR_MISSING_NAME");
                    }
                    return;
                }

                if(method.equalsIgnoreCase("DELETE_USER") && req.respondWithPermErrorIfFalse(req.perms.USERS_DELETE)) {
                    // TODO: 01.01.22 CHECKS FOR DELETING YOURSELF
                    if(req.json.has("NAME")) {
                        String name = req.json.get("NAME").getAsString();
                        User u = main.UsersFile.getUserByName(name);

                        if(u != null) {
                            if(!u.perms.USERS_IS_ADMIN) {
                                main.UsersFile.deleteUser(u);
                                req.setResponse(200, "TEXT", "DELETED");
                            } else {
                                req.setResponse(401, "TEXT", "ERR_USER_IS_ADMIN");
                            }
                        } else {
                            req.setResponse(404, "TEXT", "ERR_USER_NOT_FOUND");
                        }
                    } else {
                        req.setResponse(400, "TEXT", "ERR_MISSING_NAME");
                    }
                    return;
                }

                if(method.equalsIgnoreCase("UPDATE_USER") && req.respondWithPermErrorIfFalse(req.perms.USERS_EDIT)) {
                    if(req.json.has("NAME")) {
                        String name = req.json.get("NAME").getAsString();
                        User u = main.UsersFile.getUserByName(name);

                        if(u != null) {
                            if(req.json.has("NEWNAME")) {
                                String newName = req.json.get("NEWNAME").getAsString();
                                if(!u.name.equals(newName)) {
                                    if(!main.UsersFile.userExists(newName)) {
                                        u.updateName(newName);
                                    } else {
                                        req.setResponse(400, "TEXT", "ERR_NAME_ALREADY_TAKEN");
                                        return;
                                    }
                                }
                            }

                            if(req.json.has("ROLES") && req.json.get("ROLES").isJsonArray()) {
                                ArrayList<String> newRoles = new ArrayList<>();
                                for(JsonElement o : req.json.get("ROLES").getAsJsonArray()) {
                                    if(o.getAsString() != null) {
                                        if(main.GroupsFile.getGroupByID(o.getAsString()) != null) {
                                            newRoles.add(o.getAsString());
                                        } else {
                                            req.setResponse(400, "TEXT", "ERR_UNKNOWN_GROUP_PROVIDED");
                                            return;
                                        }
                                    }
                                }

                                u.updateRoles(newRoles);
                            }

                            if(req.json.has("PERMS")) {
                                PermissionSet perms = main.gson.fromJson(req.json.get("PERMS"), PermissionSet.class);
                                if(perms != null) {
                                    u.updatePerms(perms);
                                } else {
                                    req.setResponse(400, "TEXT", "ERR_INVALID_PERMS");
                                    return;
                                }
                            }

                            if(req.json.has("PASSWORD") && !req.json.get("PASSWORD").isJsonNull()) {
                                String password = req.json.get("PASSWORD").getAsString();
                                u.changePassword(password);
                                main.UsersFile.save();
                            }

                            req.setResponse(200, "TEXT", "UPDATED");
                            return;
                        } else {
                            req.setResponse(404, "TEXT", "ERR_USER_NOT_FOUND");
                        }
                    } else {
                        req.setResponse(400, "TEXT", "ERR_MISSING_NAME");
                    }
                    return;
                }
            }
        }

        req.setResponse(500, "TEXT", "ERR_RESPONSE_WOULD_BE_NULL");
        return;
    }

    public static void handlePageRequest(SocketRequest req) {
        if(req.json.has("PAGE")) {
            req.setResponse(200, "TEXT", webBundler.getBundledPage("pages/" + req.json.get("PAGE").getAsString()));
        } else {
            req.setResponse(400, "TEXT", "ERR_MISSING_PAGE");
        }
    }

    public static void handleWebfileRequest(SocketRequest req) {
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
        String method = req.method;

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

                    if (method.equalsIgnoreCase("SAVE_EDIT") && req.respondWithPermErrorIfFalse(req.perms.FILES_EDIT)) {
                        if (f.isFile()) {
                            if(json.has("TEXT")) {
                                String text = json.get("TEXT").getAsString();
                                try {
                                    Files.write(Paths.get(f.getPath()), text.getBytes(StandardCharsets.UTF_8));
                                    req.setResponse(200, "TEXT", "WRITTEN");
                                } catch(Exception ex) {
                                    req.setResponse(500, "TEXT", "ERR_WRITE_FAIL");
                                }
                            } else {
                                req.setResponse(400, "TEXT", "ERR_MISSING_TEXT");
                            }
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
