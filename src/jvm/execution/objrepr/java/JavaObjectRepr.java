package jvm.execution.objrepr.java;

import java.util.HashMap;
import java.util.Map.Entry;

import jvm.classparser.JClass;
import jvm.classparser.JMember.JField;
import jvm.classparser.JMember.JMethod;
import jvm.classparser.jconstants.JMemberConstant;
import jvm.execution.JClassLoader;
import jvm.execution.objrepr.ObjectRepr;


/**
 * Unoptimized Java-based runtime representation for objects. Hash lookups can
 * be replaced by array indexing by some pre-processing of code.
 *
 * This representation is not usefull to use GWT JSNI.
 *
 * @author ctalau
 */
public class JavaObjectRepr implements ObjectRepr  {
    private static HashMap<String, JavaObjectRepr> cache = new HashMap<String, JavaObjectRepr>();

    private HashMap<String, Object> objTmpl = new HashMap<String, Object>();
    private HashMap<String, JMethod> dynamicDispatchTable = new HashMap<String, JMethod>();
    private HashMap<String, JMethod> staticDispatchTable = new HashMap<String, JMethod>();
    protected JClass jc;

    private String resolve(JMemberConstant fld) {
        JClass cls = fld.getClassConstant().getJClass();
        while (cls != null) {
            JField found = null;
            for (JField field : cls.getFields()) {
                if (field.getMemberName().equals(fld.getMemberName())) {
                    found = field;
                    break;
                }
            }
            if (found != null) {
                return found.getFullName();
            }
            cls = cls.getSuperClass();
        }
        throw new AssertionError("Field not resolved: " + fld.getFullName());
    }

    @Override
    public Object getField(JMemberConstant fld) {
        return objTmpl.get(resolve(fld));
    }

    @Override
    public void putField(JMemberConstant fld, Object value) {
        objTmpl.put(resolve(fld), value);
    }

    @Override
    public JMethod dispatchMethod(JMemberConstant cm) {
        return dynamicDispatchTable.get(cm.getMemberName());
    }

    @Override
    public JMethod dispatchSuperMethod(JMemberConstant cm) {
        JavaObjectRepr superObj = getCachedJavaObject(jc.getSuperClass());
        return superObj.dispatchMethod(cm);
    }

    @Override
    public JMethod dispatchNonVirtualMethod(JMemberConstant cm) {
        JClassLoader jcl = JClassLoader.getInstance();
        JClass jc = jcl.getClassByConstant(cm.getClassConstant());
        return getCachedJavaObject(jc).staticDispatchTable.get(cm.getMemberName());
    }

    @Override
    public JClass getJClass() {
        return jc;
    }

    /**
     * Create a new object of that class by caching a template object.
     */
    public static JavaObjectRepr newJavaObjectRepr(JClass jc) {
        JavaObjectRepr tmpl = getCachedJavaObject(jc);
        JavaObjectRepr ret = new JavaObjectRepr();

        ret.objTmpl = new HashMap<String, Object>(tmpl.objTmpl);
        ret.dynamicDispatchTable = tmpl.dynamicDispatchTable;
        ret.staticDispatchTable = tmpl.staticDispatchTable;
        ret.jc = tmpl.jc;

        return ret;
    }

    private static JavaObjectRepr getCachedJavaObject(JClass jc) {
        JavaObjectRepr o = cache.get(jc.getName());
        if (o == null) {
            o = new JavaObjectRepr();
            o.putMembers(jc);
            o.jc = jc;
            cache.put(jc.getName(), o);
        }
        return o;
    }

    /**
     * Build template object, static and dynamic dispatch table
     */
    protected void putMembers(JClass c) {
        putMembers_(c);

        for (JMethod m : c.getMethods()) {
            if (m.isStatic())
                continue;
            this.staticDispatchTable.put(m.getMemberName(), m);
        }
    }

    /**
     * Build the template object and the dynamic dispatch table recursively
     */
    private void putMembers_(JClass c) {
        for (JField f : c.getFields()) {
            if (f.isStatic())
                continue;

            this.objTmpl.put(f.getFullName(), f.getInitValue());
        }

        for (JMethod m : c.getMethods()) {
            if (m.isStatic())
                continue;

            if (this.dynamicDispatchTable.get(m.getMemberName()) == null) {
                this.dynamicDispatchTable.put(m.getMemberName(), m);
            }
            // else - the method is overriden
        }

        if (c.getSuperClass() != null)
            putMembers_(c.getSuperClass());
    }

    @Override
    public String toString() {
        String ret = this.jc.getName() + "-{";
        for (Entry<String, Object> e : this.objTmpl.entrySet()) {
            ret += e.getKey() + " : " + e.getValue() + ",";
        }
        ret += "}";
        return ret;
    }

}
