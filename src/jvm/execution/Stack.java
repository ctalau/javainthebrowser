package jvm.execution;

import jvm.execution.objrepr.ObjectRepr;

/**
 * JVM Stack: it stores every numeric value boxed, for every Category 2    value
 * it skips an entry. Otherwise, because of dup operations, we should keep track
 * of the types at runtime.
 *
 * Since Java does not allow to mix between primitive and Object values in an array
 * we don't have a better option.
 *
 * @author ctalau
 */
public class Stack {
    private static final int KB = 1024;
    private static final int MB = 1024 * KB;

    protected Object [] stack = new Object [1 * MB / 1000]; // XXX: for testing
    protected int top = 0;
    protected int locals = 0;

    /*
     *  Push methods
     */
    protected Stack push(Object o){
        stack[top++] = o;
        return this;
    }

    protected Stack push(Object o, int skip){
        if (top > stack.length - 100) {
            throw new StackOverflowError();
        }
        stack[top++] = o;
        top += skip;
        return this;
    }

    protected Stack pushi(Integer o){
        return push(o);
    }

    protected Stack pushl(Long o){
        return push(o, 1);
    }

    protected Stack pushf(Float o){
        return push(o);
    }

    protected Stack pushd(Double o){
        return push(o, 1);
    }

    /*
     * Pop methods
     */
    protected Object pop(){
        Object ret = stack[--top];
        stack[top] = null;    // throw the object to the garbage collector
        return ret;
    }

    // skip should be 0 and 1
    protected Object pop(int skip){
        Object ret = stack[top - 1 - skip];
        stack[top - 1] = null;
        stack[top - 1 - skip] = null;
        top = top - 1 - skip;
        return ret;
    }

    protected Integer popi(){
        return (Integer) pop();
    }

    protected Float popf(){
        return (Float) pop();
    }

    protected Long popl(){
        return (Long) pop(1);
    }

    protected Double popd(){
        return (Double) pop(1);
    }

    protected ObjectRepr popo(){
        return (ObjectRepr) pop();
    }

    /*
     * Local variables methods
     */
    protected void storeLocal(int off, Object o){
        stack[locals + off] = o;
    }

    protected Object loadLocal(int off){
        return stack[locals + off];
    }

    // look into the stack without changing it
    protected Object peek(int off){
        return stack[top - off];
    }

    @Override
    public String toString() {
        StringBuffer sbf = new StringBuffer();
        sbf.append("Stack:\n");
        sbf.append("top: ").append(top).append('\n');
        sbf.append("locals: ").append(locals).append('\n');

        for (int i = 0; i < top; ++i) {
            sbf.append(String.format("%02", i));
            sbf.append(": ");
            sbf.append(stack[i]);
            sbf.append('\n');
        }
        return sbf.toString();
    }
}


/**
 * Performance improvement:
 *     -> Try to use javascript value representation with 64-bit tagged values
 */
