package jvm.classparser;

import java.util.HashMap;
import java.util.Vector;

import jvm.classparser.JAttribute.SourceFile;
import jvm.classparser.JMember.JField;
import jvm.classparser.JMember.JMethod;
import jvm.classparser.jconstants.JClassConstant;
import jvm.classparser.jconstants.JConstant;
import jvm.classparser.jconstants.JDataConstant;
import jvm.classparser.jconstants.JMemberConstant;
import jvm.execution.JClassLoader;
import jvm.execution.JClassLoader.JClassNotInitializedException;
import jvm.execution.objrepr.ObjectRepr;
import jvm.util.DataInputStream;


/**
 * Parser for a .class file according to the specification found here:
 *
 * http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
 *
 * @author ctalau
 *
 */
public class JClass implements ObjectRepr {
    private final static int MAGIC = 0xCAFEBABE;

    public enum Status {
        PREPARED, INITIALIZED
    }

    private Status status = Status.PREPARED;
    protected String name;
    private short flags;

    private JClass superClass;

    protected JConstantPool cpool = null;
    private Vector<JClassConstant> interfaces = new Vector<JClassConstant>();

    private Vector<JField> fields = new Vector<JField>();
    private Vector<JMethod> methods = new Vector<JMethod>();
    private Vector<JAttribute> attributes = new Vector<JAttribute>();
    private JMethod clinit;

    private String sourceFile;
    private int minVer, majVer;

    /**
     * Read the class file from the input stream and create an in-memory
     * representation of it.
     */
    public JClass(DataInputStream is) {
        int magic = is.readUInt();
        if (magic != MAGIC)
            throw (new AssertionError("Corrupted class file: magic = " + magic));
        this.minVer = is.readUShort();
        this.majVer = is.readUShort();

        this.cpool = new JConstantPool(is);

        this.flags = (short) is.readUShort();

        this.name = cpool.getClassName(is.readUShort());
        String superClassName = cpool.getClassName(is.readUShort());
        if (superClassName != null) {
            this.superClass = JClassLoader.getInstance().getClassByName(
                    superClassName);
        }

        int ifCnt = is.readUShort();
        for (int i = 0; i < ifCnt; i++) {
            this.interfaces.add((JClassConstant) cpool.get(is.readUShort()));
        }

        int fieldCnt = is.readUShort();
        for (int i = 0; i < fieldCnt; i++) {
            this.fields.add(new JField(is, this, i));
        }

        int mthCnt = is.readUShort();
        for (int i = 0; i < mthCnt; i++) {
            JMethod jm = JMethod.readMethod(is, this, i);
            if (jm.memberName.equals(JMethod.CLINIT_METHOD_NAME)) {
                clinit = jm;
            }
            this.methods.add(jm);
        }

        int attrCnt = is.readUShort();
        for (int i = 0; i < attrCnt; i++) {
            JAttribute attr = JAttribute.readAttribute(is, cpool);
            this.attributes.add(attr);

            if (attr.getName().equals("SourceFile")) {
                this.sourceFile = ((SourceFile) attr).getFileName();
            }

        }
    }

    private JClass() {
    }

    public void ensureInitialized() throws JClassNotInitializedException {
        if (this.status == Status.PREPARED) {
            throw new JClassNotInitializedException(this);
        }

        if (this.superClass != null) {
            this.superClass.ensureInitialized();
        }
    }

    /**
     * Factory method to obtain the boot class
     */
    public static JClass createBootClass(final String bootClassName) {
        final JClassConstant consolePrintStream =
                new JClassConstant("java/io/ConsolePrintStream");

        return new JClass() {
            {
                this.cpool = new JConstantPool();
                this.cpool.add(null);
                this.cpool.add(JMemberConstant.getBootMethodRef(bootClassName));
                this.cpool.add(new JClassConstant(bootClassName));
                this.cpool.add(new JClassConstant(JClassLoader.STRING_CLASS_NAME));
                this.cpool.add(consolePrintStream);
                this.cpool.add(new JMemberConstant(consolePrintStream, "register", "()V"));
                this.getMethods().add(JMethod.getBootMehtod());
            }

            public String getName() {
                return "$bootclass$";
            }
        };
    }

    public static JClass createPrimitiveClass(final String name) {
        return new JClass() {
            public String getName() {
                return name;
            }
        };
    }

    /**
     * Create array class instance
     */
    private static HashMap<String, JClass> arrayClassCache = new HashMap<String, JClass>();

    public static JClass getArrayClass(final String type) {
        JClass ret = arrayClassCache.get(type);
        if (ret == null) {
            ret = new JClass() {
                {
                    name = type;
                }
            };
            arrayClassCache.put(ret.name, ret);
        }
        return ret;
    }

    public JClass getSuperClass() {
        return this.superClass;
    }

    @Override
    public String toString() {
        if (name != null && name.startsWith("["))
            return name;
        return "L" + this.name + ";";
    }

    public JMethod getClinit() {
        return clinit;
    }

    public String getName() {
        return name;
    }

    public String getString(int i) {
        return cpool.getString(i);
    }

    public String getClassName(int i) {
        return cpool.getClassName(i);
    }

    public JClassConstant getClassConstant(int i) {
        return cpool.getClassConstant(i);
    }

    public JMemberConstant getMember(int i) {
        return cpool.getMember(i);
    }

    public JDataConstant getDataConstant(int i) {
        return cpool.getDataConstant(i);
    }

    public JConstant getConstant(int i) {
        return cpool.getConstant(i);
    }

    // XXX change it to return JClass instead (they need to be loaded anyway).
    public Vector<JClassConstant> getInterfaces() {
        return interfaces;
    }

    public Vector<JField> getFields() {
        return fields;
    }

    public Vector<JMethod> getMethods() {
        return methods;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getMinorVersion() {
        return minVer;
    }

    public int getMajorVersion() {
        return majVer;
    }

    public short getFlags() {
        return flags;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JClass) {
            return name.equals(((JClass) obj).name);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public Object getField(JMemberConstant fld) {
        throw new UnsupportedOperationException(fld.toString());
    }

    @Override
    public void putField(JMemberConstant fld, Object value) {
        throw new UnsupportedOperationException(fld.toString());
    }

    @Override
    public JMethod dispatchMethod(JMemberConstant cm) {
        throw new UnsupportedOperationException(cm.toString());
    }

    @Override
    public JMethod dispatchSuperMethod(JMemberConstant cm) {
        throw new UnsupportedOperationException(cm.toString());
    }

    @Override
    public JMethod dispatchNonVirtualMethod(JMemberConstant cm) {
        throw new UnsupportedOperationException(cm.toString());
    }

    @Override
    public JClass getJClass() {
        return JClassLoader.getInstance().getClassByName("java/lang/Class");
    }
}
