package de.tobias.spigotdash.web.sockets;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.tobias.spigotdash.utils.errorCatcher;
import de.tobias.spigotdash.utils.pluginConsole;
import de.tobias.spigotdash.web.PermissionSet;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

public class SocketRequest {

    public static Gson internalGSON = (new GsonBuilder()).serializeNulls().create();
    public JsonObject json;

    public Integer repCode;
    public String repType;
    public Object repData;
    public PermissionSet perms;

    public SocketRequest(JsonObject data) {
        this.json = data;
        this.perms = new PermissionSet();
    }

    public void setPermissionSet(JsonObject permissions) {
        this.perms.loadFromJsonObject(permissions);
    }

    public boolean respondWithPermErrorIfFalse(boolean has) {
        if(!has) {
            this.setResponse(HttpStatus.METHOD_NOT_ALLOWED_405, "TEXT", "ERR_PERM_MISSING");
        }

        return has;
    }

    public void setResponse(Integer code, String type, Object data) {
        this.repCode = code;
        this.repType = type;
        this.repData = data;

        try {
            if(type.equalsIgnoreCase("FILE")) {
                this.repData = FileUtils.readFileToByteArray((File) data);
            }
        } catch (IOException e) {
            errorCatcher.catchException(e, false);
            this.repData = "ERR_FILE_ENCODE_FAILED";
            this.repType = "TEXT";
            this.repCode = 500;
        }

        try {
            if(type.equalsIgnoreCase("RESOURCE")) {
                if(data != null) {
                    URL dataAsURL = (URL) data;
                    try {
                        this.repData = Resources.toByteArray((URL) data);
                    } catch (Exception ex) {
                        this.repData = "ERR_RESOURCE_MISSING";
                        this.repType = "TEXT";
                        this.repCode = 500;
                        pluginConsole.sendMessage("Invalid Resource Response: " + String.valueOf(data));
                    }
                } else {
                    this.repData = "ERR_RESOURCE_NULL";
                    this.repType = "TEXT";
                    this.repCode = 500;
                }
            }
        } catch (Exception e) {
            errorCatcher.catchException(e, false);
            this.repData = "ERR_RESOURCE_ENCODE_FAILED";
            this.repType = "TEXT";
            this.repCode = 500;
        }

    }

    public Object getResponseAsObject() {
        if(repCode == null || repData == null || repType == null) {
            repCode = 404;
            repData = "ERR_RESPONSE_WOULD_BE_NULL";
            repType = "TEXT";
        }

        HashMap<String, Object> response = new HashMap<>();
        response.put("CODE", this.repCode);
        response.put("DATA", this.repData);
        response.put("TYPE", this.repType);
        return response;
    }

    public String getResponseAsJson() {
        return internalGSON.toJson(getResponseAsObject());
    }
}
