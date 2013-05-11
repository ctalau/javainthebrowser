package jvm.execution.objrepr;

import java.util.HashMap;

import jvm.classparser.JClass;
import jvm.classparser.JType;
import jvm.classparser.JMember.JMethod;
import jvm.classparser.jconstants.JClassConstant;
import jvm.classparser.jconstants.JMemberConstant;
import jvm.execution.JClassLoader;
import jvm.execution.JClassLoader.JClassNotInitializedException;


/**
 * The way to access static members
 *
 * TODO: lookup members 'per class'
 *
 * @author ctalau
 */
public class StaticMembers {
    private static HashMap<String, Object> global = new HashMap<String, Object>();
    private static HashMap<String, JMethod> funct = new HashMap<String, JMethod>();

    public static void reset() {
        global.clear();
        funct.clear();
    }

    public static void putStaticField(JMemberConstant fld, Object value) throws JClassNotInitializedException {
        ensureInitialized(fld.getClassConstant());
        global.put(fld.getFullName(), value);
    }

    public static Object getStaticField(JMemberConstant fld) throws JClassNotInitializedException {
        ensureInitialized(fld.getClassConstant());
        if (!global.containsKey(fld.getFullName())) {
            global.put(fld.getFullName(), JType.getDefaultValue(fld.getType()));
        }
        return global.get(fld.getFullName());
    }

    public static JMethod dispatchMethod(JMemberConstant cm) throws JClassNotInitializedException {
        ensureInitialized(cm.getClassConstant());
        JMethod m = funct.get(cm.getFullName());

        if (m == null) {
            JClassLoader jcl = JClassLoader.getInstance();
            JClass jc = jcl.getClassByConstant(cm.getClassConstant());
            for (JMethod im : jc.getMethods()) {
                if (im.isStatic()) {
                    funct.put(im.getFullName(), im);
                }
            }
            m = funct.get(cm.getFullName());
        }

        return m;
    }

    private static void ensureInitialized(JClassConstant jcc) throws  JClassNotInitializedException {
        JClassLoader jcl = JClassLoader.getInstance();
        JClass jc = jcl.getClassByConstant(jcc);
        jc.ensureInitialized();
    }
}
