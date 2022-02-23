package de.tobias.spigotdash.backend.storage;

import com.google.gson.Gson;
import de.tobias.spigotdash.backend.logging.fieldLogger;
import de.tobias.spigotdash.backend.logging.globalLogger;
import de.tobias.spigotdash.backend.utils.GlobalVariableStore;
import org.apache.commons.io.FileUtils;

import java.io.File;

@SuppressWarnings("rawtypes, unchecked")
public class JavaObjectJsonStore {

    private Object obj;
    private final Class obj_class;
    private final Gson gson;

    private final File file;
    private final fieldLogger thisLogger;

    public JavaObjectJsonStore(Class t, File file) {
        this.obj = null;
        this.obj_class = t;
        this.file = file;
        this.gson = GlobalVariableStore.GSON;
        this.thisLogger = new fieldLogger("JavaObjStore", file.getName(), globalLogger.constructed);
    }

    public Object getObject() {
        try {
            return obj_class.cast(this.obj);
        } catch(Exception ex) {
            thisLogger.ERROREXEP("Current Object is not valid: ", ex, 0);
            return null;
        }
    }

    public boolean loadOrCreate() {
        thisLogger.INFO("Reading File...", 0);
        if(file.exists()) {
            thisLogger.INFO("File exists! Reading...", 10);
            try {
                obj = gson.fromJson(FileUtils.readFileToString(file, GlobalVariableStore.CHARSET), obj_class);
                thisLogger.INFO("Read successfully", 0);
                return true;
            } catch (Exception ex) {
                thisLogger.ERROREXEP("Cannot read the existing JSON File: ", ex, 0);
                return createFile();
            }
        } else {
            thisLogger.WARNING("File not found", 0);
            return createFile();
        }
    }

    public boolean createFile() {
        thisLogger.WARNING("Creating new default File...", 0);
        try {
            Object newDefault = obj_class.getConstructors()[0].newInstance();
            thisLogger.INFO("Constructed empty Object", 20);
            this.obj = newDefault;
            this.save();
            return true;
        } catch (Exception ex) {
            thisLogger.ERROREXEP("Failed to create new File: ", ex, 0);
            return false;
        }
    }

    public boolean save() {
        thisLogger.WARNING("Saving File...", 0);
        try {
            FileUtils.write(this.file, GlobalVariableStore.GSON.toJson(this.obj, obj_class), GlobalVariableStore.CHARSET);
            thisLogger.INFO("Wrote the current Object to File", 20);
            return true;
        } catch (Exception ex) {
            thisLogger.ERROREXEP("Cannot create new File: ", ex, 0);
            return false;
        }
    }
}
