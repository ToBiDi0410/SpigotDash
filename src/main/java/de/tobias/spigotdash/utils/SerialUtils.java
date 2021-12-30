package de.tobias.spigotdash.utils;

import com.google.gson.*;

public class SerialUtils {

// FROM: https://stackoverflow.com/questions/869033/how-do-i-copy-an-object-in-java

    public static String serializeObject(Object o) {
        Gson gson = new Gson();
        String serializedObject = gson.toJson(o);
        return serializedObject;
    }

    public static Object unserializeObject(String s, Object o){
        Gson gson = new Gson();
        Object object = gson.fromJson(s, o.getClass());
        return object;
    }

    public static Object cloneObject(Object o){
        String s = serializeObject(o);
        Object object = unserializeObject(s,o);
        return object;
    }
}