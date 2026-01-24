package javac.client;

import com.google.gwt.core.client.EntryPoint;
import gwtjava.io.fs.FileSystem;
import javac.com.sun.tools.javac.Javac;

public class JavacEntryPoint implements EntryPoint {
    private static final char[] BASE64_ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();

    @Override
    public void onModuleLoad() {
        exportApi();
    }

    public boolean compile(String fileName, String source) {
        return Javac.compile(fileName, source);
    }

    public String getClassName(String source) {
        return Javac.getClassName(source);
    }

    public String readClassFileBase64(String className) {
        String path = FileSystem.instance().cwd() + className + ".class";
        byte[] bytes = FileSystem.instance().readFile(path);
        if (bytes == null) {
            return null;
        }
        return toBase64(bytes);
    }

    private String toBase64(byte[] data) {
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

    private native void exportApi() /*-{
        var self = this;
        var api = {
            compileSource: function(source, fileNameOverride) {
                var className = self.@javac.client.JavacEntryPoint::getClassName(Ljava/lang/String;)(source);
                var fileName = fileNameOverride ? fileNameOverride : className + ".java";
                var success = self.@javac.client.JavacEntryPoint::compile(Ljava/lang/String;Ljava/lang/String;)(fileName, source);
                var classFileBase64 = success
                    ? self.@javac.client.JavacEntryPoint::readClassFileBase64(Ljava/lang/String;)(className)
                    : null;
                return {
                    success: success,
                    className: className,
                    classFileBase64: classFileBase64
                };
            },
            getClassName: function(source) {
                return self.@javac.client.JavacEntryPoint::getClassName(Ljava/lang/String;)(source);
            }
        };
        if ($wnd.__javaInTheBrowserJavacReady) {
            $wnd.__javaInTheBrowserJavacReady(api);
        }
    }-*/;
}
