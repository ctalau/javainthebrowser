package jib.client;

import gwtjava.io.IOException;
import gwtjava.io.PrintStream;

import com.google.gwt.user.client.ui.TextArea;

public class TextAreaPrintStream extends PrintStream {
    private TextArea textArea;
    public TextAreaPrintStream(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void print(Object string) {
        textArea.setValue(textArea.getValue() + String.valueOf(string));
    }
}
