package de.tobias.spigotdash.web;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;

public class EncryptionManager {
	
	public static Map<String, KeyPair> authKeys = new HashMap<String, KeyPair>();
	public static JsonParser parser = new JsonParser();
	
	public static JsonObject decryptRequest(HttpExchange he, JsonObject json) {
		if(json.has("DATA") && json.has("PAIR_ID")) {
			String data = json.get("DATA").getAsString();
			byte[] dataBytes = Base64.getDecoder().decode(data);
			KeyPair pair = authKeys.get(json.get("PAIR_ID").getAsString());
			
			if(pair != null) {
				try {
					Cipher cipher = Cipher.getInstance("RSA");
				    cipher.init(Cipher.DECRYPT_MODE, pair.getPrivate());
				    String decryptedData = new String(cipher.doFinal(dataBytes));
				    //System.out.println(decryptedData);
				    json = parser.parse(decryptedData).getAsJsonObject();
				    return json;
				} catch (Exception ex) {
					MainRequestHandler.sendJSONResponse(he, 401, "ERR_DECRYPT_JSON_INVALID");
					return null;
				}

			} else {
				MainRequestHandler.sendJSONResponse(he, 401, "ERR_PAIRID_NOT_EXISTENT");
				return null;
			}
		} else {
			return json;
		}
	}
	
	public static Map<String, String> generateAuthKey() {
		try {
			KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
	        generator.initialize(1024*2, new SecureRandom());
	        KeyPair pair = generator.generateKeyPair();  
	        String id = UUID.randomUUID().toString();
	        authKeys.put(id, pair);
	        String publickey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
	        return Map.of(
	        		"ID", id,
	        		"KEY", publickey
	        );
	        
		} catch(Exception ex) {
			return null;
		}   
		
	}
	
	public static boolean handleKeyRequest(HttpExchange he) {
		MainRequestHandler.sendJSONResponse(he, 200, generateAuthKey());
		return true;
	}
	
}
