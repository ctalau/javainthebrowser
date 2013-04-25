package jvm.execution.objrepr.java;

import static org.junit.Assert.*;

import org.junit.Test;

import jvm.TestClassLoader;
import jvm.classparser.JClass;
import jvm.classparser.jconstants.JClassConstant;
import jvm.classparser.jconstants.JMemberConstant;
import jvm.execution.JClassLoader;
import jvm.execution.objrepr.ObjectFactory;
import jvm.execution.objrepr.ObjectRepr;

public class JavaObjectReprTest {

    JClassLoader jcl = new TestClassLoader();

    @Test
    public void testTemplateFields() {
        JClassLoader.setInstance(jcl);
        String className = "java/lang/StringBuilder";
        JClass jc = jcl.getClassByName(className);

        ObjectRepr obj = ObjectFactory.newObject(jc);
        JMemberConstant jm = new JMemberConstant(new JClassConstant(className), "count", "I");
        assertEquals(0, obj.getField(jm));
    }

}
