package de.tobias.spigotdash.web.jetty;

import com.google.common.io.Resources;
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

        String classpath = "/www" + path;
        URL res = getClass().getResource(classpath);

        if (res == null) response.sendRedirect("404.html");
        res = getClass().getResource(classpath);

        OutputStream outputStream = response.getOutputStream();
        String extension = FilenameUtils.getExtension(res.getPath());

        byte[] fileContentBytes;
        if(extension.equalsIgnoreCase("html") || extension.equalsIgnoreCase("js") || extension.equalsIgnoreCase("css")) {
            String fileContent = Resources.toString(res, StandardCharsets.UTF_8);
            fileContent = translations.replaceTranslationsInString(fileContent);
            fileContentBytes = fileContent.getBytes(StandardCharsets.UTF_8);
        } else {
            fileContentBytes = Resources.toByteArray(res);
        }
        response.setStatus(HttpServletResponse.SC_OK);
        outputStream.write(fileContentBytes);
        outputStream.flush();
        outputStream.close();
    }
}