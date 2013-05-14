package jvm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import jvm.classparser.JClass;
import jvm.classparser.jconstants.JClassConstant;
import jvm.execution.JClassLoader;
import jvm.util.DataInputStream;

public class TestClassLoader extends JClassLoader {

    @Override
    public JClass loadClass(String name) {
        byte [] data = getClassBytes(name);
        if (data == null) {
            throw new AssertionError(name);
        }
        assert (data != null);
        JClass jc = new JClass(new DataInputStream(data));
        printSuperClasses(jc);
        return jc;
    }

    public void printSuperClasses(JClass jc) {
        if (jc == null) {
            return;
        }
        System.out.println(jc.getName());
        printSuperClasses(jc.getSuperClass());
        for (JClassConstant intf : jc.getInterfaces()) {
            printSuperClasses(intf.getJClass());
        }
    }

    private static final int MB = 1000 * 1000;
    private static final String [] PATH = { "test-classes/"};
    private static final String RT_PATH = "/usr/lib/jvm/java-7-openjdk-amd64/jre/lib/";
    private static final String RT_JAR = "rt.jar";


    private static byte[] readClassFromJar(String jarPath, String name) {
        try {
            JarFile jf = new JarFile(new File(jarPath));
            JarEntry je = jf.getJarEntry(name);
            return readInputStream(jf.getInputStream(je), (int) je.getSize());
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] readClassFromFile(String path) {
        File f = new File(path);
        try {
            return readInputStream(new FileInputStream(f), (int) f.length());
        } catch (Exception e) {
            return null;
        }
    }

    public static byte[] getClassBytes(String className)
            throws IllegalArgumentException {
        byte[] data = null;
        for (String dir : PATH){
            String path = dir + className + ".class";
            data = readClassFromFile(path);
            if (data != null) {
                return data;
            }
        }
        data = readClassFromJar(RT_PATH + RT_JAR, className + ".class");
        return data;
    }

    public byte[] getClassFile(String className)
            throws IllegalArgumentException {
        return getClassBytes(className);
    }

    private static byte[] readInputStream(InputStream is, int size) {
        byte ret[] = new byte[size];
        if (size > 10 * MB)
            return null;

        int off = 0, n;
        while (off < size) {
            try {
                n = is.read(ret, off, size - off);
            } catch (IOException e) {
                return null;
            }
            off += n;
        }
        return ret;
    }

}
