package jvm.classparser;

import java.util.Vector;

import jvm.classparser.jconstants.JBaseConstant;
import jvm.classparser.jconstants.JClassConstant;
import jvm.classparser.jconstants.JConstant;
import jvm.classparser.jconstants.JDataConstant;
import jvm.classparser.jconstants.JMemberConstant;
import jvm.classparser.jconstants.JStringConstant;
import jvm.classparser.jconstants.JUTF8Constant;
import jvm.util.DataInputStream;


/**
 * JConstantPool : a collection of JConstants from which we can extract Strings
 * and class names without any need for a cast.
 *
 * @author ctalau
 *
 */
public class JConstantPool extends Vector<JConstant> {
    private static final long serialVersionUID = -9042108001788219242L;

    public JConstantPool(DataInputStream is) {
        int cpCnt = is.readUShort();
        setSize(cpCnt);

        for (int i = 1; i < cpCnt; i++) {
            JConstant c = readConstant(is);
            set(i, c);
            if (c instanceof JBaseConstant) {
                Object repr = ((JBaseConstant) c).getRepr();
                if (repr instanceof Long || repr instanceof Double) {
                    i++;
                }
            }
        }

        /**
         * In the constant pool, references are represented by indices. That's
         * why we convert them to pointer references in the in-memory image of
         * the class file.
         */
        for (int i = 1; i < cpCnt; i++) {
            JConstant ct = get(i);
            if (ct != null)
                set(i, ct.link(this));
        }

    }

    public JConstantPool(){
    }

    private static JConstant readConstant(DataInputStream is) {
        byte tag = (byte) is.read();

        switch (tag) {
        case JConstant.CONSTANT_Class:
            return JClassConstant.createConstantStub(tag, is);
        case JConstant.CONSTANT_Fieldref:
        case JConstant.CONSTANT_Methodref:
        case JConstant.CONSTANT_InterfaceMethodref:
        case JConstant.CONSTANT_NameAndType:
            return JMemberConstant.createConstantStub(tag, is);
        case JConstant.CONSTANT_String:
            return JStringConstant.createConstantStub(tag, is);
        case JConstant.CONSTANT_Integer:
        case JConstant.CONSTANT_Float:
        case JConstant.CONSTANT_Long:
        case JConstant.CONSTANT_Double:
            return new JBaseConstant(tag, is);
        case JConstant.CONSTANT_UTF8:
            return new JUTF8Constant(is);
            default:
                System.out.println("tag: " + tag);
        }
        return null;
    }


    public String getString(int i) {
        JConstant elem = this.get(i);
        return ((JUTF8Constant) elem).value;
    }

    public String getClassName(int i){
        JClassConstant jc = (JClassConstant) get(i);
        return (jc == null) ? null : jc.getName();
    }

    public JClassConstant getClassConstant(int i) {
        return (JClassConstant) this.get(i);
    }

    public JMemberConstant getMember(int i) {
        return (JMemberConstant) this.get(i);
    }

    public JBaseConstant getBaseConstant(int i) {
        return (JBaseConstant) get(i);
    }

    public JConstant getConstant(int i) {
        return get(i);
    }

    public JDataConstant getDataConstant(int i) {
        return (JDataConstant) get(i);
    }
}
