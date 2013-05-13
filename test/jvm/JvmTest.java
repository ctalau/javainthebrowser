package jvm;

import gwtjava.io.fs.FileSystem;
import jib.client.JibClassLoader;
import jvm.execution.ExecutionEngine;
import jvm.execution.JClassLoader;
import jvm.execution.objrepr.StaticMembers;

import org.junit.Test;

public class JvmTest {
    @Test
    public void testNativeFS() {
        JClassLoader.setInstance(new TestClassLoader());
        run("jvm/sample/HelloWorld");
    }

    @Test
    public void testEmulatedFS() {
        FileSystem.instance().reset();
        run("jvm/sample/HelloWorld");
        System.out.println();
        System.out.println("*********************************");
        run("jvm/sample/HelloWorld");
    }

    private void run(String name){
        JClassLoader.setInstance(new JibClassLoader());
        StaticMembers.reset();
        ExecutionEngine exec = new ExecutionEngine();
        exec.bootstrap(name);
    }
}
