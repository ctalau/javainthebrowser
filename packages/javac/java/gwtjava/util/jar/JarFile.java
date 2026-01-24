package gwtjava.util.jar;

import gwtjava.io.File;
import gwtjava.io.IOException;

public class JarFile {

    public JarFile(File file) throws IOException {
    }

    public Manifest getManifest() throws IOException {
        return new Manifest();
    }

    public void close() throws IOException {
    }

}
