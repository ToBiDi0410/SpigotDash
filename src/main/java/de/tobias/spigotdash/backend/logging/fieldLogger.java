package de.tobias.spigotdash.backend.logging;

import java.util.Locale;

public class fieldLogger {

    private final String fieldName;
    private final globalLogger l;

    private final String PREFIX;

    public fieldLogger(String field, globalLogger gbl) {
        this.fieldName = field;
        this.PREFIX = "&8[&5" + field + "&8] &r";
        this.l = gbl;
    }

    public fieldLogger(String field, String subfield, globalLogger gbl) {
        this.fieldName = field + " | " + subfield;
        this.PREFIX = "&8[&5" + fieldName + "&8] &r";
        this.l = gbl;
    }

    public fieldLogger subFromParent(String subfield) {
        return new fieldLogger(fieldName, subfield, l);
    }

    public void FIELD_INFO(String msg) {
        l.INFO(PREFIX + "&7" + msg);
    }

    public void FIELD_WARNING(String msg) {
        l.WARNING(PREFIX + "&e" + msg);
    }

    public void FIELD_ERROR(String msg) {
        l.ERROR(PREFIX + "&c" + msg);
    }

    public void FIELD_ERROREXEP(String msg, Exception ex) {
        l.ERROREXEP(PREFIX + "&c" + msg, ex);
    }

    private boolean shouldDebug() {
        return (l.getDebugFields().contains(this.fieldName) || l.getDebugFields().contains("*")) && !l.getDebugFields().contains("!" + this.fieldName.toUpperCase(Locale.ROOT));
    }

    public void INFO(String msg, Integer LEVEL) {
        if(LEVEL > 0) {
            if(l.shouldDebug() && l.getDebugLevel() >= LEVEL) {
                if (shouldDebug()) {
                    FIELD_INFO("&8[&dDEBUG&8] &7" + msg);
                }
            }
        } else {
            FIELD_INFO(msg);
        }
    }

    public void WARNING(String msg, Integer LEVEL) {
        if(LEVEL > 0) {
            if(l.shouldDebug() && l.getDebugLevel() >= LEVEL) {
                if (shouldDebug()) {
                    FIELD_WARNING("&8[&dDEBUG&8] &e" + msg);
                }
            }
        } else {
            FIELD_WARNING(msg);
        }
    }

    public void ERROR(String msg, Integer LEVEL) {
        if(LEVEL > 0) {
            if(l.shouldDebug() && l.getDebugLevel() >= LEVEL) {
                if (shouldDebug()) {
                    FIELD_ERROR("&8[&dDEBUG&8] &c" + msg);
                }
            }
        } else {
            FIELD_ERROR(msg);
        }
    }

    public void ERROREXEP(String msg, Exception ex, Integer LEVEL) {
        if(LEVEL > 0) {
            if(l.shouldDebug() && l.getDebugLevel() >= LEVEL) {
                if (shouldDebug()) {
                    FIELD_ERROREXEP("&8[&dDEBUG&8] &c" + msg, ex);
                }
            }
        } else {
            FIELD_ERROREXEP(msg, ex);
        }
    }
}
