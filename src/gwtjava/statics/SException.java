package gwtjava.statics;

import gwtjava.io.PrintStream;
import gwtjava.io.PrintWriter;

public class SException {

    public static void printStackTrace(Throwable ex, PrintStream ps) {
        ps.println(ex.getClass() + ": " + ex.getMessage());
        for (StackTraceElement elt : ex.getStackTrace()) {
            ps.println(elt.toString());
        }
        if (ex.getCause() != null) {
            ps.println("Cause:");
            printStackTrace(ex.getCause(), ps);
        }
    }

    public static void printStackTrace(Throwable ex, PrintWriter ps) {
        ps.println(ex.getClass() + ": " + ex.getMessage());
        for (StackTraceElement elt : ex.getStackTrace()) {
            ps.println(elt.toString());
        }
        if (ex.getCause() != null) {
            ps.println("Cause:");
            printStackTrace(ex.getCause(), ps);
        }
    }

}
