package de.tobias.spigotdash.backend.io.http;

import de.tobias.spigotdash.backend.io.http.HttpServerManager;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import de.tobias.spigotdash.main;
import org.apache.commons.io.IOUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

public class WebresourceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        fieldLogger thisLogger = HttpServerManager.constructed.getFieldLogger();
        thisLogger.INFO("Request: " + req.getRequestURI(), 10);

        File staticFolder = new File(GlobalVariableStore.pl.getDataFolder(), "www");
        File requested = new File(staticFolder, req.getRequestURI());

        if(requested.isDirectory()) requested = new File(requested, "index.html");

        if(!requested.exists()) {
            thisLogger.WARNING("Not found: " + req.getRequestURI(), 20);
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        InputStream in = null;
        OutputStream out;
        try {
            String filetype = "text/plain";

            if(requested.getName().endsWith(".html")) filetype = "text/html";
            if(requested.getName().endsWith(".js")) filetype = "text/javascript";
            if(requested.getName().endsWith(".css")) filetype = "text/css";
            if(requested.getName().endsWith(".png")) filetype = "image/png";
            if(requested.getName().endsWith(".ico")) filetype = "image/vnd.microsoft.icon";
            res.setContentType(filetype);
            res.setStatus(HttpServletResponse.SC_OK);

            in = new FileInputStream(requested);
            out = res.getOutputStream();
            IOUtils.copy(in, out);
            thisLogger.INFO("Transmitted: " + requested.getName() + " (" + filetype + ")", 20);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
