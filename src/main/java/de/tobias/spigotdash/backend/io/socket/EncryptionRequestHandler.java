package de.tobias.spigotdash.backend.io.socket;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class EncryptionRequestHandler {

    public static WebsocketRequestV1Handler.subHandler handler = (res, data) -> {
        if (data.has("SUBMETHOD")) {
            String subMethod = data.get("SUBMETHOD").getAsString();
            if (subMethod.equalsIgnoreCase("GET_PAIR")) {
                HashMap<String, String> values = new HashMap<>();
                String generatedSetID = RSAEncryptor.generateOwnSet();
                values.put("PAIR_ID", generatedSetID);
                values.put("PAIR_PUBLIC", Base64.encode(RSAEncryptor.getSetPublicKey(generatedSetID).toString().getBytes(StandardCharsets.UTF_8)));

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
