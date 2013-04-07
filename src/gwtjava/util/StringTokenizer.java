package gwtjava.util;

// http://docs.oracle.com/javase/1.4.2/docs/api/java/util/StringTokenizer.html
public class StringTokenizer extends java.util.StringTokenizer {

    public StringTokenizer(String docComment, String string) {
        super(docComment, string);
    }

    public StringTokenizer(String path) {
        super(path);
    }

//    public boolean hasMoreTokens() {
//        // TODO Auto-generated method stub
//        return false;
//    }
//
//    public String nextToken() {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    public int countTokens() {
//        // TODO Auto-generated method stub
//        return 0;
//    }

}
