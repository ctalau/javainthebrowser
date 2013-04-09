package gwtjava.util.jar;


public class Attributes {
    public static class Name {
        public static final Name CLASS_PATH = new Name();
    }

    java.util.jar.Attributes mainAttributes;
    public Attributes(java.util.jar.Attributes mainAttributes) {
        this.mainAttributes = mainAttributes;
    }

    public String getValue(Name classPath) {
        assert (classPath == Name.CLASS_PATH);
        String ret = mainAttributes.getValue(java.util.jar.Attributes.Name.CLASS_PATH);
        return ret;
    }

}
