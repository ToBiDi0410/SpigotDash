package de.tobias.spigotdash.models;

import java.util.Date;

public class DataPoint {

    public Date TIME;
    public Object DATA;

    public DataPoint(Object data) {
        this.TIME = new Date();
        this.DATA = data;
    }
}
