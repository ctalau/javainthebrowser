package gwtjava.lang;

public class ClassLoader {
    public java.lang.ClassLoader jcl;

    public ClassLoader(java.lang.ClassLoader jcl) {
        this.jcl = jcl;
    }

    public Class<?> loadClass(String processorName) throws ClassNotFoundException {
        try {
            return jcl.loadClass(processorName);
        } catch (java.lang.ClassNotFoundException e) {
            throw new ClassNotFoundException(e.getMessage());
        }
    }

}
