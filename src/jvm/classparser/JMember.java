package jvm.classparser;

import java.util.List;
import java.util.Vector;

import jvm.classparser.JAttribute.CodeAttribute;
import jvm.classparser.JAttribute.ConstantValueAttribute;
import jvm.execution.Natives;
import jvm.util.DataInputStream;


/**
 * Information about members of the class: mehtods, static methods, fields
 *
 *
 * @author ctalau
 */
public class JMember {
    public static final int STATIC = 0x0008;
    public static final int NATIVE = 0x0100;

    protected int flags; // static or not?

    protected String memberName;
    protected String fullName;
    protected String type;
    protected Vector<JAttribute> attributes = new Vector<JAttribute>();
    protected int id = 0;
    protected JClass cls;

    public JMember(DataInputStream is, JClass cls, int id, int flags) {
        JConstantPool cpool = cls.cpool;
        this.cls = cls;
        this.id = id;

        this.flags = flags;
        String name = cpool.getString(is.readUShort());
        String type = cpool.getString(is.readUShort());

        this.memberName = name + type;
        this.type = type;
        this.fullName = cls.name + "/" + memberName;


        int attrCnt = is.readUShort();
        for (int i = 0; i < attrCnt; i++)
            attributes.add(JAttribute.readAttribute(is, cpool));
    }

    public JMember(){}

    @Override
    public String toString() {
        return this.memberName + " : " + attributes;
    }

    public boolean isStatic(){
        return (flags & STATIC) == STATIC;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getFullName() {
        return fullName;
    }

    public int getID(){
        return id;
    }

    /**
     * JMethod contains the [code] of the method and the size of it's arguments
     * on the stack.
     *
     * @author ctalau
     */
    public static class JMethod extends JMember {
        // size of the arguments on the stack, including dispatch object
        protected int argsSize, retSize;
        protected List<Integer> argsSizes;

        public static JMethod readMethod(DataInputStream is, JClass cls, int id) {
            int flags = is.readUShort();
            if ((flags & NATIVE) == NATIVE) {
                JMethod jm = new JMethod(is, cls, id, flags);
                JNativeMethod jnm = Natives.getNativeMethod(jm.getFullName());
                jnm.init(jm);
                return jnm;
            } else {
                return new JBytecodeMethod(is, cls, id, flags);
            }
        }

        protected JMethod(DataInputStream is, JClass cls, int id, int flags) {
            super(is, cls, id, flags);
            this.argsSize = JType.argsSize(type);
            this.argsSizes = JType.argsSizes(type);

            // Instance methods have on the stack the dispatch object
            if (!isStatic()){
                this.argsSizes.add(0, 1);
                this.argsSize++;
            }
            this.retSize = JType.retSize(type);
        }

        protected JMethod() {
        }

        private static JBytecodeMethod bootMehtod = new JBytecodeMethod(){{
            this.flags = STATIC;
            this.memberName = this.fullName = "<initmethod>";
            this.code = CodeAttribute.getBootCode();
        }};

        public static JBytecodeMethod getBootMehtod() {
            return bootMehtod;
        }

        @Override
        public String toString() {
            return fullName;
        }

        public int getArgsSize() {
            return argsSize;
        }

        public int getRetSize() {
            return retSize;
        }

        public List<Integer> getArgsSizes() {
            return argsSizes;
        }

        public JClass getJClass() {
            return cls;
        }

        public static final String CLINIT_METHOD_NAME = "<clinit>()V";
        public static final String STRING_INIT_METHOD_NAME = "<init>([C)V";
    }

    public static class JBytecodeMethod extends JMethod {
        protected CodeAttribute code;

        public JBytecodeMethod(DataInputStream is, JClass cls, int id, int flags) {
            super(is, cls, id, flags);
            for (JAttribute a : attributes)
                if (a.getName().equals("Code"))
                    code = (CodeAttribute) a;
        }

        private JBytecodeMethod() {
        }

        public CodeAttribute getCode() {
            return code;
        }
    }

    public static abstract class JNativeMethod extends JMethod {
        public abstract Object call(Object... args);
        public void init(JMethod jm) {
            this.attributes = jm.attributes;
            this.cls = jm.cls;
            this.flags = jm.flags;
            this.fullName = jm.fullName;
            this.id = jm.id;
            this.memberName = jm.memberName;
            this.type = jm.type;

            this.argsSize = jm.argsSize;
            this.argsSizes = jm.argsSizes;
            this.retSize = jm.retSize;
        }
    }

    public static abstract class JNativeMethod0<R> extends JNativeMethod {
        public Object call(Object... args) {
            return call();
        }
        protected abstract R call();
    }

    public static abstract class JNativeMethod1<R, T1> extends JNativeMethod {
        @SuppressWarnings("unchecked")
        public Object call(Object... args) {
            return call((T1)args[0]);
        }
        public abstract R call(T1 arg1);
    }

    public static abstract class JNativeMethod2<R, T1, T2> extends JNativeMethod {
        @SuppressWarnings("unchecked")
        public Object call(Object... args) {
            return call((T1)args[0], (T2)args[1]);
        }
        public abstract R call(T1 arg1, T2 arg2);
    }

    public static abstract class JNativeMethod3<R, T1, T2, T3> extends JNativeMethod {
        @SuppressWarnings("unchecked")
        public Object call(Object... args) {
            return call((T1)args[0], (T2)args[1], (T3)args[2]);
        }
        public abstract R call(T1 arg1, T2 arg2, T3 arg3);
    }

    public static abstract class JNativeMethod4<R, T1, T2, T3, T4> extends JNativeMethod {
        @SuppressWarnings("unchecked")
        public Object call(Object... args) {
            return call((T1)args[0], (T2)args[1], (T3)args[2], (T4)args[3]);
        }
        public abstract R call(T1 arg1, T2 arg2, T3 arg3, T4 arg4);
    }

    public static abstract class JNativeMethod5<R, T1, T2, T3, T4, T5> extends JNativeMethod {
        @SuppressWarnings("unchecked")
        public Object call(Object... args) {
            return call((T1)args[0], (T2)args[1], (T3)args[2], (T4)args[3], (T5)args[4]);
        }
        public abstract R call(T1 arg1, T2 arg2, T3 arg3, T4 arg4, T5 arg5);
    }

    /**
     * JField contains the initialization value for a field as well as it's size
     *
     * @author ctalau
     */
    public static class JField extends JMember {
        private Object initValue;
        private int size;

        public JField(DataInputStream is, JClass c, int id) {
            super(is, c, id, is.readUShort());
            for (JAttribute a : attributes)
                if (a.getName().equals("ConstantValue"))
                    initValue = ((ConstantValueAttribute) a).getValue();
            if (initValue == null)
                initValue = JType.getDefaultValue(type);
            this.size = JType.size(type);
        }

        public Object getInitValue() {
            return initValue;
        }

        public int getSize() {
            return size;
        }
    }
}
