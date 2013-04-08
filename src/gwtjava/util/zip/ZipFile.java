package gwtjava.util.zip;

import gwtjava.io.File;
import gwtjava.io.IOException;
import gwtjava.io.InputStream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class ZipFile {
    java.util.zip.ZipFile jfile;

    public ZipFile(File zipFile) throws IOException {
        try {
            jfile = new java.util.zip.ZipFile(zipFile.jfile);
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public Enumeration<? extends ZipEntry> entries() {
        Enumeration<? extends java.util.zip.ZipEntry> jentries = jfile.entries();
        ArrayList<ZipEntry> list = new ArrayList<ZipEntry>();
        while(jentries.hasMoreElements()) {
            list.add(new ZipEntry(jentries.nextElement()));
        }
        return Collections.enumeration(list);
    }

    public void close() throws IOException {
        try {
            jfile.close();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public String getName() {
        // TODO Auto-generated method stub
        return jfile.getName();
    }

    public InputStream getInputStream(ZipEntry entry) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException();
    }

    public ZipEntry getEntry(String path) {
        return new ZipEntry(new java.util.zip.ZipEntry(path));
    }

}
