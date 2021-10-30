package de.tobias.spigotdash.web.jetty;

import com.google.common.io.Resources;
import de.tobias.spigotdash.main;
import de.tobias.spigotdash.utils.errorCatcher;
import de.tobias.spigotdash.utils.pluginConsole;
import org.eclipse.jetty.util.resource.Resource;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class WebServerFileRoot {

    public String type;
    public Object base;

    public WebServerFileRoot(String type, Object base) {
        this.type = type;
        this.base = base;
    }

    public byte[] getBytesOfFile(String path) {
        pluginConsole.sendMessage("REQ: " + path);
        try {
            if(this.type.equalsIgnoreCase("FILE")) {
                File f = (File) base;
                File requested = new File(f, path);
                byte[] bytes = Files.readAllBytes(Path.of(requested.getPath()));
                return bytes;
            } else {
                String respath = (String) base + path;
                URL res = main.pl.getClass().getResource(respath);
                return Resources.toByteArray(res);
            }
        } catch(Exception ex) {
            pluginConsole.sendMessage("Failed to read requested Webfile: " + path);
            //errorCatcher.catchException(ex, false);
            return null;
        }

    }
}
