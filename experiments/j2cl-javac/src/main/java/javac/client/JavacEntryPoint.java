package javac.client;

import gwtjava.io.fs.FileSystem;
import javac.com.sun.tools.javac.Javac;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(namespace = JsPackage.GLOBAL, name = "JavacCompiler")
public class JavacEntryPoint {
    private static final char[] BASE64_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    @JsMethod
    public static boolean compile(String fileName, String source) {
        return Javac.compile(fileName, source);
    }

    @JsMethod
    public static String getClassName(String source) {
        return Javac.getClassName(source);
    }

    @JsMethod
    public static String readClassFileBase64(String className) {
        String path = FileSystem.instance().cwd() + className + ".class";
        byte[] bytes = FileSystem.instance().readFile(path);
        if (bytes == null) {
            return null;
        }
        return toBase64(bytes);
    }

    @JsMethod
    public static String compileSource(String source, String fileNameOverride) {
        String className = getClassName(source);
        String fileName = (fileNameOverride != null && !fileNameOverride.isEmpty())
                ? fileNameOverride : className + ".java";
        boolean success = compile(fileName, source);
        String classFileBase64 = success ? readClassFileBase64(className) : null;
        // Return a JSON-like string result
        if (success) {
            return "{\"success\":true,\"className\":\"" + className
                    + "\",\"classFileBase64\":\"" + classFileBase64 + "\"}";
        } else {
            return "{\"success\":false,\"className\":\"" + className + "\"}";
        }
    }

    private static String toBase64(byte[] data) {
        StringBuilder builder = new StringBuilder((data.length + 2) / 3 * 4);
        int index = 0;
        while (index < data.length) {
            int b0 = data[index++] & 0xFF;
            int b1 = index < data.length ? data[index++] & 0xFF : -1;
            int b2 = index < data.length ? data[index++] & 0xFF : -1;

            builder.append(BASE64_ALPHABET[b0 >> 2]);
            if (b1 >= 0) {
                builder.append(BASE64_ALPHABET[((b0 & 0x03) << 4) | (b1 >> 4)]);
                if (b2 >= 0) {
                    builder.append(BASE64_ALPHABET[((b1 & 0x0F) << 2) | (b2 >> 6)]);
                    builder.append(BASE64_ALPHABET[b2 & 0x3F]);
                } else {
                    builder.append(BASE64_ALPHABET[(b1 & 0x0F) << 2]);
                    builder.append('=');
                }
            } else {
                builder.append(BASE64_ALPHABET[(b0 & 0x03) << 4]);
                builder.append("==");
            }
        }
        return builder.toString();
    }
}
