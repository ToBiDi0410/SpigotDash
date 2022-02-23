package de.tobias.spigotdash.backend.io.WebsocketRequestHandlers;

import de.tobias.spigotdash.backend.io.socket.RSAEncryptor;
import de.tobias.spigotdash.backend.io.socket.WebsocketRequestV1Handler;

import java.util.Base64;
import java.util.HashMap;

public class EncryptionRequestHandler {

    public static final WebsocketRequestV1Handler.subHandler handler = (res, data) -> {
        if (data.has("SUBMETHOD")) {
            String subMethod = data.get("SUBMETHOD").getAsString();
            if (subMethod.equalsIgnoreCase("GET_PAIR")) {
                HashMap<String, Object> values = new HashMap<>();
                String generatedSetID = RSAEncryptor.generateOwnSet();
                values.put("PAIR_ID", generatedSetID);
                String publicKeyString = "-----BEGIN RSA PUBLIC KEY-----\n" + Base64.getEncoder().encodeToString(RSAEncryptor.getSetPublicKey(generatedSetID).getEncoded()) + "\n-----END RSA PUBLIC KEY-----\n";
                values.put("PAIR_PUBLIC", publicKeyString);

                res.setCode(200).setData(values).send();
                return;
            }

            if (subMethod.equalsIgnoreCase("SUGGEST_PAIR")) {
                if (!data.has("PAIR_ID")) {
                    res.setCode(400).setData("NO_PAIR_ID").send();
                    return;
                }
                if (!data.has("PAIR_PUBLIC")) {
                    res.setCode(400).setData("NO_PAIR_PUBLIC").send();
                    return;
                }

                RSAEncryptor.addOtherSetWithPublicKey(data.get("PAIR_ID").getAsString(), data.get("PAIR_PUBLIC").getAsString());
                res.setCode(200).setData("DONE").send();
            }
        } else {
            res.setCode(400).setData("NO_SUBMETHOD").send();
        }
    };
}
