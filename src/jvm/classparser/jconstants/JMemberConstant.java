package jvm.classparser.jconstants;

import jvm.classparser.JConstantPool;
import jvm.classparser.JType;
import jvm.execution.ExecutionEngine;
import jvm.util.DataInputStream;

public class JMemberConstant implements JConstant {
    protected String fullName, memberName, className, type;
    protected JClassConstant classCst;
    /**
     * For fields, size is 2 for Long/Double and 0 for the others
     *
     * For methods, argsSize is the size of the arguments on the stack
     * including dispatch object.
     */
    protected int size, argsSize;


    public static JConstant createConstantStub(int tag, DataInputStream is){
        if (tag == JConstant.CONSTANT_NameAndType){
            return new JNaTConstantStub(is);
        } else { // MemberRef
            return new JMemberConstantStub(is);
        }
    }

    public JMemberConstant(JClassConstant classCst, String name, String type){
        this.classCst = classCst;
        this.className = classCst.getName();
        this.type = type;
        this.memberName = name + type;
        this.fullName = className + "/" + this.memberName;
        this.size = JType.size(type);
        this.argsSize = JType.argsSize(type) + 1;
    }

    public String getFullName() {
        return fullName;
    }

    public JClassConstant getClassConstant() {
        return classCst;
    }

    public String getClassName(){
        return className;
    }

    public String getMemberName() {
        return memberName;
    }

    public int getArgsSize() {
        return argsSize;
    }

    public int getSize() {
        return size;
    }

    public String getType() {
        return type;
    }

    private JMemberConstant(){}

    public static JMemberConstant getBootMethodRef(final String bootClass){
        return new JMemberConstant(){{
            this.classCst = new JClassConstant(bootClass);
            this.fullName = bootClass + "/" + ExecutionEngine.BOOT_METHOD_DESCRIPTOR;
            this.memberName = ExecutionEngine.BOOT_METHOD_DESCRIPTOR;
            this.argsSize = 1;
        }};
    }

    @Override
    public JConstant link(JConstantPool cpool) {
        return this;
    }

    @Override
    public String toString() {
        return getFullName();
    }
    private static class JNaTConstant implements JConstant {
        public String name, type;

        public JNaTConstant(String name, String type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public JConstant link(JConstantPool cpInfo) {
            return this;
        }

        @Override
        public String toString() {
            return name + ":" + type;
        }
    }

    private static class JNaTConstantStub implements JConstant {
        private int nameIdx, typeIdx;

        public JNaTConstantStub(DataInputStream is) {
            nameIdx = is.readUShort();
            typeIdx = is.readUShort();
        }

        private JNaTConstant jc = null;
        @Override
        public JConstant link(JConstantPool cpool) {
            if (jc == null){
                String name = cpool.getString(nameIdx);
                String type = cpool.getString(typeIdx);

                jc = new JNaTConstant(name, type);
            }
            return jc;
        }
    }

    private static class JMemberConstantStub implements JConstant {
        private int classIdx, nameIdx;
        public JMemberConstantStub(DataInputStream is) {
            classIdx = is.readUShort();
            nameIdx = is.readUShort();
        }

        private JMemberConstant jmc = null;
        @Override
        public JConstant link(JConstantPool cpool) {
            if (jmc == null){
                JClassConstant classCst = (JClassConstant) cpool.get(classIdx).link(cpool);
                JNaTConstant jnt = (JNaTConstant) cpool.get(nameIdx).link(cpool);
                jmc = new JMemberConstant(classCst, jnt.name, jnt.type);
            }
            return jmc;
        }
    }

}

