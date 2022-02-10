package de.tobias.spigotdash.backend.logging;

public class fieldLogger {

    private final String fieldName;
    private final globalLogger l;

    private final String PREFIX;

    public fieldLogger(String field, globalLogger gbl) {
        this.fieldName = field;
        this.PREFIX = "&8[&5" + field + "&8] &r";
        this.l = gbl;
    }

    public void FIELD_INFO(String msg) {
        l.INFO(PREFIX + msg);
    }

    public void FIELD_WARNING(String msg) {
        l.WARNING(PREFIX + msg);
    }

    public void FIELD_ERROR(String msg) {
        l.ERROR(PREFIX + msg);
    }

    public void INFO(String msg, Integer LEVEL) {
        if(LEVEL > 0) {
            if(l.shouldDebug() && l.getDebugLevel() >= LEVEL) {
                System.out.println(LEVEL);
                System.out.println(l.getDebugLevel());
                if (l.getDebugFields().contains(this.fieldName) || l.getDebugFields().contains("*")) {
                    FIELD_INFO("[DEBUG] " + msg);
                }
            }
        } else {
            FIELD_INFO(msg);
        }
    }

    public void WARNING(String msg, Integer LEVEL) {
        if(LEVEL > 0) {
            if(l.shouldDebug() && l.getDebugLevel() >= LEVEL) {
                if (l.getDebugFields().contains(this.fieldName) || l.getDebugFields().contains("*")) {
                    FIELD_WARNING("[DEBUG] " + msg);
                }
            }
        } else {
            FIELD_WARNING(msg);
        }
    }

    public void ERROR(String msg, Integer LEVEL) {
        if(LEVEL > 0) {
            if(l.shouldDebug() && l.getDebugLevel() >= LEVEL) {
                if (l.getDebugFields().contains(this.fieldName) || l.getDebugFields().contains("*")) {
                    FIELD_ERROR("[DEBUG] " + msg);
                }
            }
        } else {
            FIELD_ERROR(msg);
        }
    }
}
