package jvm.util;

/**
 * Reimplementation of java.io.DataInputStream, but just reads data from a byte
 * array.
 *
 * @author ctalau
 *
 */
public class DataInputStream {
    private byte[] data;
    private int pos;

    public DataInputStream(byte[] data) {
        this.data = data;
        this.pos = 0;
    }

    // XXX the int is actually signed
    public static int readUInt(byte [] data, int pos){
        int ch1 = data[pos++] & 0xFF;
        int ch2 = data[pos++] & 0xFF;
        int ch3 = data[pos++] & 0xFF;
        int ch4 = data[pos++] & 0xFF;
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    public int readUInt() {
        int ret = readUInt(data,pos);
        pos += 4;
        return ret;
    }

    public static int readShort(byte [] data, int pos){
        int ch1 = data[pos++] & 0xFF;
        int ch2 = data[pos++] & 0xFF;
        return (short)((ch1 << 8) | (ch2 << 0));
    }

    public static int readUShort(byte [] data, int pos){
        int ch1 = data[pos++] & 0xFF;
        int ch2 = data[pos++] & 0xFF;
        return ((ch1 << 8) + (ch2 << 0));
    }

    public int readUShort() {
        int ret = readUShort(data, pos);
        pos += 2;
        return ret;
    }

    public int read() {
        return data[pos++] & 0xFF;
    }

    public void read(byte[] buf) {
        for (int i = 0; i < buf.length; i++)
            buf[i] = data[pos++];
    }


    public static long readLong(byte [] data, int pos){
        long high = readUInt(data, pos) & 0xFFFFFFFFL;
        pos += 4;
        long low  = readUInt(data, pos) & 0xFFFFFFFFL;
        return (high << 32) + low;

    }
    public long readLong() {
        long ret = readLong(data, pos);
        pos += 8;
        return ret;
    }

    public double readDouble() {
        double value;
        long lvalue = readLong();
        if (lvalue == 0x7f800000) {
            value = Double.POSITIVE_INFINITY;
        } else if (lvalue == 0xff800000) {
            value = Double.NEGATIVE_INFINITY;
        } else if ((lvalue >= 0x7f800001 && lvalue <= 0x7fffffff)
                || (lvalue >= 0xff800001 && lvalue <= 0xffffffff)) {
            value = Double.NaN;
        } else {
            int s = ((lvalue >> 63) == 0) ? 1 : -1;
            int e = (int) ((lvalue >> 52) & 0x7ffL);
            long m = (e == 0) ? (lvalue & 0xfffffffffffffL) << 1
                    : (lvalue & 0xfffffffffffffL) | 0x10000000000000L;

            value = (double) (s * m * Math.pow(2, e - 1075));
        }
        return value;

    }

    public float readFloat(){
        float value;
        int ivalue = readUInt();
        if (ivalue == 0x7f800000) {
            value = Float.POSITIVE_INFINITY;
        } else if (ivalue == 0xff800000) {
            value = Float.NEGATIVE_INFINITY;
        } else if ((ivalue >= 0x7f800001 && ivalue <= 0x7fffffff)
                || (ivalue >= 0xff800001 && ivalue <= 0xffffffff)) {
            value = Float.NaN;
        } else {
            int s = ((ivalue >> 31) == 0) ? 1 : -1;
            int e = ((ivalue >> 23) & 0xff);
            int m = (e == 0) ? (ivalue & 0x7fffff) << 1
                    : (ivalue & 0x7fffff) | 0x800000;

            value = (float) (s * m * Math.pow(2, e - 150));
        }
        return value;
    }


    public void skip(int n){
        pos += n;
    }
}
