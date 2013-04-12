package gwtjava.io.fs;

import java.util.ArrayList;

import gwtjava.io.File;

import gwtjava.io.IOException;

class JVMFileSystem extends FileSystem {

    @Override
    public void reset() {
    }

    @Override
    public byte[] readFile(String path) {
        java.io.File file = new java.io.File(path);
        byte[] content = new byte[(int) file.length()];
        try {
            new java.io.RandomAccessFile(file, "r").readFully(content);
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return content;
    }

    @Override
    public void writeFile(String path, byte[] content, int off, int len)
            throws gwtjava.io.IOException {
        try {
            java.io.FileOutputStream jfos = new java.io.FileOutputStream(path);
            jfos.write(content, off, len);
            jfos.close();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }

    }

    @Override
    public String cwd() {
        return new java.io.File(".").getAbsolutePath();
    }

    @Override
    public File[] listFiles(String path) {
        ArrayList<File> ret = new ArrayList<File>();
        java.io.File [] files = new java.io.File(path).listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                ret.add(new File(file.getAbsolutePath()));
            }
            return ret.toArray(new File[0]);
        }
        return null;
    }

    @Override
    public boolean exists(String name) {
        return new java.io.File(name).exists();
    }

    @Override
    public boolean isFile(String path) {
        return new java.io.File(path).isFile();
    }

    @Override
    public boolean isDirectory(String path) {
        return new java.io.File(path).isDirectory();
    }

    @Override
    public long length(String path) {
        return new java.io.File(path).length();
    }

}
