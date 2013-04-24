package jvm.classparser.jconstants;

import jvm.classparser.JConstantPool;
import jvm.util.DataInputStream;

public class JUTF8Constant implements JConstant {
    public String value;

    public JUTF8Constant(DataInputStream is) {
        int length = is.readUShort();
        byte data[] = new byte[length];
        is.read(data);
        this.value = new String(data);
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
    @Override
    public JConstant link(JConstantPool cpool) {
        return this;
    }
}
