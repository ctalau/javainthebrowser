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
        final TextArea log = new TextArea();
        System.setOut(new TextAreaPrintStream(log));
        System.setErr(System.out);
        RootPanel.get("log-div").add(log);
        log.setCharacterWidth(80);
        log.setVisibleLines(25);
        log.setReadOnly(true);

        final Button runButton = new Button("Run!");
        RootPanel.get().add(runButton);
        runButton.addStyleName("runButton");

        runButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                log.setValue("");
                String code = getSourceCode();
                String className = Main.getClassName(code);

                Main.compile(className + ".java", code);

                String classFile = fs.cwd() + className + ".class";
                if (fs.exists(classFile)) {
                    byte [] bytecode = fs.readFile(classFile);
                    System.out.print("Magic number: 0x");
                    for (int i = 0; i < 4; i++) {
                        System.out.print(Integer.toHexString(bytecode[i] & 0xFF));
                    }
                    System.out.println();
                } else {
                    System.out.println("It didn't work!");
                }
            }
        });
    }

    private native String getSourceCode() /*-{
        return $wnd.editor.getCode();
    }-*/;
}
