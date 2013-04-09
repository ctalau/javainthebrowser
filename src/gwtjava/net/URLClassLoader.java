package gwtjava.net;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;

public class URLClassLoader extends ClassLoader {

    java.net.URLClassLoader jcl;
    public URLClassLoader(URL[] urls, ClassLoader parent) {
        java.net.URL [] jurls = new java.net.URL [urls.length];
        for (int i = 0; i < urls.length; i++) {
            try {
                jurls[i] = new java.net.URL(urls[i].url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        jcl = new java.net.URLClassLoader(jurls);
    }

    @Override
    public Enumeration<java.net.URL> getResources(String name)
            throws IOException {
        return jcl.getResources(name);
    }
}
