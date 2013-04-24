/**
 *
 */
package jvm.execution.objrepr.java;

import java.util.Arrays;

import jvm.classparser.JClass;
import jvm.classparser.JType;
import jvm.classparser.JMember.JMethod;
import jvm.classparser.jconstants.JMemberConstant;
import jvm.execution.objrepr.ArrayRepr;


public class JavaArrayRepr extends ArrayRepr {
    public Object[] array;
    private JClass jc;

    public JavaArrayRepr(int size) {
        array = new Object[size];
    }

    @Override
    public Object get(int i) {
        return array[i];
    }

    @Override
    public void set(int i, Object val) {
        array[i] = val;
    }

    // Cache array object template
    public static ArrayRepr newJavaArrayRepr(String type, int size) {
//        JClassLoader jcl = JClassLoader.getInstance();
//        JClass superClass = jcl.getClassByName("java/lang/Object");
//        jo.putMembers(superClass); XXX: add it back

        JavaArrayRepr jo = new JavaArrayRepr(size);
        Arrays.fill(jo.array, JType.getDefaultElemValue(type));
        jo.jc = JClass.getArrayClass(type);

        return jo;
    }

    @Override
    public int length() {
        return array.length;
    }

    @Override
    public String toString() {
        String ret = "[";
        for (Object o : array) {
            ret += o + ",";
        }
        ret += "]";
        return ret;
    }

    @Override
    public JMethod dispatchMethod(JMemberConstant cm) {
        // XXX: TO implement
        throw new UnsupportedOperationException();
    }

    @Override
    public JClass getJClass() {
        return jc;
    }
}