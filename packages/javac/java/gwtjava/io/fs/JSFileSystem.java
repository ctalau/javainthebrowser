package gwtjava.io.fs;

import gwtjava.io.File;
import gwtjava.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class JSFileSystem extends FileSystem {
    private final String USER_PATH = "/tmp/";

    private final Map<String, byte[]> files = new HashMap<String, byte[]>();

    public void addFile(String name, String content) {
        files.put(USER_PATH + name, content.getBytes());
    }

    @Override
    public void reset() {
        files.clear();
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
        byte [] buffer = new byte[len];
        System.arraycopy(content, off, buffer, 0, len);
        files.put(path, buffer);
    }

    @Override
    public String cwd() {
        return USER_PATH;
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
