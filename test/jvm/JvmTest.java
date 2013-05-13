package jvm;

import gwtjava.io.fs.FileSystem;
import jib.client.JibClassLoader;
import jvm.execution.ExecutionEngine;
import jvm.execution.JClassLoader;

import org.junit.Test;

public class JvmTest {
    @Test
    public void testNativeFS() throws InstantiationException, IllegalAccessException {
        run("jvm/sample/HelloWorld", TestClassLoader.class);
    }

    @Test
    public void testEmultatedFS() throws InstantiationException, IllegalAccessException {
        FileSystem.instance().reset();
        run("jvm/sample/HelloWorld", JibClassLoader.class);
    }

    @Test
    public void testRestart() throws InstantiationException, IllegalAccessException {
        FileSystem.instance().reset();
        run("jvm/sample/HelloWorld", JibClassLoader.class);
        run("jvm/sample/HelloWorld", JibClassLoader.class);
    }

    private void run(String name, Class<? extends JClassLoader> cls) throws InstantiationException, IllegalAccessException{
        JClassLoader.setInstance(cls.newInstance());
        ExecutionEngine exec = new ExecutionEngine();
        exec.bootstrap(name);
    }
}
