package gwtjava.net;

import gwtjava.io.InputStream;

public class URL {

    java.net.URL jurl;
    public URL(URL url, String fullName) throws MalformedURLException {
        try {
            this.jurl = new java.net.URL(url.jurl, fullName);
        } catch (java.net.MalformedURLException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    public URL(String string, String string2, String name) throws MalformedURLException {
        try {
            this.jurl = new java.net.URL(string, string2, name);
        } catch (java.net.MalformedURLException e) {
            throw new MalformedURLException(e.getMessage());
        }
    }

    URL(java.net.URL url) {
        this.jurl = url;
    }


    public InputStream openStream() {
        throw new UnsupportedOperationException();
    }

}
