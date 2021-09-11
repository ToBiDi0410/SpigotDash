package de.tobias.spigotdash.web;

import java.net.HttpCookie;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;

import de.tobias.spigotdash.utils.configuration;
import de.tobias.spigotdash.utils.errorCatcher;

public class AuthHandler {

	public static Map<String, Object> sessionData = new HashMap<String, Object>();
	public static Map<String, KeyPair> authKeys = new HashMap<String, KeyPair>();
	
	public static void handle(HttpExchange he, JsonObject json) {
		try {
			if (!isAuthed(he)) {
				if (json.has("username") && json.has("password")) {
					if (isValid(json.get("username").getAsString(), json.get("password").getAsString())) {
						String cok = generateNewCookie().toString();
						he.getResponseHeaders().add("Set-Cookie", cok);
						MainRequestHandler.sendJSONResponse(he, 200, cok);
						return;
					} else {
						MainRequestHandler.sendJSONResponse(he, 400, "ERR_WRONG_NAME_OR_PASSWORD");
						return;
					}
				} else {
					MainRequestHandler.sendJSONResponse(he, 400, "ERR_MISSING_NAME_OR_PASSWORD");
					return;
				}
			} else {
				MainRequestHandler.sendJSONResponse(he, 200, "WARN_ALREADY_AUTHED");
			}
		} catch (Exception ex) {
			errorCatcher.catchException(ex, false);
		}
	}

	public static String hashPassword(String password) {
		return "NOT_IMPLEMENTED";
	}
	
	public static boolean addAccount(String username, String password) {
		return true;
	}
	
	public static boolean isValid(String username, String password) {
		username = username.toLowerCase();
		if(username.equalsIgnoreCase("admin") && password.equals(configuration.CFG.get("WEB_PASSWORD").toString())) {
			return true;
		}
		return false;
	}
	
	public static HttpCookie generateNewCookie() {
		String newID = UUID.randomUUID().toString();
		HttpCookie sessionCookie = new HttpCookie("sessionId", newID);
		sessionData.put(newID, "TEST");
		return sessionCookie;
	}
	
	public static boolean isAuthed(HttpExchange he) {
		HttpCookie sessionCookie = getSessionCookie(he);		
		if(sessionCookie != null) {
			if(sessionData.containsKey(sessionCookie.getValue())) {
				return true;
			}
		}
		return false;
	}
	
	public static HttpCookie getSessionCookie(HttpExchange he) {
		return getCookieByName(he, "sessionId");
	}
	
	public static HttpCookie getCookieByName(HttpExchange he, String name) {
		if(!he.getRequestHeaders().containsKey("Cookie")) return null;
		//pluginConsole.sendMessage("Cookie Header found!");
		for(String cookieHeader : he.getRequestHeaders().get("Cookie")) {
			//pluginConsole.sendMessage("Parsing Cookie Header: " + cookieHeader);
			String[] cookieParts = cookieHeader.split(";");
			for(String cookieString : cookieParts) {
				HttpCookie cok = HttpCookie.parse(cookieString).get(0);
				//pluginConsole.sendMessage("Checking Cookie: " + cok);
				if(cok.getName().equalsIgnoreCase(name)) {
					return cok;
				}
			}
		}
		return null;
	}
}
