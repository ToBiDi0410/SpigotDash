package de.tobias.spigotdash.backend.io.socket;

import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import io.socket.socketio.server.SocketIoSocket;

import java.util.HashMap;
import java.util.Map;

public class WebsocketRequestManager {

    public static HashMap<String, WebsocketRequestListener> listeners = new HashMap<>();
    public static fieldLogger thisLogger = new fieldLogger("WEBSREQMAN", globalLogger.constructed);

    public static WebsocketEventReciever WEBSOCKET_REQUEST_RECIEVER = new WebsocketEventReciever() {
        @Override
        public boolean handle(String eventName, SocketIoSocket soc, Object... args) {
            fieldLogger socketLogger = thisLogger.subFromParent(soc.getId());

            socketLogger.INFO("Received Socket request", 15);
            WebsocketRequest req = new WebsocketRequest(args[0].toString(), new fieldLogger("WEBSREQ", globalLogger.constructed), soc);
            if(req.parsePayload()) {
                socketLogger = thisLogger.subFromParent(req.getID().toString());
                socketLogger.INFO("Payload seems valid", 15);
                socketLogger.INFO("Processor Name: " + req.getNamespace(), 20);
                socketLogger.INFO("Method Name: " + req.getMethod(), 20);
                for(Map.Entry<String, WebsocketRequestListener> listener : listeners.entrySet()) {
                    if(listener.getKey().equalsIgnoreCase(req.getNamespace())) {
                        socketLogger.INFO("Processor found! Will apply correct Method", 15);
                        if(req.getMethod().equalsIgnoreCase("GET")) return listener.getValue().DO_GET(req);
                        return false;
                    } else {
                        socketLogger.WARNING("Will ignore Processor: " + listener.getKey(), 40);
                    }
                }

                socketLogger.ERROR("Cannot find any Processors", 15);
                req.sendResponse(500, "INVALID_NAMESPACE_OR_METHOD");
            }
            return false;
        }
    };

    public static void registerListeners() {
        listeners.put("TEST", new WebsocketRequestListener() {
            @Override
            public boolean DO_GET(WebsocketRequest soc) {
                soc.sendResponse(200, "DAS GEHT DOCH NICHT");
                return false;
            }
        });
        thisLogger.INFO("Registered Listeners", 0);
    }
}
