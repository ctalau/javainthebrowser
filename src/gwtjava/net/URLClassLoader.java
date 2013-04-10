package gwtjava.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import gwtjava.lang.ClassLoader;

public class URLClassLoader extends ClassLoader {

    public URLClassLoader(URL[] urls, ClassLoader parent) {
        super(getClassLoader(urls, parent));
    }

    private static java.net.URLClassLoader getClassLoader(URL[] urls, ClassLoader parent) {
        java.net.URL [] jurls = new java.net.URL [urls.length];
        for (int i = 0; i < urls.length; i++) {
            try {
                jurls[i] = new java.net.URL(urls[i].url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return new java.net.URLClassLoader(jurls, parent.jcl);
    }
    public Enumeration<java.net.URL> getResources(String name)
            throws IOException {
        System.out.println(jcl);
        return jcl.getResources(name);
    }
}
