package de.tobias.spigotdash.web.jetty;

import de.tobias.spigotdash.main;
import de.tobias.spigotdash.utils.pluginConsole;
import org.bukkit.Bukkit;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;

public class FileUploadServlet extends HttpServlet {

    public static HashMap<String, Part> cachedFiles = new HashMap<String, Part>();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String respS;

            Part file = req.getPart("FILE");
            if(file != null) {
                String uid = UUID.randomUUID().toString();
                cachedFiles.put(uid, file);

                resp.setStatus(HttpServletResponse.SC_OK);
                respS = "{\"ID\":\"" + uid + "\"}";

                Bukkit.getScheduler().runTaskLater(main.pl, new Runnable() {
                    @Override
                    public void run() {
                        if(cachedFiles.containsKey(uid)) {
                            cachedFiles.remove(uid);
                            pluginConsole.sendMessage("&6Uploaded File was not authorized fast enough! Deleted the cached Content!");
                        }
                    }
                }, 20*10);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                respS = "{}";
            }

            resp.setContentLength(respS.getBytes(StandardCharsets.UTF_8).length);
            resp.getOutputStream().write(respS.getBytes(StandardCharsets.UTF_8));
            resp.getOutputStream().close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

    }
}
