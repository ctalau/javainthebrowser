package jvm.classparser.jconstants;

import jvm.classparser.JConstantPool;
import jvm.util.DataInputStream;

/**
 * Represents constants of base types, string or class stored in class files.
 */
public class JBaseConstant extends JDataConstant {
    private Object value;

    public JBaseConstant(int tag, DataInputStream is) {
        switch(tag){
        case CONSTANT_Integer:
            this.value = is.readUInt();
            break;
        case CONSTANT_Float:
            this.value = is.readFloat();
            break;
        case CONSTANT_Long:
            this.value = is.readLong();
            break;
        case CONSTANT_Double:
            this.value = is.readDouble();
            break;
        default:
            throw new AssertionError("The constant does not have a value class!");
        }
    }

    protected JBaseConstant(){
    }

    @Override
    public JConstant link(JConstantPool cpInfo) {
        return this;
    }

    @Override
    public Object getRepr() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
