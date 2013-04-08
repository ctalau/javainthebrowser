package gwtjava.io;

import gwtjava.net.URI;
import gwtjava.net.URISyntaxException;

public class File {

    public static final char separatorChar = '/';
    public static final String separator = "/";
    public static final String pathSeparator = ":";
    public static final char pathSeparatorChar = ':';

    public java.io.File jfile;
    public File(String name) {
        this(new java.io.File(name));
    }

    public File(File parent, String name) {
        this(new java.io.File(parent.jfile, name));
    }

    public File(String parent, String name) {
        this(new java.io.File(parent, name));
    }

    public File(java.io.File file) {
        this.jfile = file;
       // System.out.println(file.getAbsolutePath() + " " + file.exists());
    }

    public String getName() {
        return jfile.getName();
    }

    public File[] listFiles() {
        java.io.File [] jfiles = jfile.listFiles();
        if (jfiles == null) {
            return null;
        } else {
            File [] files = new File[jfiles.length];

            for (int i = 0; i < jfiles.length; i++) {
                files[i] = new File(jfiles[i]);
            }
            return files;
        }
    }

    public boolean isDirectory() {
        return jfile.isDirectory();
    }

    public File getParentFile() {
        return new File(jfile.getParent());
    }

    public boolean exists() {
        return jfile.exists();
    }

    public boolean canWrite() {
        return jfile.exists();
    }

    public boolean isAbsolute() {
        return jfile.isAbsolute();
    }

    public String getPath() {
        return jfile.getPath();
    }

    public URI toURI() {
        try {
            return new URI(jfile.toURI().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCanonicalPath() throws IOException {
        try {
            return jfile.getCanonicalPath();
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public boolean isFile() {
        return jfile.isFile();
    }

    public String getAbsolutePath() {
        return jfile.getAbsolutePath();
    }

    public long lastModified() {
        return jfile.lastModified();
    }

    public long length() {
        return jfile.length();
    }

    public File getAbsoluteFile() {
        return new File(jfile.getAbsoluteFile());
    }

    public String getParent() {
        return jfile.getParent();
    }

    public File getCanonicalFile() throws IOException {
        try {
            return new File(jfile.getCanonicalFile());
        } catch (java.io.IOException e) {
            throw new IOException(e.getMessage());
        }
    }

    public boolean delete() {
        return jfile.delete();
    }

    public boolean mkdirs() {
        return jfile.mkdirs();
    }

}
