package jib.client;

import gwtjava.io.fs.FileSystem;
import gwtjava.lang.System;
import javac.com.sun.tools.javac.Main;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Jib implements EntryPoint {
    FileSystem fs = FileSystem.instance();

    /**
     * This is the entry point method.
     */
    @Override
    public void onModuleLoad() {
        final Button runButton = new Button("Run!");
        final TextArea source = new TextArea();
        final TextArea log = new TextArea();


        RootPanel.get("source-div").add(source);
        source.setCharacterWidth(80);
        source.setVisibleLines(25);

        System.setOut(new TextAreaPrintStream(log));
        RootPanel.get("log-div").add(log);
        log.setCharacterWidth(80);
        log.setVisibleLines(25);

        RootPanel.get().add(runButton);
        runButton.addStyleName("runButton");

        runButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                log.setValue("");
                String code = source.getValue();
                String className = Main.getClassName(code);

                Main.compile(className + ".java", code);

                String classFile = fs.cwd() + className + ".class";
                if (fs.exists(classFile)) {
                    byte [] bytecode = fs.readFile(classFile);
                    System.out.print("Magic number: 0x");
                    for (int i = 0; i < 8; i++) {
                        System.out.print(""+(char)bytecode[i]);
                    }
                    System.out.println();
                } else {
                    System.out.println("It didn't work!");
                }
            }
        });
    }
}
