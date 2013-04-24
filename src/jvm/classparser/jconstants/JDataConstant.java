package jvm.classparser.jconstants;



public abstract class JDataConstant implements JConstant {

    /** Returns the representation of the object: value type, string or class.
     * @throws JClassNotLoadedException */
    public abstract Object getRepr();
}
