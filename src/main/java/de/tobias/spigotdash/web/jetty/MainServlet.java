package de.tobias.spigotdash.web.jetty;

import com.google.common.io.Resources;
import de.tobias.spigotdash.main;
import de.tobias.spigotdash.utils.files.translations;
import org.apache.commons.io.FilenameUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getRequestURI().replace("/web", "");
        if (path.equalsIgnoreCase("/")) {
            path = "/index.html";
        }

        byte[] content = main.webroot.getBytesOfFile(path);
        if(content == null) {
            response.sendRedirect("404.html");
            return;
        }

        OutputStream outputStream = response.getOutputStream();
        String extension = FilenameUtils.getExtension(path);

        if(extension.equalsIgnoreCase("html") || extension.equalsIgnoreCase("js") || extension.equalsIgnoreCase("css")) {
            String fileContent = new String(content, StandardCharsets.UTF_8);
            fileContent = translations.replaceTranslationsInString(fileContent);
            content = fileContent.getBytes(StandardCharsets.UTF_8);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        outputStream.write(content);
        outputStream.flush();
        outputStream.close();
    }
}