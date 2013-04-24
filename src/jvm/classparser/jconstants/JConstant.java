package jvm.classparser.jconstants;

import jvm.classparser.JConstantPool;

public interface JConstant {

    public static final byte CONSTANT_Class = 7;
    public static final byte CONSTANT_Fieldref = 9;
    public static final byte CONSTANT_Methodref = 10;
    public static final byte CONSTANT_InterfaceMethodref = 11;
    public static final byte CONSTANT_String = 8;
    public static final byte CONSTANT_Integer = 3;
    public static final byte CONSTANT_Float = 4;
    public static final byte CONSTANT_Long = 5;
    public static final byte CONSTANT_Double = 6;
    public static final byte CONSTANT_NameAndType = 12;
    public static final byte CONSTANT_UTF8 = 1;

    /**
     * Should return the linked version of self.
     */
    public abstract JConstant link(JConstantPool cpInfo);
}
