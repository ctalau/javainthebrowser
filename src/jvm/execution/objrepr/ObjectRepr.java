package jvm.execution.objrepr;

import jvm.classparser.JClass;
import jvm.classparser.JMember.JMethod;
import jvm.classparser.jconstants.JMemberConstant;

public interface ObjectRepr  {

    public abstract Object getField(JMemberConstant fld);

    public abstract void putField(JMemberConstant fld, Object value);

    public abstract JMethod dispatchMethod(JMemberConstant cm);

    public abstract JMethod dispatchSuperMethod(JMemberConstant cm);

    public abstract JMethod dispatchNonVirtualMethod(JMemberConstant cm);

    public abstract JClass getJClass();

}
