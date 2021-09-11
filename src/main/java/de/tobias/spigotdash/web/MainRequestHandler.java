package de.tobias.spigotdash.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.io.Resources;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import de.tobias.spigotdash.utils.errorCatcher;
import de.tobias.spigotdash.utils.pluginConsole;
import de.tobias.spigotdash.utils.translations;
import org.apache.commons.io.FilenameUtils;

public class MainRequestHandler implements HttpHandler {
	
	@Override
	public void handle(HttpExchange he) {
		try {
			if (addCorsHeaders(he) && handleWithSections(he)) {
				String path = he.getRequestURI().getPath();
				if (path.equalsIgnoreCase("/")) {
					path = "/index.html";
				}

				String classpath = "/www" + path;
				URL res = getClass().getResource(classpath);

				if (res == null) {
					classpath = "/www/404.html";
				}
				res = getClass().getResource(classpath);
				
				OutputStream outputStream = he.getResponseBody();
				String extension = FilenameUtils.getExtension(res.getPath().toString());

				if(extension.equalsIgnoreCase("html") || extension.equalsIgnoreCase("js")) {
					String fileContent = Resources.toString(res, StandardCharsets.UTF_8);
					fileContent = translations.replaceTranslationsInString(fileContent);
					byte[] fileContentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
					he.sendResponseHeaders(200, fileContentBytes.length);
					outputStream.write(fileContentBytes);
				} else {
					File f = new File(res.toExternalForm());
					he.sendResponseHeaders(200, f.length());
					getClass().getResourceAsStream(classpath).transferTo(outputStream);
				}
				
				outputStream.flush();
				outputStream.close();
			}
		} catch (Exception ex) {
			errorCatcher.catchException(ex, false);
		}
	}
	
	public boolean handleWithSections(HttpExchange he) {
		String path = he.getRequestURI().getPath();
		String request_body = castInputStreamToString(he.getRequestBody());
		JsonObject json;
		
		if(path.equalsIgnoreCase("/encryptKey")) {
			EncryptionManager.handleKeyRequest(he);
			return false;
		}
		
		if(path.equalsIgnoreCase("/bundledCSS")) {
			try {
				OutputStream outputStream = he.getResponseBody();
				String result = webBundler.bundleDefaultCSS();
				byte[] content = result.getBytes(StandardCharsets.UTF_8);
				he.sendResponseHeaders(200, content.length);
				outputStream.write(content);
				outputStream.flush();
				outputStream.close();
				return false;
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if(path.equalsIgnoreCase("/bundledJS")) {
			try {
				OutputStream outputStream = he.getResponseBody();
				String result = webBundler.bundleDefaultJS();
				byte[] content = result.getBytes(StandardCharsets.UTF_8);
				he.sendResponseHeaders(200, content.length);
				outputStream.write(content);
				outputStream.flush();
				outputStream.close();
				return false;
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if(path.equalsIgnoreCase("/bundledPage")) {
			Map<String, String> query = queryToMap(he.getRequestURI().getQuery());
			if(query != null && query.containsKey("page")) {
				sendJSONResponse(he, 200, webBundler.getBundledPage(query.get("page")));
			} else {
				sendJSONResponse(he, 400, "ERR_MISSING_PAGE");
			}
			return false;
		}
		
		try {
			JsonElement tempjson = new JsonParser().parse(request_body);
			if(tempjson == null || !tempjson.isJsonObject()) return true;
			json = tempjson.getAsJsonObject();
		} catch(Exception ex) {
			ex.printStackTrace();
			pluginConsole.sendMessage("&cFailed to parse JSON from Request: \n&b" + request_body);
			errorCatcher.transmitError("MainRequestHandler.java", 81 , "JSON Parse failed: " + request_body);
			return false;
		}

		
		if(request_body == null || json == null) return true;
		
		//DATA DECRYPTION
		json = EncryptionManager.decryptRequest(he, json);
		if(json == null) return false;
		
		if(path.equalsIgnoreCase("/api")) {
			if(!AuthHandler.isAuthed(he)) {
				sendJSONResponse(he, 401, "ERR_REQUIRE_AUTH");
				return false;
			}
			APIHandler.handle(he, json);
			return false;
		}
		
		if(path.equalsIgnoreCase("/auth")) {
			AuthHandler.handle(he, json);
			return false;
		}
		
		return true;
	}

	public boolean addCorsHeaders(HttpExchange he) {
		try {
			he.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			if (he.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
				he.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS, POST");
				he.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
				he.sendResponseHeaders(204, -1);
				return false;
			}
			return true;
		} catch (Exception ex) {
			pluginConsole.sendMessage("&c[ERROR] Failed to add/mange CORS Headers to/in Response:");
			errorCatcher.catchException(ex, false);
			he.close();
			return false;
		}
	}
	
	public static String castInputStreamToString(InputStream ios) {
		try {
			 InputStreamReader isr = new InputStreamReader(ios, StandardCharsets.UTF_8);
		     BufferedReader br = new BufferedReader(isr);
		     String text = br.lines().collect(Collectors.joining("\n"));
		     return text;
		} catch (Exception ex) {
			pluginConsole.sendMessage("&c[ERROR] Failed to read InputStream into String:");
			errorCatcher.catchException(ex, false);
			return null;
		}
	}
	
	public static void sendJSONResponse(HttpExchange he, Integer code, Object data) {
		try {
			String response_string = new GsonBuilder().serializeNulls().create().toJson(data);
			byte[] message_bytes = response_string.getBytes(StandardCharsets.UTF_8);
			he.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
			he.sendResponseHeaders(code, message_bytes.length);
			OutputStream outputStream = he.getResponseBody();
			outputStream.write(message_bytes);
			outputStream.close();
		} catch (Exception ex) {
			pluginConsole.sendMessage("&c[ERROR] Failed to send JSON Response:");
			errorCatcher.catchException(ex, false);
			he.close();
		}
	}
	
	public static void sendFileResponse(HttpExchange he, File f, Integer code) {
		try {
			he.sendResponseHeaders(200, f.length());
			OutputStream outputStream = he.getResponseBody();
			FileInputStream fIn = new FileInputStream(f);
			fIn.transferTo(outputStream);
			fIn.close();
			outputStream.close();
		} catch (Exception ex) {
			he.close();
			errorCatcher.catchException(ex, false);
		}
	}
	
	public Map<String, String> queryToMap(String query) {
	    if(query == null) {
	        return null;
	    }
	    Map<String, String> result = new HashMap<>();
	    for (String param : query.split("&")) {
	        String[] entry = param.split("=");
	        if (entry.length > 1) {
	            result.put(entry[0], entry[1]);
	        }else{
	            result.put(entry[0], "");
	        }
	    }
	    return result;
	}

}
