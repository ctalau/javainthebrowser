package jvm;

import jib.client.JibClassLoader;
import jvm.execution.ExecutionEngine;
import jvm.execution.JClassLoader;

import org.junit.Test;


public class JvmTest {

    @Test
    public void test() {
        JClassLoader.setInstance(new JibClassLoader());
        run("Smth");
    }

    private void run(String name){
        ExecutionEngine.getInstance().bootstrap("/tmp/" + name);
    }
}
