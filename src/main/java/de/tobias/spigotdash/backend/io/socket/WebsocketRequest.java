package de.tobias.spigotdash.backend.io.socket;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import de.tobias.spigotdash.main;
import io.socket.socketio.server.SocketIoSocket;

import java.net.Socket;

public class WebsocketRequest {

    private String PAYLOAD_RAW;
    private SocketIoSocket socket;

    private Integer REQUEST_ID;
    private JsonObject REQUEST_DATA;
    private String REQUEST_METHOD;
    private String REQUEST_NAMESPACE;

    private Integer RESPONSE_CODE;
    private Object RESPONSE_DATA;
    private boolean RESPONSE_SENT;

    private fieldLogger thisLogger;

    public WebsocketRequest(String payload, fieldLogger log, SocketIoSocket socket) {
        this.PAYLOAD_RAW = payload;
        this.socket = socket;
        this.thisLogger = log;
    }

    public boolean parsePayload() {
        thisLogger.INFO("Parsing Payload of Websocket Request", 15);
        try {
            JsonObject tree = main.GLOBAL_GSON.fromJson(PAYLOAD_RAW, JsonObject.class);
            if(tree == null) throw new Exception("Recieved JSON Tree is probably not an Object");
            if(!tree.has("DATA")) throw new Exception("Provided Request has no Data");
            if(!tree.has("ID")) throw new Exception("Provided Request has no ID");
            if(!tree.has("METHOD")) throw new Exception("Provided Request has no METHOD");
            if(!tree.has("NAMESPACE")) throw new Exception("Provided Request has no NAMESPACE");
            if(!tree.has("IS_WEBSOCKET_REQUEST_V1")) throw new Exception("Provided Request is not matching this Standard");

            if(tree.has("ENCRYPTED") && tree.get("ENCRYPTED").getAsBoolean()) {
                thisLogger.INFO("Tree is encrypted! Decrypting...", 20);
            }

            this.REQUEST_ID = tree.get("ID").getAsInt();
            this.thisLogger = this.thisLogger.subFromParent(this.REQUEST_ID.toString());
            this.REQUEST_DATA = tree.get("DATA").getAsJsonObject();
            this.REQUEST_METHOD = tree.get("METHOD").getAsString();
            this.REQUEST_NAMESPACE = tree.get("NAMESPACE").getAsString();

            thisLogger.INFO("Parsing done!", 15);
            return true;
        } catch (Exception ex) {
            thisLogger.ERROREXEP("Failed to parse Payload of Websocket Request", ex, 5);
            sendError("ILLEGAL_REQUEST");
            return false;
        }
    }

    public boolean sendResponse(Integer HTTPCode, Object payload) {
        if(this.RESPONSE_SENT) return false;

        try {
            JsonObject obj = main.GLOBAL_GSON.fromJson("{}", JsonObject.class);
            obj.addProperty("ID", this.REQUEST_ID);
            obj.addProperty("CODE", HTTPCode);
            obj.addProperty("DATA", main.GLOBAL_GSON.toJson(payload));

            this.socket.emit("WRV1_RESPONSE", main.GLOBAL_GSON.toJson(obj));
            this.RESPONSE_SENT = true;
            thisLogger.INFO("Response sent", 10);
        } catch(Exception ex) {
            thisLogger.ERROREXEP("Failed to response to Websocket Request", ex, 0);
        }

        return this.RESPONSE_SENT;
    }

    public void sendError(String message) {
        this.socket.emit("WRV1_ERROR", message);
    }

    public JsonObject getData() {
        return this.REQUEST_DATA;
    }

    public String getNamespace() {
        return this.REQUEST_NAMESPACE;
    }

    public String getMethod() {
        return this.REQUEST_METHOD;
    }

    public Integer getID() {
        return this.REQUEST_ID;
    }
}
