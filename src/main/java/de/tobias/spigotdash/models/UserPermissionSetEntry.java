package de.tobias.spigotdash.models;

public class UserPermissionSetEntry {

    private final String name;
    private final String description;

    private final Integer weight;
    private Object value;

    public UserPermissionSetEntry(String pName, String pDescription) {
        this.name = pName;
        this.description = pDescription;
        this.weight = 1;
        this.value = false;
    }

    public void setValue(Object val) throws Exception {
        if(val instanceof Boolean) {
            this.value = val;
        } else if(val == null) {
            this.value = null;
        } else {
            throw new Exception("Illegal Parameter: Value is not Null and no Boolean");
        }
    }

    public String getName() { return this.name; }
    public String getDescription() { return this.description; }
    public Integer getWeight() { return this.weight; }
    public Boolean isSet() {
        return (value instanceof Boolean);
    }
    public Boolean hasPermission() {
        return (Boolean) value;
    }
}
