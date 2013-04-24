package jvm.classparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * Decoding JVM representation of types as descriptors.
 *
 * @author ctalau
 */
public class JType {
    public static final int BYTE = 0;
    public static final int CHAR = 1;
    public static final int DOUBLE = 2;
    public static final int FLOAT = 3;
    public static final int INT = 4;
    public static final int LONG = 5;
    public static final int OBJECT = 6;
    public static final int SHORT = 7;
    public static final int BOOLEAN = 8;
    public static final int ARRAY = 9;

    public int type;
    public class PrimitiveType extends JType {
    }

    public class ArrayType extends JType {
        public JType elemType;
    }

    public class RefType extends JType {
        String name;
    }

    public static boolean isReference(String type){
        return type.startsWith("L");
    }

    public static boolean isArray(String type){
        return type.startsWith("[");
    }

    public static boolean isPrimitive(String type){
        return type.length() == 1;
    }

    public static String elemType(String type){
        return type.substring(1);
    }


    public static int retSize(String type){
        type = type.substring(type.indexOf(')') + 1);
        return size(type);
    }

    public static int size(String type){
        switch (type.charAt(0)){
        case 'D':
        case 'J':
            return 2;
        case 'V':
            return 0;
        default:
            return 1;
        }
    }

    private static HashMap<Character, Object> def = new HashMap<Character, Object>();
    private static HashSet<Character> intCastable = new HashSet<Character>();
    static {
        def.put('B', new Byte((byte)0));
        def.put('C', new Character((char)0));
        def.put('D', new Double(0.0));
        def.put('F', new Float(0.0f));
        def.put('I', new Integer(0));
        def.put('J', new Long(0L));
        def.put('S', new Short((short)0));
        def.put('Z', new Integer(0));

        intCastable.add('B');
        intCastable.add('C');
        intCastable.add('S');
    }

    private static String [] names =
        {null, null, null, null, "Z", "C", "F", "D", "B", "S", "I", "J"};

    public static String getArrayElementType(int code){
        return names[code];
    }

    public static Object getDefaultValue(String type){
        return def.get(type.charAt(0));
    }

    public static Object getDefaultElemValue(String type){
        if (intCastable.contains(type.charAt(0))){
            return def.get('I');
        } else {
            return def.get(type.charAt(0));
        }
    }

    public static int argsSize(String type){
        if (type.startsWith("(")) {
            int sum = 0;
            for (int size : argsSizes(type)){
                sum += size;
            }
            return sum;
        } else {
            return size(type);
        }
    }

    public static List<Integer> argsSizes(String type) {
        if (!type.startsWith("("))    // field
            throw new AssertionError(type);

        type = type.substring(1, type.indexOf(')'));

        List<Integer> ret = new ArrayList<Integer>();
        char [] chars = type.toCharArray();
        int pos = 0;

        while (pos < chars.length){
            switch (chars[pos++]){
            case 'B':
            case 'C':
            case 'F':
            case 'I':
            case 'S':
            case 'Z':
                ret.add(1); break;
            case 'D':
            case 'J':
                ret.add(2); break;
            case 'L':
                while (chars[pos] != ';') pos += 1;
                pos++;
                ret.add(1);
                break;
            case '[':
                while (chars[pos] == '[') pos++;
                if (chars[pos] == 'L') {
                    while (chars[pos++] != ';');
                }
                pos++;
                ret.add(1);
            }
        }
        return ret;
    }


}
