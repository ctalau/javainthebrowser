package gwtjava.net;

import gwtjava.lang.ClassLoader;

import java.io.IOException;
import java.util.Enumeration;

public class URLClassLoader extends ClassLoader {
    public URLClassLoader(URL[] urls, ClassLoader parent) {
    }

    public Enumeration<URL> getResources(String name)
            throws IOException {
        throw new UnsupportedOperationException();
    }
}
