package de.tobias.spigotdash.web.sockets;

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.tobias.spigotdash.utils.errorCatcher;
import org.apache.commons.io.FileUtils;
import org.eclipse.jetty.util.resource.Resource;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;

public class SocketRequest {

    public JsonObject json;

    public Integer repCode;
    public String repType;
    public Object repData;

    public SocketRequest(JsonObject data) {
        this.json = data;
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
                this.repData = Resources.toByteArray((URL) data);
            }
        } catch (IOException e) {
            errorCatcher.catchException(e, false);
            this.repData = "ERR_RESOURCE_ENCODE_FAILED";
            this.repType = "TEXT";
            this.repCode = 500;
        }

    }

    public Object getResponseAsObject() {
        HashMap<String, Object> response = new HashMap<>();
        response.put("CODE", this.repCode);
        response.put("DATA", this.repData);
        response.put("TYPE", this.repType);
        return response;
    }

    public String getResponseAsJson() {
        return (new Gson()).toJson(getResponseAsObject());
    }
}
