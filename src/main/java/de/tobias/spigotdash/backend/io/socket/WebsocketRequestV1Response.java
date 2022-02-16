package de.tobias.spigotdash.backend.io.socket;

import de.tobias.spigotdash.main;
import io.socket.socketio.server.SocketIoSocket;

public class WebsocketRequestV1Response {

/*
    Basic Structure:
        ARGS: 3
            0. ID --> Integer
            1. CODE --> Integer
            2. DATA --> String (JSON encoded)
     */

    private final Integer ID;
    private Integer CODE;
    private Object DATA;

    private boolean wasSent = false;
    private final SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback;

    public WebsocketRequestV1Response(Integer id, SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback) {
        this.callback = callback;
        this.ID = id;
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

        System.out.println(callback);
        callback.sendAcknowledgement(ID, CODE, main.GLOBAL_GSON.toJson(DATA));

        this.wasSent = true;
        return true;
    }
}
