package jvm.execution;

import gwtjava.lang.System;

import java.util.HashMap;

import jvm.classparser.JClass;
import jvm.classparser.JMember.JNativeMethod;
import jvm.classparser.JMember.JNativeMethod0;
import jvm.classparser.JMember.JNativeMethod1;
import jvm.classparser.JMember.JNativeMethod2;
import jvm.classparser.JMember.JNativeMethod3;
import jvm.classparser.JMember.JNativeMethod5;
import jvm.classparser.jconstants.JClassConstant;
import jvm.classparser.jconstants.JMemberConstant;
import jvm.classparser.jconstants.JStringConstant;
import jvm.execution.JClassLoader.JClassNotInitializedException;
import jvm.execution.objrepr.ArrayRepr;
import jvm.execution.objrepr.ObjectFactory;
import jvm.execution.objrepr.ObjectRepr;
import jvm.execution.objrepr.StaticMembers;


public class Natives {

    private static class NativeArrayCopy extends JNativeMethod5<Void, ArrayRepr, Integer, ArrayRepr, Integer, Integer> {
        @Override
        public Void call(ArrayRepr src, Integer srcPos, ArrayRepr dest, Integer destPos,
                Integer length) {
            for (int i = 0; i < length; i++) {
                dest.set(destPos + i, src.get(srcPos + i));
            }
            return null;
        }
    }

    private static class NativeGetPrimitiveClass extends JNativeMethod1<ObjectRepr, ObjectRepr> {
        @Override
        public ObjectRepr call(ObjectRepr name) {
            JClassLoader jcl = JClassLoader.getInstance();
            return jcl.getClassByName(JStringConstant.toString(name));
        }
    }

    private static class NativeFloatToRawIntBits extends JNativeMethod1<Integer, Float> {
        @Override
        public Integer call(Float f) {
            return Float.floatToIntBits(f);
        }
    }

    private static class NativeDoubleToRawLongBits extends JNativeMethod1<Long, Double> {
        @Override
        public Long call(Double d) {
            return Double.doubleToLongBits(d);
        }
    }

    private static class NativeNanoTime extends JNativeMethod0<Long> {
        @Override
        protected Long call() {
            return 0L;
        }
    }

    private static class NativeCurrentTime extends JNativeMethod0<Long> {
        @Override
        protected Long call() {
            return 0L;
        }
    }

    private static class NativeIdentityHashCode extends JNativeMethod1<Integer, ObjectRepr> {
        @Override
        public Integer call(ObjectRepr arg1) {
            return java.lang.System.identityHashCode(arg1);
        }
    }

    private static class NativeCurrentThread extends JNativeMethod0<ObjectRepr> {
        @Override
        public ObjectRepr call() {
            JClassLoader jcl = JClassLoader.getInstance();
            return ObjectFactory.newObject(jcl.getClassByName("java/lang/Thread"));
        }
    }

    private static class NativeFreeMemory extends JNativeMethod0<Long> {
        @Override
        public Long call() {
            return 1000L;
        }
    }

    private static class NativeGetCallerClass extends JNativeMethod1<ObjectRepr, Integer> {
        private int count;
        @Override
        public ObjectRepr call(Integer arg1) {
            if (count++ == 0) {
                JClassLoader jcl = JClassLoader.getInstance();
                return jcl.getClassByName("java/util/Properties");
            } else {
                throw new AssertionError();
            }
        }
    }

    private static class NativeFillInStackTrace extends JNativeMethod2<ObjectRepr, ObjectRepr, Integer> {
        @Override
        public ObjectRepr call(ObjectRepr exn, Integer lg) {
            return exn;
        }
    }

    private static class NativeGetClass extends JNativeMethod1<ObjectRepr, ObjectRepr> {
        @Override
        public ObjectRepr call(ObjectRepr arg1) {
            return arg1.getJClass();
        }
    }

    private static class NativeDoPrivileged extends JNativeMethod1<ObjectRepr, ObjectRepr> {
        @Override
        public ObjectRepr call(ObjectRepr arg1) {
          return JStringConstant.createString("US-ASCII");
        }
    }

    private static class NativeForName extends JNativeMethod3<JClass, ObjectRepr, Integer, ObjectRepr> {
        @Override
        public JClass call(ObjectRepr name, Integer arg3, ObjectRepr arg4) {
            JClassLoader jcl = JClassLoader.getInstance();
            String className = JStringConstant.toString(name);
            className = className.replaceAll("\\.", "/");
            return jcl.getClassByName(className);
        }
    }

    private static class NativeNewInstance extends JNativeMethod1<ObjectRepr, JClass> {
        @Override
        public ObjectRepr call(JClass cls) {
            return ObjectFactory.newObject(cls);
        }
    }

    private static class NativeAllocateMemory extends JNativeMethod2<Long, ObjectRepr, Long> {
        @Override
        public Long call(ObjectRepr unsafe, Long amount) {
            return 0L;
        }
    }

    private static class NativePutLong extends JNativeMethod3<Void, ObjectRepr, Long, Long> {
        @Override
        public Void call(ObjectRepr arg1, Long arg2, Long arg3) {
            return null;
        }
    }

    private static class NativeGetByte extends JNativeMethod2<Integer, ObjectRepr, Long> {
        @Override
        public Integer call(ObjectRepr arg1, Long arg2) {
            return 0x08;
        }
    }

    private static class NativeSetOut extends JNativeMethod1<Void, ObjectRepr> {
        @Override
        public Void call(ObjectRepr ps) {
            try {
                StaticMembers.putStaticField(new JMemberConstant(
                        new JClassConstant("java/lang/System"),
                        "out", "Ljava/io/PrintStream;"), ps);
            } catch (JClassNotInitializedException e) {
                throw new AssertionError(e.jClass.getStatus());

            }
            return null;
        }
    }

    private static class NativeConsoleWrite extends JNativeMethod1<Void, Integer> {
        @Override
        public Void call(Integer arg1) {
            System.out.print((char) arg1.intValue());
            return null;
        }
    }

    private static class NativeDoFreeMemory extends JNativeMethod2<Void, ObjectRepr, Long> {
        @Override
        public Void call(ObjectRepr unsafe, Long arg1) {
            return null;
        }
    }

    private static class NativeDefault extends JNativeMethod {
        @Override
        public Object call(Object... args) {
            throw new UnsupportedOperationException();
        }
    }


    private static HashMap<String, JNativeMethod> methods = new HashMap<String, JNativeMethod>();

    static {
        methods.put("sun/reflect/Reflection/getCallerClass(I)Ljava/lang/Class;",
                new NativeGetCallerClass());
        methods.put("java/lang/Throwable/fillInStackTrace(I)Ljava/lang/Throwable;",
                new NativeFillInStackTrace());
        methods.put("java/lang/Object/getClass()Ljava/lang/Class;",
                new NativeGetClass());
        methods.put("java/lang/Class/getPrimitiveClass(Ljava/lang/String;)Ljava/lang/Class;",
                new NativeGetPrimitiveClass());
        methods.put("java/lang/Float/floatToRawIntBits(F)I",
                new NativeFloatToRawIntBits());
        methods.put("java/lang/Double/doubleToRawLongBits(D)J",
                new NativeDoubleToRawLongBits());
        methods.put("java/lang/System/arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V",
                new NativeArrayCopy());
        methods.put("java/lang/System/identityHashCode(Ljava/lang/Object;)I",
                new NativeIdentityHashCode());
        methods.put("java/security/AccessController/doPrivileged(Ljava/security/PrivilegedAction;)Ljava/lang/Object;",
                new NativeDoPrivileged());
        methods.put("java/lang/Thread/currentThread()Ljava/lang/Thread;",
                new NativeCurrentThread());
        methods.put("java/lang/Runtime/freeMemory()J",
                new NativeFreeMemory());
        methods.put("java/lang/Class/forName0(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;",
                new NativeForName());
        methods.put("java/lang/Class/newInstance()Ljava/lang/Object;",
                new NativeNewInstance());
        methods.put("java/lang/System/setOut0(Ljava/io/PrintStream;)V",
                new NativeSetOut());
        methods.put("java/io/ConsolePrintStream$ConsoleOutputStream/write0(I)V",
                new NativeConsoleWrite());

        methods.put("sun/misc/Unsafe/freeMemory(J)V", new NativeDoFreeMemory());
        methods.put("sun/misc/Unsafe/getByte(J)B", new NativeGetByte());
        methods.put("sun/misc/Unsafe/putLong(JJ)V", new NativePutLong());
        methods.put("sun/misc/Unsafe/allocateMemory(J)J", new NativeAllocateMemory());
        methods.put("java/lang/System/nanoTime()J", new NativeNanoTime());
        methods.put("java/lang/System/currentTimeMillis()J", new NativeCurrentTime());
    }

    public static JNativeMethod getNativeMethod(String name) {
        if (methods.containsKey(name)) {
            return methods.get(name);
        }
        return new NativeDefault();
    }


}
