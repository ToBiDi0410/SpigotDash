package de.tobias.spigotdash.backend.io.socket;

import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import de.tobias.spigotdash.backend.utils.RSACryptor;
import io.socket.socketio.server.SocketIoSocket;

public class WebsocketRequestV1Response {

/*
    Basic Structure:
        ARGS: 3
            0. ID --> Integer
            1. CODE --> Integer
            2. DATA --> String (JSON encoded)
     */

    private final SocketIoSocket soc;
    private final Integer ID;
    private Integer CODE;
    private Object DATA;

    private String ENCRYPTION_PAIR_UUID;

    private boolean wasSent = false;
    private final SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback;
    private final fieldLogger thisLogger;

    public WebsocketRequestV1Response(Integer id, SocketIoSocket soc, SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback) {
        this.callback = callback;
        this.ID = id;
        this.soc = soc;
        this.thisLogger = (new fieldLogger("SOC-REQ1-RESPONSE", globalLogger.constructed)).subFromParent(id.toString());
    }

    public boolean hasBeenFilled() {
        return (CODE != null && DATA != null);
    }

    public WebsocketRequestV1Response setCode(Integer HTTPCode) {
        this.CODE = HTTPCode;
        return this;
    }

    public WebsocketRequestV1Response setData(Object PAYLOAD) {
        this.DATA = PAYLOAD;
        return this;
    }

    public boolean send() {
        if(this.wasSent) return false;
        if(this.callback == null) return false;

        String STRING_DATA = GlobalVariableStore.GSON.toJson(DATA);
        if(ENCRYPTION_PAIR_UUID != null && RSACryptor.getSetPublicKey(ENCRYPTION_PAIR_UUID) != null) {
            STRING_DATA = RSACryptor.encryptStringToBase64(ENCRYPTION_PAIR_UUID, STRING_DATA);
        }

        callback.sendAcknowledgement(ID, CODE, STRING_DATA);

        this.wasSent = true;
        return true;
    }

    public void setEncryptionPair(String uuid) {
        ENCRYPTION_PAIR_UUID = uuid;
    }

    public boolean respondWithPermissionError(String permissionName) {
        boolean hasPerm = false;
        thisLogger.INFO("Checking for Permission: " + permissionName, 20);
        if(SocketDataStore.hasUserData(soc) && permissionName != null && SocketDataStore.getUser(soc).getPermissionSet().containsKey(permissionName)) {
            hasPerm = SocketDataStore.getUser(soc).getPermissionSet().get(permissionName);
            thisLogger.INFO("Found Permission in Set, will use this value", 20);
        }

        if(!hasPerm) {
            thisLogger.WARNING("Permission result is false!", 20);
            thisLogger.WARNING("If this appears very often, you may be under attack or your Plugin is misconfigurated", 20);
            this.setData("NO_PERMISSION").setCode(401).send();
            return true;
        } else {
            return false;
        }

    }

    public SocketIoSocket getSocket() {
        return this.soc;
    }
}
