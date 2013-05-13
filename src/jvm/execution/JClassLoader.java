package jvm.execution;

import java.util.HashMap;

import jvm.classparser.JClass;
import jvm.classparser.JType;


/**
 * Lazy asynchronous class loader
 *
 * @author ctalau
 *
 */
public abstract class JClassLoader {
    // global cache for loaded classes
    protected HashMap<String, JClass> classes = new HashMap<String, JClass>();

    /**
     * Return a loaded class or load it.
     */
    public JClass getClassByName(String name) {
        JClass ret = classes.get(name);
        if (ret == null) {
            if (JType.isArray(name)) {
                return JClass.getArrayClass(name);
            } else if (JType.isPrimitive(name)) {
                throw new UnsupportedOperationException();
            } else {
                ret = loadClass(name);
                classes.put(name, ret);
            }

        }
        return ret;
    }

    protected abstract JClass loadClass(String name);

    /**
     * Methods to deal with system classes.
     *
     */
    // System classes - that are loaded by every non-trivial program
    public static final String OBJECT_CLASS_NAME = "java/lang/Object";
    public static final String STRING_CLASS_NAME = "java/lang/String";
    public static final String SYSTEM_CLASS_NAME = "java/lang/System";
    public static final String MATH_CLASS_NAME = "java/lang/Math";
    public static final String DOUBLE_CLASS_NAME = "java/lang/Double";
    public static final String FLOAT_CLASS_NAME = "java/lang/Float";
    public static final String PSTREAM_CLASS_NAME = "java/io/PrintStream";

    private static final String[] systemClassNames = { STRING_CLASS_NAME};
    private static final String[] primitiveClassNames = { "int", "float", "double" };

    /**
     * This method should be called prior to anything and given a continuation
     */
    public void loadSystemClasses() {
        for (int i = systemClassNames.length - 1; i >= 0; i--) {
            loadClass(systemClassNames[i]);
        }
        for (String name : primitiveClassNames) {
            classes.put(name, JClass.createPrimitiveClass(name));
        }
    }

    @SuppressWarnings("serial")
    public static class JClassNotInitializedException extends Exception {
        public JClass jClass;

        public JClassNotInitializedException(JClass jClass) {
            this.jClass = jClass;
        }
    }

    /**
     * Singleton instance of the class loader
     */
    private static JClassLoader inst;

    public static JClassLoader getInstance() {
        assert (inst != null);
        return inst;
    }

    public static void setInstance(JClassLoader inst) {
        JClassLoader.inst = inst;
    }
}
