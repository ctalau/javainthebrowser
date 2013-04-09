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
        if (parent == null) {
            this.jfile = new java.io.File(name);
        } else {
            this.jfile = new java.io.File(parent.jfile, name);
        }
    }

    public File(String parent, String name) {
        this(new java.io.File(parent, name));
    }

    public File(java.io.File file) {
        if (file == null) {
            throw new NullPointerException();
        }
        this.jfile = file;
//        if (file.exists());
//            System.out.println(file.getAbsolutePath());
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
                files[i] = (jfiles[i] == null) ? null : new File(jfiles[i]);
            }
            return files;
        }
    }

    public boolean isDirectory() {
        return jfile.isDirectory();
    }

    public File getParentFile() {
        java.io.File parent = jfile.getParentFile();
        if (parent == null) {
            return null;
        }
        return new File(parent);
    }

    public boolean exists() {
        return jfile.exists();
    }

    public boolean canWrite() {
        return jfile.canWrite();
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
        java.io.File absolute = jfile.getAbsoluteFile();
        if (absolute == null) {
            return null;
        }
        return new File(absolute);
    }

    public String getParent() {
        return jfile.getParent();
    }

    public File getCanonicalFile() throws IOException {
        try {
            java.io.File canonical = jfile.getCanonicalFile();
            if (canonical == null) {
                return null;
            }
            return new File(canonical);
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
