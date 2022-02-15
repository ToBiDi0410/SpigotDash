package de.tobias.spigotdash.backend.io.socket;

import com.google.gson.JsonObject;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import de.tobias.spigotdash.main;
import io.socket.socketio.server.SocketIoSocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WebsocketRequestV1Handler implements WebsocketEventReciever {

    /*
    Basic Structure:
        ARGS: 4
            0. ID --> Int
            1. NAMESPACE --> String
            2. PAYLOAD --> String (JSON encoded)
            3. CALLBACK --> Function
     */

    public static fieldLogger thisLogger = new fieldLogger("SOCREQ1H", globalLogger.constructed);
    public static HashMap<String, subHandler> subHandlers = new HashMap<>();

    @Override
    public boolean handle(String eventName, SocketIoSocket soc, Object... args) {
        Integer ID = Integer.parseInt(args[0].toString());
        fieldLogger requestLogger = thisLogger.subFromParent(ID.toString());

        requestLogger.INFO("Request Received", 0);

        if(args.length == 4) {
            WebsocketRequestV1Response resp = new WebsocketRequestV1Response(ID, (SocketIoSocket.ReceivedByLocalAcknowledgementCallback) args[3]);
            String NAMESPACE = args[1].toString();
            String PAYLOAD = args[2].toString();

            requestLogger.INFO("Searching Handler...", 0);
            for(Map.Entry<String, subHandler> handlerEntry : subHandlers.entrySet()) {
                if(handlerEntry.getKey().equalsIgnoreCase(NAMESPACE)) {
                    requestLogger.INFO("Found Handler", 10);
                    JsonObject tree = main.GLOBAL_GSON.fromJson(PAYLOAD, JsonObject.class);
                    requestLogger.INFO("JSON Parsed", 10);
                    handlerEntry.getValue().handle(resp, tree);
                    requestLogger.INFO("Handler is done", 10);
                }
            }

            return !resp.setCode(500).setData("NO_RESPONSE").send();
        } else {
            requestLogger.ERROR("Illegal Request: ARGUMENTS exceeded range", 5);
            return false;
        }
    }

    public interface subHandler {
        void handle(WebsocketRequestV1Response res, JsonObject data);
    }
}
