package de.tobias.spigotdash.backend.io.socket;

import io.socket.socketio.server.SocketIoSocket;

public class WebsocketRequestV1Response {

/*
    Basic Structure:
        ARGS: 3
            0. ID --> Integer
            1. CODE --> Integer
            2. DATA --> String (JSON encoded)
     */

    private Integer ID;
    private Integer CODE;
    private Object DATA;

    private boolean wasSent = false;
    private SocketIoSocket.ReceivedByLocalAcknowledgementCallback callback;

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

        callback.sendAcknowledgement(ID, CODE, DATA);

        this.wasSent = true;
        return true;
    }
}
