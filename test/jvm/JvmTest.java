package jvm;

import gwtjava.io.fs.FileSystem;
import jib.client.JibClassLoader;
import jvm.execution.ExecutionEngine;
import jvm.execution.JClassLoader;

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
        JClassLoader.setInstance(new JibClassLoader());
        run("jvm/sample/HelloWorld");
    }

    private void run(String name){
        ExecutionEngine.getInstance().bootstrap(name);
    }
}
