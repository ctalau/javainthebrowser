package jvm.classparser.jconstants;

import jvm.classparser.JClass;
import jvm.classparser.JConstantPool;
import jvm.execution.JClassLoader;
import jvm.util.DataInputStream;



public class JClassConstant extends JDataConstant {
    private String name;
    private JClass jClass; // Caches the JClass of this constant.
    public static final JClassConstant ANY = new JClassConstant("$any$");

    public JClassConstant(String name){
        this.name = name;
    }

    public static JConstant createConstantStub(int tag, DataInputStream is){
        return new JClassConstantStub(is);
    }

    @Override
    public JConstant link(JConstantPool cpool) {
        return this;
    }

    public String getName() {
        return name;
    }

    public JClass getJClass() {
        if (jClass == null ) {
            jClass = JClassLoader.getInstance().getClassByName(name);
        }
        return jClass;
    }

    @Override
    public Object getRepr() {
        return getJClass();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Temporary class until linking is done.
     */
    private static class JClassConstantStub implements JConstant {
        private int nameIdx;
        public JClassConstantStub(DataInputStream is) {
            this.nameIdx = is.readUShort();
        }

        @Override
        public JConstant link(JConstantPool cpool) {
            return new JClassConstant(cpool.getString(nameIdx));
        }

    }
}
