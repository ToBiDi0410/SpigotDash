package de.tobias.spigotdash.backend.io.socket;

import com.google.gson.JsonObject;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import de.tobias.spigotdash.backend.utils.RSACryptor;
import io.socket.socketio.server.SocketIoSocket;

import java.util.HashMap;
import java.util.Map;

public class WebsocketRequestV1Handler implements WebsocketEventReciever {

    /*
    Basic Structure:
        ARGS: 4
            0. ID --> Int
            1. NAMESPACE --> String
            2. PAYLOAD --> String (JSON encoded)
            3. RECEIVE-ENCRYPTION-SET-UUID --> String
            4. RESPONSE-ENCRYPTION-SET-UUID --> String
            5. CALLBACK --> Function

     */

    public static final fieldLogger thisLogger = new fieldLogger("SOC-REQ1-HANDLER", globalLogger.constructed);
    public static final HashMap<String, subHandler> subHandlers = new HashMap<>();

    @Override
    public boolean handle(String eventName, SocketIoSocket soc, Object... args) {
        Integer ID = Integer.parseInt(args[0].toString());
        fieldLogger requestLogger = thisLogger.subFromParent(ID.toString());

        requestLogger.INFO("Request Received", 5);

        if(args.length == 6) {
            WebsocketRequestV1Response resp = new WebsocketRequestV1Response(ID, soc, (SocketIoSocket.ReceivedByLocalAcknowledgementCallback) args[5]);

            if(args[4] != null) {
                requestLogger.INFO("Response should be encrypted", 5);
                resp.setEncryptionPair(args[4].toString());
            }
            String NAMESPACE = args[1].toString();
            String PAYLOAD = args[2].toString();

            requestLogger.INFO("Searching Handler...", 5);
            for(Map.Entry<String, subHandler> handlerEntry : subHandlers.entrySet()) {
                if(handlerEntry.getKey().equalsIgnoreCase(NAMESPACE)) {
                    requestLogger.INFO("Found Handler", 10);

                    JsonObject tree;
                    if(args[3] != null) {
                        String ENCRYPT_SET = args[3].toString();

                        requestLogger.INFO("Payload seems to be Encrypted! Decrypting...", 10);
                        String decoded = RSACryptor.decodeString(ENCRYPT_SET, PAYLOAD);
                        if(decoded == null) {
                            requestLogger.ERROR("Failed to Decrypt Payload", 5);
                            return !resp.setCode(400).setData("INVALID_ENCRYPTION_KEY").send();
                        }
                        requestLogger.INFO("Decryption successful", 15);
                        tree = GlobalVariableStore.GSON.fromJson(decoded, JsonObject.class);
                    } else {
                        tree = GlobalVariableStore.GSON.fromJson(PAYLOAD, JsonObject.class);
                    }

                    requestLogger.INFO("JSON Parsed", 10);
                    handlerEntry.getValue().handle(resp, tree);
                    requestLogger.INFO("Handler is done", 10);
                }
            }

            return !resp.setCode(500).setData("NO_RESPONSE").send();
        } else {
            requestLogger.ERROR("Illegal Request: ARGUMENTS exceeded range (5)", 5);
            return false;
        }
    }

    public interface subHandler {
        void handle(WebsocketRequestV1Response res, JsonObject data);
    }
}
