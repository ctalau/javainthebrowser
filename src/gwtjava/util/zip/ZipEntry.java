package gwtjava.util.zip;

public class ZipEntry {

    java.util.zip.ZipEntry entry;

    public ZipEntry(java.util.zip.ZipEntry entry) {
        this.entry = entry;
    }

    public String getName() {
        return entry.getName();
    }

    public long getTime() {
        return entry.getTime();
    }

}
