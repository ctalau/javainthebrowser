package jib.client;

import gwtjava.io.fs.FileSystem;
import gwtjava.lang.System;
import javac.com.sun.tools.javac.Javac;
import jvm.main.JVM;

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
        debugMilestone("GWT module loaded - Jib.onModuleLoad() called");

        final TextArea log = new TextArea();
        RootPanel.get("log-div").add(log);
        log.addStyleName("logBox");
        log.setVisibleLines(10);
        log.setReadOnly(true);
        System.setOut(new TextAreaPrintStream(log));
        System.setErr(System.out);

        debugSuccess("Output text area initialized and PrintStream redirected");

        final Button runButton = new Button("Run!");
        RootPanel.get("btn-div").add(runButton);
        runButton.addStyleName("runButton");

        debugSuccess("Run button created and attached to DOM");

        runButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                debugMilestone("=== RUN BUTTON CLICKED - Starting execution ===");
                try {
                    log.setValue("");
                    debugInfo("Output log cleared");

                    debugInfo("Retrieving source code from editor");
                    String code = getSourceCode();
                    debugSuccess("Source code retrieved - " + code.length() + " characters");

                    debugInfo("Extracting class name from source code");
                    String className = Javac.getClassName(code);
                    debugSuccess("Class name extracted: " + className);

                    debugMilestone("Starting Java compilation for: " + className + ".java");
                    boolean ok = Javac.compile(className + ".java", code);

                    if (ok) {
                        debugSuccess("Compilation successful!");
                        System.out.println("Compiled!");

                        debugInfo("Reading compiled .class file from filesystem");
                        byte[] classFile = fs.readFile(fs.cwd() + className + ".class");
                        debugSuccess("Class file read - " + classFile.length + " bytes");

                        printMagic(classFile);
                        System.out.println("Output:");

                        debugMilestone("Setting up JVM class loader");
                        JVM.setClassLoader(new JibClassLoader());
                        debugSuccess("JibClassLoader installed");

                        debugMilestone("Starting JVM execution of: " + className);
                        JVM.run(className);
                        debugSuccess("JVM execution completed");

                    } else {
                        debugError("Compilation failed - check output for errors");
                    }
                } catch (Exception e) {
                    debugError("Unhandled Java exception in Run process: " + e.getClass().getName() + " - " + e.getMessage());
                    String stackTrace = getStackTrace(e);
                    debugError("Stack trace: " + stackTrace);
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void printMagic(byte [] bytecode) {
        System.out.print("Magic code: 0x");
        for (int i = 0; i < 4; i++) {
            System.out.print(Integer.toHexString(bytecode[i] & 0xFF));
        }
        System.out.println();
    }

    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] elements = e.getStackTrace();
        for (int i = 0; i < Math.min(elements.length, 10); i++) {
            if (i > 0) sb.append(" | ");
            sb.append(elements[i].toString());
        }
        return sb.toString();
    }

    // Debug logging methods using JSNI to call JavaScript debug logging
    private native void debugInfo(String message) /*-{
        if ($wnd.debugLog) {
            $wnd.debugLog.info(message);
        }
    }-*/;

    private native void debugSuccess(String message) /*-{
        if ($wnd.debugLog) {
            $wnd.debugLog.success(message);
        }
    }-*/;

    private native void debugError(String message) /*-{
        if ($wnd.debugLog) {
            $wnd.debugLog.error(message);
        }
    }-*/;

    private native void debugWarning(String message) /*-{
        if ($wnd.debugLog) {
            $wnd.debugLog.warning(message);
        }
    }-*/;

    private native void debugMilestone(String message) /*-{
        if ($wnd.debugLog) {
            $wnd.debugLog.milestone(message);
        }
    }-*/;

    private native String getSourceCode() /*-{
        return $wnd.editor.getCode();
    }-*/;
}
