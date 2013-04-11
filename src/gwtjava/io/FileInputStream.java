package gwtjava.io;

public class FileInputStream extends ByteArrayInputStream {

    public FileInputStream(File file) throws FileNotFoundException {
        super(readFile(file.jfile));
        System.out.println(file.getAbsolutePath());
    }

    private static byte [] readFile(java.io.File file) throws FileNotFoundException {
        try {
            byte [] content = new byte[(int) file.length()];
            int read = new java.io.FileInputStream(file).read(content);
            assert (read == file.length());
            return content;
        } catch (java.io.IOException e) {
            throw new FileNotFoundException(e.getMessage());
        }
    }
}
