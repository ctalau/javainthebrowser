package jvm.main;

import jvm.execution.ExecutionEngine;
import jvm.execution.JClassLoader;

public class JVM {
    public static void setClassLoader(JClassLoader jcl) {
        JClassLoader.setInstance(jcl);
    }

    public static void run(String path) {
        ExecutionEngine exec = new ExecutionEngine();
        exec.bootstrap(path);
    }
}
