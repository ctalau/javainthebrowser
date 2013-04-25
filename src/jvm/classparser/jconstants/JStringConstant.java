package jvm.classparser.jconstants;

import jvm.classparser.JClass;
import jvm.classparser.JConstantPool;
import jvm.classparser.JMember.JMethod;
import jvm.execution.JClassLoader;
import jvm.execution.objrepr.ArrayRepr;
import jvm.execution.objrepr.ObjectFactory;
import jvm.execution.objrepr.ObjectRepr;
import jvm.util.DataInputStream;

/**
 * The object's value after patching is not initialized. This should be done
 * before <clinit> method is invoked using [charArray].
 */
public class JStringConstant extends JDataConstant {
    private String value;
    private ObjectRepr objectRepr;

    private JStringConstant(String value) {
        this.value = value;
    }

    public static JConstant createConstantStub(int tag, DataInputStream is){
        return new JStringConstantStub(tag, is);
    }

    @Override
    public JConstant link(JConstantPool cpool) {
        return this;
    }

    @Override
    public Object getRepr() {
        if (objectRepr == null) {
            objectRepr = createString(value);
        }
        return objectRepr;
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }

    private static JMemberConstant valueField = new JMemberConstant(
            new JClassConstant(JClassLoader.STRING_CLASS_NAME), "value", "[C");
    private static JClass stringClass = JClassLoader.getInstance()
            .getClassByName(JClassLoader.STRING_CLASS_NAME);

    public static ObjectRepr createString(final String value) {
        ArrayRepr charArray = new ArrayRepr() {

            @Override
            public Object get(int i) {
                return value.charAt(i);
            }

            @Override
            public void set(int i, Object val) {
                throw new UnsupportedOperationException();
            }

            @Override
            public int length() {
                return value.length();
            }

            @Override
            public JMethod dispatchMethod(JMemberConstant cm) {
                // XXX: at least check that it is not required.
                throw new UnsupportedOperationException();
            }

            @Override
            public JClass getJClass() {
                // XXX: at least check that it is not required.
                throw new UnsupportedOperationException();
            }

            @Override
            public String toString() {
                return "\"" + value + "\"";
            }
        };
        ObjectRepr ret = ObjectFactory.newObject(stringClass);
        ret.putField(valueField, charArray);
        return ret;
    }

    public static String toString(ObjectRepr scst) {
        // XXX: bad perf
        JMemberConstant valueField = new JMemberConstant(
                new JClassConstant(JClassLoader.STRING_CLASS_NAME), "value", "[C");

        ArrayRepr value = (ArrayRepr) scst.getField(valueField);
        char [] ret = new char[value.length()];
        for (int i = 0; i < value.length(); i++) {
            ret[i] = (Character) value.get(i);
        }
        return new String(ret);
    }

    private static class JStringConstantStub implements JConstant {
        private int valueIdx;

        public JStringConstantStub(int tag, DataInputStream is) {
            valueIdx = is.readUShort();
        }

        @Override
        public JConstant link(JConstantPool cpool) {
            return new JStringConstant(cpool.getString(valueIdx));
        }
    }
}
