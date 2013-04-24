package jib.client;

import gwtjava.io.IOException;
import gwtjava.io.PrintStream;
import gwtjava.io.fs.FileSystem;
import gwtjava.lang.System;
import javac.com.sun.tools.javac.Main;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.TextAreaElement;
import com.google.gwt.user.client.DOM;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Jib implements EntryPoint {

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {

        setOut();
        log("starting");
        try {
            Main.compile("class Smth { String f() { return \"\"; }}");
            if (FileSystem.instance().exists("/tmp/Smth.class")) {
                log("it worked");
            } else {
                log("it didn't work");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void log(String s) {
        Element elem = DOM.getElementById("log");
        TextAreaElement textArea = (TextAreaElement) elem;
        textArea.setValue(textArea.getValue() + "\n" + s);
    }

    public void setOut() {
        System.setOut(new PrintStream() {
            @Override
            public void print(Object string) {
                log(String.valueOf(string));
            }

            @Override
            public void flush() throws IOException {
            }

            @Override
            public void close() {
            }
        });
    }
}
