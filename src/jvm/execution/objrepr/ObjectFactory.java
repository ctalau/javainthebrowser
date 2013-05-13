package jvm.execution.objrepr;

import jvm.classparser.JClass;
import jvm.execution.objrepr.java.JavaArrayRepr;
import jvm.execution.objrepr.java.JavaObjectRepr;

public class ObjectFactory {
    public static void reset() {
        JavaObjectRepr.reset();
    }

    public static ObjectRepr newObject(JClass jc) {
        return JavaObjectRepr.newJavaObjectRepr(jc);
    }

    public static ArrayRepr newArray(String type, int size) {
        return JavaArrayRepr.newJavaArrayRepr(type, size);
    }

    /**
     * Creation of a multidimensional array.
     *
     * @param className
     */
    public static Object newMultiArray(String className, int dims[]) {
        return newMultiArray(className, dims, 0);
    }

    private static Object newMultiArray(String className, int dims[], int off) {
       ArrayRepr a = ObjectFactory.newArray(className, dims[off]);

       if (off < dims.length - 1) {
           for (int i = 0; i < dims[off]; i++)
               a.set(i, newMultiArray(className.substring(1), dims, off + 1));
       }

       return a;
   }

}
