package jvm.classparser;

import java.util.Vector;

import jvm.classparser.jconstants.JClassConstant;
import jvm.classparser.jconstants.JDataConstant;
import jvm.classparser.jconstants.JUTF8Constant;
import jvm.execution.OPCodes;
import jvm.util.DataInputStream;


/**
 * Attributes that can be found in a class file.
 * By now, we deal only with [ConstantValue] and [Code] attribute types.
 *
 * @author ctalau
 *
 */
public class JAttribute {
    protected int length;
    private String name;


    protected JAttribute(DataInputStream is, JConstantPool cpool, String name) {
        length = is.readUInt();
        this.name = name;
    }

    private JAttribute(){}

    @Override
    public String toString() {
        return this.name;
    }

    public static JAttribute readAttribute(DataInputStream is,
            JConstantPool cpool) {
        String name = (String) ((JUTF8Constant) cpool.get(is.readUShort())).value;
        if (name.equals("ConstantValue")) {
            return new ConstantValueAttribute(is, cpool, name);
        } else if (name.equals("Code")) {
            return new CodeAttribute(is, cpool, name);
        } else if (name.equals("LineNumberTable")){
            return new LineNumberTable(is, cpool, name);
        } else if (name.equals("LocalVariableTable")){
            return new LocalVariableTable(is, cpool, name);
        } else if (name.equals("SourceFile")){
            return new SourceFile(is, cpool, name);
        } else {
            return new IngoredAttribute(is, cpool, name);
        }
    }

    public String getName() {
        return name;
    }

    /**
     * "ConstantValue" attributes are referenced from the code.
     */
    public static class ConstantValueAttribute extends JAttribute {
        private Object value;

        public ConstantValueAttribute(DataInputStream is, JConstantPool cpool,
                String name) {
            super(is, cpool, name);
            JDataConstant ctVal = cpool.getDataConstant(is.readUShort());
            value = ctVal.getRepr();
        }

        public Object getValue() {
            return value;
        }
    }


    /**
     * Every method have one "Code" attribute, containing the bytecode and
     * exception handlers and details about the stackframe.
     */
    public static class CodeAttribute extends JAttribute {
        public static class ExceptionDescriptor {
            public int start_pc, end_pc, handler_pc;
            public JClassConstant type;

            public ExceptionDescriptor(DataInputStream is, JConstantPool cpool) {
                this.start_pc = is.readUShort();
                this.end_pc = is.readUShort();
                this.handler_pc = is.readUShort();
                int typeIndex = is.readUShort();
                this.type = typeIndex == 0 ? JClassConstant.ANY : cpool.getClassConstant(typeIndex);
            }
        }

        public int maxStack;
        public int maxLocals;
        public int argsSize;
        public byte [] bytecode;
        public Vector<ExceptionDescriptor> exns = new Vector<ExceptionDescriptor>();
        public Vector <JAttribute> attributes = new Vector<JAttribute>();

        public CodeAttribute (DataInputStream is, JConstantPool cpool, String name) {
            super(is, cpool, name);
            this.maxStack = is.readUShort();
            this.maxLocals = is.readUShort();

            int codeLength = is.readUInt();
            this.bytecode = new byte[codeLength];
            is.read(this.bytecode);

            int exnsCnt = is.readUShort();
            for (int i = 0; i < exnsCnt; i++)
                this.exns.add(new ExceptionDescriptor(is, cpool));

            int attrCnt = is.readUShort();
            for (int i = 0; i < attrCnt; i++)
                this.attributes.add(JAttribute.readAttribute(is, cpool));
        }
        private CodeAttribute(){}



        private static CodeAttribute bootCode = new CodeAttribute(){
            {
                maxStack = 1;
                maxLocals = 1;
                argsSize = 0;

                byte [] bytecode = {

                /*
                 * Register the output stream.
                 */
                OPCodes.OP_invokestatic,
                0,
                5,

                /*
                 *  Create a String array of size 0; as a side effect,
                 *  the java/lang/String class is loaded.
                 */
                OPCodes.OP_iconst_0,
                OPCodes.OP_anewarray,
                0,
                3,

                // store the array in the first local variable (argument)
                OPCodes.OP_astore_0,

                // invoke the <initmethod> (the first one in the constant pool)
                OPCodes.OP_invokestatic,
                0,
                1,

                // implementation dependent opcode to exit the VM
                OPCodes.OP_impdep1,
                };
                this.bytecode = bytecode;

            }
        };
        public static CodeAttribute getBootCode(){
            return bootCode;
        }
    }

    /**
     * Useful because it can be retrieved by the front-end and used for single
     * stepping.
     *
     * @author ctalau
     *
     */
    public static class LineNumberTable extends JAttribute {
        private static class LinePCPair {
            @SuppressWarnings("unused")
            int startpc, lineno;

            public LinePCPair(int startpc, int lineno) {
                super();
                this.startpc = startpc;
                this.lineno = lineno;
            }

        }
        private LinePCPair lines [];

        public LineNumberTable(DataInputStream is, JConstantPool cpool,
                String name) {
            super(is, cpool, name);

            int tableSize = is.readUShort();
            lines = new LinePCPair[tableSize];

            for (int i = 0; i < tableSize; i++){
                int startpc = is.readUShort();
                int lineno = is.readUShort();
                lines[i] = new LinePCPair(startpc, lineno);
            }
        }
    }

    /**
     * Retrieved by the front-end.
     *
     * @author ctalau
     *
     */
    public static class LocalVariableTable extends JAttribute {
        private static class LocalVariable {
            @SuppressWarnings("unused")
            int startpc, length, index;
            @SuppressWarnings("unused")
            String name, descriptor;
            public LocalVariable(int startpc, int length, int index,
                    String name, String descriptor) {
                super();
                this.startpc = startpc;
                this.length = length;
                this.index = index;
                this.name = name;
                this.descriptor = descriptor;
            }
        }
        private LocalVariable table [];
        public LocalVariableTable(DataInputStream is, JConstantPool cpool,
                String name) {
            super(is, cpool, name);
            int tableSize = is.readUShort();
            table = new LocalVariable[tableSize];
            for (int i = 0; i < tableSize; i++){
                int startpc = is.readUShort();
                int length = is.readUShort();
                String varname = cpool.getString(is.readUShort());
                String descriptor = cpool.getString(is.readUShort());
                int index = is.readUShort();


                table[i] = new LocalVariable(startpc, length, index, varname, descriptor);
            }
        }
    }

    /**
     * The name of the source file for this class. Used by the debugger
     * front-end to show source while stepping.
     *
     * @author ctalau
     *
     */
    public static class SourceFile extends JAttribute {
        private String fileName;
        public SourceFile(DataInputStream is, JConstantPool cpool, String name) {
            super(is, cpool, name);
            this.fileName = cpool.getString(is.readUShort());
        }
        public String getFileName() {
            return fileName;
        }
    }

    /**
     * This kind of attribute is requested by the font-end, but ...
     *
     * I don't know whether I should load the definition of the inner classes
     * when the outer one is loaded?!
     *
     * @author ctalau
     *
     */
    public static class InnerClassesAttribute extends JAttribute {
        protected InnerClassesAttribute(DataInputStream is, JConstantPool cpool, String name) {
            super(is, cpool, name);
            is.skip(this.length);
        }
    }

    public static class IngoredAttribute extends JAttribute {
        public IngoredAttribute(DataInputStream is, JConstantPool cpool, String name) {
            super(is, cpool, name);
            is.skip(this.length);
        }
    }

}
