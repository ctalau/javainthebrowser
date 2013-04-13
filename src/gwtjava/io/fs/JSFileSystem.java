package gwtjava.io.fs;

import gwtjava.io.File;
import gwtjava.io.IOException;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class JSFileSystem extends FileSystem {

    private final Map<String, byte[]> files = new HashMap<String, byte[]>();

    {
        try {
            reset();
            addFile(new java.io.File("Basic.java"));
            addFile(new java.io.File("Error.java"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void addFile(java.io.File file)
            throws FileNotFoundException, java.io.IOException {
        byte[] content = new byte[(int) file.length()];
        new java.io.RandomAccessFile(file, "r").readFully(content);
        files.put(File.canonical(file.getAbsolutePath()), content);
   }

    @Override
    public void reset() throws FileNotFoundException {
        for (Map.Entry<String, String> entry : FileSystemContent.files.entrySet()) {
            files.put("/jre/" + entry.getKey(), hexDecode(entry.getValue()));
        }
    }

    private byte[] hexDecode(String s) {
        byte[] ret = new byte[s.length()/2];
        for (int i = 0; i < s.length(); i += 2) {
            ret[i/2] = (byte) Integer.parseInt(s.substring(i, i+2), 16);
        }
        return ret;
    }

    @Override
    public byte[] readFile(String path) {
        return files.get(path);
    }

    @Override
    public void writeFile(String path, byte[] content, int off, int len)
            throws IOException {
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
        HashSet<File> children = new HashSet<File>();
        for (String fileName : files.keySet()) {
            if (fileName.startsWith(path)) {
                String relPath = fileName.substring(path.length() + 1);
                int index = relPath.indexOf(File.separatorChar);
                if (index > 0) {
                    children.add(new File(path + File.separator
                            + relPath.substring(0, index)));
                } else {
                    children.add(new File(fileName));
                }
            }
        }
        return children.toArray(new File[0]);
    }

    @Override
    public boolean exists(String name) {
        for (String fileName : files.keySet()) {
            if (fileName.startsWith(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isFile(String path) {
        return files.containsKey(path);
    }

    @Override
    public boolean isDirectory(String path) {
        return exists(path) && !isFile(path);
    }

    @Override
    public long length(String path) {
        return files.get(path).length;
    }
}
