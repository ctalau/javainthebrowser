package jvm.execution.objrepr;

import jvm.classparser.JClass;
import jvm.classparser.JMember.JMethod;
import jvm.classparser.jconstants.JMemberConstant;

public abstract class ArrayRepr implements ObjectRepr {

    /** For clone, toString, etc. */
    public abstract JMethod dispatchMethod(JMemberConstant cm);

    public abstract JClass getJClass();

    public abstract Object get(int i);

    public abstract void set(int i, Object val);

    public abstract int length();


    @Override
    public Object getField(JMemberConstant fld) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putField(JMemberConstant fld, Object value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public JMethod dispatchSuperMethod(JMemberConstant cm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public JMethod dispatchNonVirtualMethod(JMemberConstant cm) {
        throw new UnsupportedOperationException();
    }
}
