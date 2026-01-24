package gwtjava.util.logging;

import gwtjava.util.Collections;

import java.util.List;

public class Logger {

    public static Logger getLogger(String name) {
        return new Logger();
    }

    public Logger getParent() {
        return this;
    }

    public void setLevel(String all) {
    }

    public List<Handler> getHandlers() {
        return Collections.emptyList();
    }

}
