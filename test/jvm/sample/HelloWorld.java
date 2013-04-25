package jvm.sample;

import java.io.ConsolePrintStream;

public class HelloWorld {
    public static void main(String[] args) {
        System.setOut(new ConsolePrintStream());
        System.out.println("Hello, World!");
    }
}
