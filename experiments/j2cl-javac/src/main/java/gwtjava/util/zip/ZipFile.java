package gwtjava.util.zip;

import gwtjava.io.File;
import gwtjava.io.IOException;
import gwtjava.io.InputStream;

import java.util.Enumeration;

public class ZipFile {

    public ZipFile(File zipFile) throws IOException {
        throw new UnsupportedOperationException();
    }

    public Enumeration<? extends ZipEntry> entries() {
        throw new UnsupportedOperationException();
    }

    public void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    public String getName() {
        throw new UnsupportedOperationException();
    }

    public InputStream getInputStream(ZipEntry entry) {
        throw new UnsupportedOperationException();
    }

    public ZipEntry getEntry(String path) {
        throw new UnsupportedOperationException();
    }

}
