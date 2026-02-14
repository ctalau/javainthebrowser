package gwtjava.util;

public class ResourceBundle {

    private final String name;
    public ResourceBundle(String name) {
        this.name = name;
    }

    public static ResourceBundle getBundle(String name, Locale l) {
        return new ResourceBundle(name);
    }

    public static ResourceBundle getBundle(String name) {
        return new ResourceBundle(name);
    }

    public String getString(String key) {
        return "ResourceBundle("+name+").valueOf("+key+")";

    }

}
