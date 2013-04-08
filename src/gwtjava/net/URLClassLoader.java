package gwtjava.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class URLClassLoader extends ClassLoader {

    java.net.URLClassLoader jcl;
    public URLClassLoader(URL[] urls, ClassLoader parent) {
        java.net.URL [] jurls = new java.net.URL [urls.length];
        for (int i = 0; i < urls.length; i++) {
            jurls[i] = urls[i].jurl;
        }
        jcl = new java.net.URLClassLoader(jurls);
    }

    @Override
    public java.net.URL getResource(String name) {
        return jcl.getResource(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return jcl.getResourceAsStream(name);
    }

    @Override
    public Enumeration<java.net.URL> getResources(String name)
            throws IOException {
        return jcl.getResources(name);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return jcl.loadClass(name);
    }
}
