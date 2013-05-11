package jvm.main;

import jvm.execution.ExecutionEngine;
import jvm.execution.JClassLoader;
import jvm.execution.objrepr.StaticMembers;

public class JVM {
    public static void setClassLoader(JClassLoader jcl) {
        JClassLoader.setInstance(jcl);
    }

    public static void run(String path) {
        StaticMembers.reset();
        ExecutionEngine.getInstance().bootstrap(path);
    }
}
