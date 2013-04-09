package gwtjava.util.jar;


import gwtjava.io.File;
import gwtjava.io.IOException;

public class JarFile {

    java.util.jar.JarFile jfile;
    public JarFile(File file) throws IOException {
        try {
            jfile = new java.util.jar.JarFile(file.jfile);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public Manifest getManifest() throws IOException {
        try {
            return new Manifest(jfile.getManifest());
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void close() throws IOException {
        try {
            jfile.close();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

}
