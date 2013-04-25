package jvm;

import jib.client.JibClassLoader;
import jvm.classparser.JMember.JNativeMethod1;
import jvm.execution.ExecutionEngine;
import jvm.execution.JClassLoader;
import jvm.execution.Natives;

import org.junit.Test;


public class JvmTest {

    private static class NativeConsoleWrite extends JNativeMethod1<Void, Integer> {
        @Override
        public Void call(Integer arg1) {
            System.out.write(arg1);
            System.out.flush();
            return null;
        }
    }

    @Test
    public void test() {
        JClassLoader.setInstance(new JibClassLoader());
        Natives.registerConsoleOut(new NativeConsoleWrite());
        run("Smth");
    }

    private void run(String name){
        ExecutionEngine.getInstance().bootstrap("/tmp/" + name);
    }
}
