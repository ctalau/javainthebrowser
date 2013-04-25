package jvm.execution;

import gwtjava.statics.SException;
import gwtjava.lang.System;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jvm.classparser.JClass;
import jvm.classparser.JType;
import jvm.classparser.JAttribute.CodeAttribute.ExceptionDescriptor;
import jvm.classparser.JClass.Status;
import jvm.classparser.JMember.JBytecodeMethod;
import jvm.classparser.JMember.JMethod;
import jvm.classparser.JMember.JNativeMethod;
import jvm.classparser.jconstants.JClassConstant;
import jvm.classparser.jconstants.JDataConstant;
import jvm.classparser.jconstants.JMemberConstant;
import jvm.classparser.jconstants.JStringConstant;
import jvm.execution.JClassLoader.JClassNotInitializedException;
import jvm.execution.objrepr.ArrayRepr;
import jvm.execution.objrepr.ObjectFactory;
import jvm.execution.objrepr.ObjectRepr;
import jvm.execution.objrepr.StaticMembers;
import jvm.util.DataInputStream;


/**
 *
 * Execution engine with lazy class loading and limited support for JSNI
 *
 * The class loading process is similar with lazy memory allocation in modern
 * operating systems. When the program wants to use a page, it generates a
 * SEGFAULT, the page is loaded and the program resumed.
 *
 * In our case, we generate a ClassNotLoadedException, and we resume executing
 * the program in the exception handler.
 *
 */
public class ExecutionEngine extends Stack {
    public static String BOOT_METHOD_DESCRIPTOR = "main([Ljava/lang/String;)V";

    private JClassLoader jcl = JClassLoader.getInstance();

    // Next instruction to be executed
    private int pc = -1;
    private JBytecodeMethod m = null;
    private JClass crtClass = null;

    private void execute() {
        int skip, index, addr, ppc = 0;
        Integer i;
        Long l;
        Float f;
        Double d;
        try {
            while (true) {
                try {
                    ppc = pc;
                    skip = 0;
                    byte opcode = (byte) nextByte();

                    switch (opcode) {
                    case OPCodes.OP_nop:
                        break;
                    case OPCodes.OP_aconst_null:
                        push(null);
                        break;
                    case OPCodes.OP_iconst_m1:
                    case OPCodes.OP_iconst_0:
                    case OPCodes.OP_iconst_1:
                    case OPCodes.OP_iconst_2:
                    case OPCodes.OP_iconst_3:
                    case OPCodes.OP_iconst_4:
                    case OPCodes.OP_iconst_5:
                        pushi(opcode - OPCodes.OP_iconst_m1 - 1);
                        break;
                    case OPCodes.OP_lconst_0:
                    case OPCodes.OP_lconst_1:
                        pushl((long) (opcode - OPCodes.OP_lconst_0));
                        break;
                    case OPCodes.OP_fconst_0:
                    case OPCodes.OP_fconst_1:
                    case OPCodes.OP_fconst_2:
                        pushf((float) (opcode - OPCodes.OP_fconst_0));
                        break;
                    case OPCodes.OP_dconst_0:
                    case OPCodes.OP_dconst_1:
                        pushd((double) (opcode - OPCodes.OP_dconst_0));
                        break;
                    case OPCodes.OP_bipush:
                        pushi((int) (byte) (int) nextByte());
                        break;
                    case OPCodes.OP_sipush:
                        pushi((int) (short) (int) nextShort());
                        break;

                    case OPCodes.OP_ldc_w:
                    case OPCodes.OP_ldc2_w:
                    case OPCodes.OP_ldc: {
                        index = nextByte();
                        if (opcode != OPCodes.OP_ldc)
                            index = (index << 8) + nextByte();
                        JDataConstant cst = crtClass.getDataConstant(index);

                        Object constant = cst.getRepr();
                        int size = opcode == OPCodes.OP_ldc2_w ? 1 : 0;
                        push(constant, size);
                        break;
                    }

                    // load / store
                    case OPCodes.OP_lload:
                    case OPCodes.OP_dload:
                        skip = 1;
                    case OPCodes.OP_iload:
                    case OPCodes.OP_fload:
                    case OPCodes.OP_aload:
                        push(loadLocal(nextByte()), skip);
                        break;

                    case OPCodes.OP_lload_0:
                    case OPCodes.OP_lload_1:
                    case OPCodes.OP_lload_2:
                    case OPCodes.OP_lload_3:
                    case OPCodes.OP_dload_0:
                    case OPCodes.OP_dload_1:
                    case OPCodes.OP_dload_2:
                    case OPCodes.OP_dload_3:
                        skip = 1;
                    case OPCodes.OP_iload_0:
                    case OPCodes.OP_iload_1:
                    case OPCodes.OP_iload_2:
                    case OPCodes.OP_iload_3:
                    case OPCodes.OP_fload_0:
                    case OPCodes.OP_fload_1:
                    case OPCodes.OP_fload_2:
                    case OPCodes.OP_fload_3:
                    case OPCodes.OP_aload_0:
                    case OPCodes.OP_aload_1:
                    case OPCodes.OP_aload_2:
                    case OPCodes.OP_aload_3:
                        index = (opcode - 2) % 4;
                        push(loadLocal(index), skip);
                        break;

                    case OPCodes.OP_laload:
                    case OPCodes.OP_daload:
                        skip = 1;
                    case OPCodes.OP_iaload:
                    case OPCodes.OP_faload:
                    case OPCodes.OP_aaload:
                    case OPCodes.OP_caload:
                    case OPCodes.OP_saload:
                    case OPCodes.OP_baload: {
                        int idx = popi();
                        ArrayRepr arrayRef = (ArrayRepr) pop();
                        Object val = arrayRef.get(idx);
                        // All the values should be loaded as integer
                        if (opcode == OPCodes.OP_caload) {
                            val = (int) ((Character) val).charValue();
                        } else if (opcode == OPCodes.OP_saload) {
                            val = ((Short) val).intValue();
                        } else if (opcode == OPCodes.OP_baload) {
                            val = ((Byte) val).intValue();
                        }

                        push(val, skip);
                        break;
                    }

                    case OPCodes.OP_istore:
                    case OPCodes.OP_lstore:
                    case OPCodes.OP_fstore:
                    case OPCodes.OP_dstore:
                    case OPCodes.OP_astore: {
                        index = nextByte();
                        storeLocal(index, pop());
                        break;
                    }

                    case OPCodes.OP_lstore_0:
                    case OPCodes.OP_lstore_1:
                    case OPCodes.OP_lstore_2:
                    case OPCodes.OP_lstore_3:
                    case OPCodes.OP_dstore_0:
                    case OPCodes.OP_dstore_1:
                    case OPCodes.OP_dstore_2:
                    case OPCodes.OP_dstore_3:
                        skip = 1;
                    case OPCodes.OP_istore_0:
                    case OPCodes.OP_istore_1:
                    case OPCodes.OP_istore_2:
                    case OPCodes.OP_istore_3:
                    case OPCodes.OP_fstore_0:
                    case OPCodes.OP_fstore_1:
                    case OPCodes.OP_fstore_2:
                    case OPCodes.OP_fstore_3:
                    case OPCodes.OP_astore_0:
                    case OPCodes.OP_astore_1:
                    case OPCodes.OP_astore_2:
                    case OPCodes.OP_astore_3: {
                        index = (opcode - 3) % 4;
                        storeLocal(index, pop(skip));
                        break;
                    }

                    case OPCodes.OP_lastore:
                    case OPCodes.OP_dastore:
                        skip = 1;
                    case OPCodes.OP_iastore:
                    case OPCodes.OP_fastore:
                    case OPCodes.OP_aastore: {
                        Object val = pop(skip);
                        int idx = popi();
                        ArrayRepr arrayRef = (ArrayRepr) pop();
                        arrayRef.set(idx, val);
                        break;
                    }

                    case OPCodes.OP_castore:
                    case OPCodes.OP_sastore:
                    case OPCodes.OP_bastore: {

                        int val = (Integer) pop(skip);
                        int idx = popi();
                        ArrayRepr arrayRef = (ArrayRepr) pop();

                        // All the values should be loaded as integer
                        Object elem;
                        if (opcode == OPCodes.OP_castore) {
                            elem = (char) (val & 0xFFFF);
                        } else if (opcode == OPCodes.OP_sastore) {
                            elem = (short) (val & 0xFFFF);
                        } else if (opcode == OPCodes.OP_bastore) {
                            elem = (byte) (val & 0xFF);
                        } else {
                            throw new AssertionError();
                        }

                        arrayRef.set(idx, elem);
                        break;
                    }

                    // Stack management
                    case OPCodes.OP_pop2:
                        pop();
                    case OPCodes.OP_pop:
                        pop();
                        break;
                    case OPCodes.OP_dup: {
                        Object v = pop();
                        push(v).push(v);
                        break;
                    }
                    case OPCodes.OP_dup_x1: {
                        Object v1 = pop(), v2 = pop();
                        push(v1).push(v2).push(v1);
                        break;
                    }
                    case OPCodes.OP_dup_x2: {
                        Object v1 = pop(), v2 = pop(), v3 = pop();
                        push(v1).push(v3).push(v2).push(v1);
                        break;
                    }
                    case OPCodes.OP_dup2: {
                        Object v1 = pop(), v2 = pop();
                        push(v2).push(v1).push(v2).push(v1);
                        break;
                    }

                    case OPCodes.OP_dup2_x1: {
                        Object v1 = pop(), v2 = pop(), v3 = pop();
                        push(v2).push(v1).push(v3).push(v2).push(v1);
                        break;
                    }
                    case OPCodes.OP_dup2_x2: {
                        Object v1 = pop(), v2 = pop(), v3 = pop(), v4 = pop();
                        push(v2).push(v1).push(v4).push(v3).push(v2).push(v1);
                        break;
                    }
                    case OPCodes.OP_swap: {
                        Object v1 = pop(), v2 = pop();
                        push(v2).push(v1);
                        break;
                    }

                    // Arithmetic
                    case OPCodes.OP_iadd:
                        pushi(popi() + popi());
                        break;
                    case OPCodes.OP_ladd:
                        pushl(popl() + popl());
                        break;
                    case OPCodes.OP_fadd:
                        pushf(popf() + popf());
                        break;
                    case OPCodes.OP_dadd:
                        pushd(popd() + popd());
                        break;

                    case OPCodes.OP_isub:
                        i = popi();
                        pushi(popi() - i);
                        break;
                    case OPCodes.OP_lsub:
                        l = popl();
                        pushl(popl() - l);
                        break;
                    case OPCodes.OP_fsub:
                        f = popf();
                        pushf(popf() - f);
                        break;
                    case OPCodes.OP_dsub:
                        d = popd();
                        pushd(popd() - d);
                        break;

                    case OPCodes.OP_imul:
                        pushi(popi() * popi());
                        break;
                    case OPCodes.OP_lmul:
                        pushl(popl() * popl());
                        break;
                    case OPCodes.OP_fmul:
                        pushf(popf() * popf());
                        break;
                    case OPCodes.OP_dmul:
                        pushd(popd() * popd());
                        break;

                    // XXX Division by zero exception
                    case OPCodes.OP_idiv:
                        i = popi();
                        pushi(popi() / i);
                        break;

                    case OPCodes.OP_ldiv:
                        l = popl();
                        pushl(popl() / l);
                        break;
                    case OPCodes.OP_fdiv:
                        f = popf();
                        pushf(popf() / f);
                        break;
                    case OPCodes.OP_ddiv:
                        d = popd();
                        pushd(popd() / d);
                        break;

                    case OPCodes.OP_irem:
                        i = popi();
                        pushi(popi() % i);
                        break;
                    case OPCodes.OP_lrem:
                        l = popl();
                        pushl(popl() % l);
                        break;
                    case OPCodes.OP_frem:
                        f = popf();
                        pushf(popf() % f);
                        break;
                    case OPCodes.OP_drem:
                        d = popd();
                        pushd(popd() % d);
                        break;

                    case OPCodes.OP_ineg:
                        pushi(-popi());
                        break;
                    case OPCodes.OP_lneg:
                        pushl(-popl());
                        break;
                    case OPCodes.OP_fneg:
                        pushf(-popf());
                        break;
                    case OPCodes.OP_dneg:
                        pushd(-popd());
                        break;

                    case OPCodes.OP_ishl:
                        i = popi();
                        pushi(popi() << i);
                        break;
                    case OPCodes.OP_lshl:
                        i = popi();
                        pushl(popl() << i);
                        break;
                    case OPCodes.OP_ishr:
                        i = popi();
                        pushi(popi() >> i);
                        break;
                    case OPCodes.OP_lshr:
                        i = popi();
                        pushl(popl() >> i);
                        break;

                    case OPCodes.OP_iushr:
                        i = popi();
                        pushi(popi() >>> i);
                        break;
                    case OPCodes.OP_lushr:
                        i = popi();
                        pushl(popl() >>> i);
                        break;

                    case OPCodes.OP_iand:
                        pushi(popi() & popi());
                        break;
                    case OPCodes.OP_land:
                        pushl(popl() & popl());
                        break;

                    case OPCodes.OP_ior:
                        pushi(popi() | popi());
                        break;
                    case OPCodes.OP_lor:
                        pushl(popl() | popl());
                        break;

                    case OPCodes.OP_ixor:
                        pushi(popi() ^ popi());
                        break;
                    case OPCodes.OP_lxor:
                        pushl(popl() ^ popl());
                        break;

                    case OPCodes.OP_iinc:
                        index = nextByte();
                        storeLocal(index, (Integer) loadLocal(index)
                                + (int) (byte) nextByte());
                        break;

                    case OPCodes.OP_i2l:
                        pushl(popi().longValue());
                        break;
                    case OPCodes.OP_i2f:
                        pushf(popi().floatValue());
                        break;
                    case OPCodes.OP_i2d:
                        pushd(popi().doubleValue());
                        break;
                    case OPCodes.OP_l2i:
                        pushi(popl().intValue());
                        break;
                    case OPCodes.OP_l2f:
                        pushf(popl().floatValue());
                        break;
                    case OPCodes.OP_l2d:
                        pushd(popl().doubleValue());
                        break;
                    case OPCodes.OP_f2i:
                        pushi(popf().intValue());
                        break;
                    case OPCodes.OP_f2l:
                        pushl(popf().longValue());
                        break;
                    case OPCodes.OP_f2d:
                        pushd(popf().doubleValue());
                        break;
                    case OPCodes.OP_d2i:
                        pushi(popd().intValue());
                        break;
                    case OPCodes.OP_d2l:
                        pushl(popd().longValue());
                        break;
                    case OPCodes.OP_d2f:
                        pushf(popd().floatValue());
                        break;
                    case OPCodes.OP_i2b:
                        pushi((int) (byte) (int) popi());
                        break;
                    case OPCodes.OP_i2c:
                        pushi(popi() & 0xFFFF);
                        break;
                    case OPCodes.OP_i2s:
                        pushi((int) (short) (int) popi());
                        break;

                    // Comparison
                    case OPCodes.OP_lcmp:
                        l = popl();
                        pushi(popl().compareTo(l));
                        break;
                    case OPCodes.OP_fcmpl:
                    case OPCodes.OP_fcmpg:
                        f = popf();
                        pushi(popf().compareTo(f));
                        break;
                    case OPCodes.OP_dcmpl:
                    case OPCodes.OP_dcmpg:
                        d = popd();
                        pushi(popd().compareTo(d));
                        break;

                    // Control flow
                    case OPCodes.OP_ifeq:
                        addr = nextShort();
                        if (popi() == 0)
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_ifne:
                        addr = nextShort();
                        if (popi() != 0)
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_iflt:
                        addr = nextShort();
                        if (popi() < 0)
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_ifge:
                        addr = nextShort();
                        if (popi() >= 0)
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_ifgt:
                        addr = nextShort();
                        if (popi() > 0)
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_ifle:
                        addr = nextShort();
                        if (popi() <= 0)
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_if_icmpeq:
                        addr = nextShort();
                        if (popi() == popi())
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_if_icmpne:
                        addr = nextShort();
                        if (popi() != popi())
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_if_icmplt:
                        addr = nextShort();
                        if (popi() > popi())
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_if_icmpge:
                        addr = nextShort();
                        if (popi() <= popi())
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_if_icmpgt:
                        addr = nextShort();
                        if (popi() < popi())
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_if_icmple:
                        addr = nextShort();
                        if (popi() >= popi())
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_if_acmpeq:
                        addr = nextShort();
                        if (pop() == pop())
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_if_acmpne:
                        addr = nextShort();
                        if (pop() != pop())
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_goto:
                        pc = ppc + nextShort();
                        break;

                    case OPCodes.OP_ifnull:
                        addr = nextShort();
                        if (pop() == null)
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_ifnonnull:
                        addr = nextShort();
                        if (pop() != null)
                            pc = ppc + addr;
                        break;
                    case OPCodes.OP_goto_w:
                        pc = ppc + nextInt();
                        break;

                    // subroutine call
                    case OPCodes.OP_jsr:
                        push(pc + 2);
                        pc = ppc + nextShort();
                        break;
                    case OPCodes.OP_jsr_w:
                        push(pc + 4);
                        pc = ppc + nextInt();
                        break;
                    case OPCodes.OP_ret:
                        pc = (Integer) loadLocal(nextByte());
                        break;

                    // switch
                    case OPCodes.OP_tableswitch: {
                        // skip padding and read the three integers
                        pc = ((pc + 3) >> 2) << 2;
                        int def = nextInt(), low = nextInt(), high = nextInt();
                        int tpc = pc;
                        index = popi();

                        // default branch
                        if (index < low || index > high)
                            pc = def;

                        // extract the address from the table and jump
                        pc = tpc + index - low;
                        pc = ppc + nextByte();
                        break;
                    }

                    case OPCodes.OP_lookupswitch: {
                        pc = ((pc + 3) >> 2) << 2;
                        int def = ppc + nextInt(), npairs = nextInt();
                        int key = popi(), found = 0;

                        for (i = 0; i < npairs; i++) {
                            if (key == nextInt()) {
                                pc = ppc + nextInt();
                                found = 1;
                                break;
                            }
                            pc += 4; // skip the offset offset
                        }

                        if (found == 0)
                            pc = def;
                        break;
                    }

                    case OPCodes.OP_anewarray: {
                        JClassConstant cst = crtClass
                                .getClassConstant(nextShort());
                        JClass jc = jcl.getClassByConstant(cst);
                        push(ObjectFactory.newArray(jc.getName(), popi()));
                        break;
                    }

                    case OPCodes.OP_newarray: {
                        int code = nextByte();
                        String type = JType.getArrayElementType(code);
                        push(ObjectFactory.newArray(type, popi()));
                        break;
                    }

                    case OPCodes.OP_multianewarray: {
                        String arrayClassName = crtClass
                                .getClassName(nextShort());

                        int[] dims = new int[nextByte()];
                        for (i = dims.length - 1; i >= 0; i--)
                            dims[i] = popi();
                        push(ObjectFactory.newMultiArray(arrayClassName, dims));
                        break;
                    }

                    case OPCodes.OP_arraylength:
                        pushi(((ArrayRepr) pop()).length());
                        break;

                    // new object
                    case OPCodes.OP_new: { // index of the classname
                        JClassConstant cst = crtClass
                                .getClassConstant(nextShort());
                        JClass jc = jcl.getClassByConstant(cst);
                        jc.ensureInitialized();
                        push(ObjectFactory.newObject(jc));
                        break;
                    }

                    // field access
                    case OPCodes.OP_getstatic: {
                        JMemberConstant fld = crtClass.getMember(nextShort());
                        Object val = StaticMembers.getStaticField(fld);
                        push(val, fld.getSize() - 1);
                        break;
                    }
                    case OPCodes.OP_putstatic: {
                        JMemberConstant fld = crtClass.getMember(nextShort());
                        Object val = peek(fld.getSize());
                        StaticMembers.putStaticField(fld, val);
                        pop(fld.getSize() - 1);
                        break;
                    }

                    case OPCodes.OP_getfield: {
                        // XXX: the class of the field may be any superclass of
                        // the
                        // class of the object.
                        JMemberConstant fld = crtClass.getMember(nextShort());
                        Object value = popo().getField(fld);
                        push(value, fld.getSize() - 1);
                        break;
                    }
                    case OPCodes.OP_putfield: {
                        JMemberConstant fld = crtClass.getMember(nextShort());
                        Object val = pop(fld.getSize() - 1);
                        ObjectRepr obj = popo();
                        obj.putField(fld, val);
                        break;
                    }

                    /**
                     * Method invocation
                     */
                    case OPCodes.OP_invokestatic: {
                        JMemberConstant m = crtClass.getMember(nextShort());
                        JMethod cm = StaticMembers.dispatchMethod(m);
                        call(cm);
                        break;
                    }

                    case OPCodes.OP_invokevirtual:
                    case OPCodes.OP_invokeinterface: {
                        JMemberConstant m = crtClass.getMember(nextShort());
                        ObjectRepr obj = (ObjectRepr) peek(m.getArgsSize());
                        pc += (opcode == OPCodes.OP_invokeinterface) ? 2 : 0;

                        if (m.getFullName().equals(
                                "java/lang/Class/desiredAssertionStatus()Z")) {
                            popo();
                            pushi(0);
                        } else if (m
                                .getFullName()
                                .equals("java/lang/Class/getClassLoader()Ljava/lang/ClassLoader;")) {
                            popo();
                            push(null);
                        } else if (m.getFullName().equals(
                                "java/lang/Class/getName()Ljava/lang/String;")) {
                            String name = ((JClass) obj).getName();
                            ObjectRepr nameRepr = JStringConstant
                                    .createString(name);
                            push(nameRepr);
                        } else if (m.getFullName().equals("java/lang/Class/getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;")) {
                            pop();
                            pop();
                            push(null);
                        } else if (m.getFullName().equals("java/lang/Class/newInstance()Ljava/lang/Object;")){
                            JClass cls = (JClass) pop();
                            ObjectRepr inst = ObjectFactory.newObject(cls);
                            push(inst);
                            push(inst);
                            for (JMethod jm : cls.getMethods()) {
                                if (jm.getMemberName().equals("<init>()V")) {
                                    call(jm);
                                    break;
                                }
                            }

                        } else {
                            JMethod dm = obj.dispatchMethod(m);
                            call(dm);
                        }
                        break;
                    }

                    case OPCodes.OP_invokespecial: {
                        JMemberConstant m = crtClass.getMember(nextShort());
                        ObjectRepr obj = (ObjectRepr) peek(m.getArgsSize());
                        JMethod dm;
                        if (isSuperclass(m.getClassName(), crtClass) == 1
                                && !m.getClassName().equals(crtClass.getName())
                                && !m.getMemberName().startsWith("<")) {
                            dm = obj.dispatchSuperMethod(m);
                        } else {
                            dm = obj.dispatchNonVirtualMethod(m);
                        }
                        call(dm);
                        break;
                    }

                    case OPCodes.OP_lreturn:
                    case OPCodes.OP_dreturn:
                        skip = 1;
                    case OPCodes.OP_ireturn:
                    case OPCodes.OP_freturn:
                    case OPCodes.OP_areturn: {
                        Object retVal = pop(skip);
                        ret();
                        push(retVal, skip);
                        break;
                    }
                    case OPCodes.OP_return:
                        ret();
                        break;

                    case OPCodes.OP_instanceof: {
                        String refName = crtClass.getClassName(nextShort());
                        ObjectRepr obj = popo();
                        pushi(obj == null ? 0 : isSuperclass(refName,
                                obj.getJClass()));
                        break;
                    }

                    // Exception
                    case OPCodes.OP_athrow:
                        this.pc = ppc;
                        athrow();
                        break;

                    case OPCodes.OP_impdep1:
                        /* Exit the interpreter */
                        return;

                    case OPCodes.OP_wide: // XXX maybe it is not even used
                        throw new UnsupportedOperationException("WIDE");

                    case OPCodes.OP_checkcast: {
                        nextShort();
                        break;
                    }
                    case OPCodes.OP_monitorenter: {
                        popo();
                        break;
                    }
                    case OPCodes.OP_monitorexit: {
                        popo();
                        break;
                    }

                    case OPCodes.OP_breakpoint:
                    case OPCodes.OP_xxxunusedxxx1:
                    case OPCodes.OP_impdep2:
                    default:
                        throw new UnsupportedOperationException("Opcode: "
                                + opcode);
                    }
                } catch (JClassNotInitializedException e) {
                    this.pc = ppc;
                    e.jClass.setStatus(Status.INITIALIZED);
                    this.callInitClass(e.jClass);
                }
            }
        } catch (Throwable e) {
            pc = ppc;
            printVMStackTrace(e);
            SException.printStackTrace(e, System.out);
        }
    }

    /**
     * Bootstraps the JVM inside some harcoded method that invokes "main"
     *
     * @throws JClassNotLoadedException
     */
    public void bootstrap(final String bootClassName) {
        this.crtClass = JClass.createBootClass(bootClassName);
        this.m = JMethod.getBootMehtod();
        this.pc = 0;

        this.locals = 0;
        this.top = 1;

        this.jcl.loadSystemClasses();
        this.execute();
    }

    /** Initialize a class object. */
    private void callInitClass(JClass cls) {
        if (cls.getClinit() != null) {
            call(cls.getClinit());
        }
    }

    /**
     * Function call (after dispatch was done) Frame layout: obj <--- locals
     * arg1 ... argn .... (maxLocals entries) locals index pc method crtclass
     * <--- top
     */
    private void call(JMethod jm) {
        if (jm instanceof JNativeMethod) {
            callNative((JNativeMethod) jm);
            return;
        } else {
            JBytecodeMethod m = (JBytecodeMethod) jm;
            int newlocals = top - m.getArgsSize();
            top = newlocals + m.getCode().maxLocals;

            pushi(this.locals);
            pushi(this.pc);
            push(this.m);
            push(this.crtClass);
            this.locals = newlocals;

            this.pc = 0;
            this.m = m;
            this.crtClass = m.getJClass();
        }
    }

    /**
     * Function return
     */
    private boolean ret() {
        // System.out.println("Ret  " + m.getFullName());
        if (m.getMemberName().equals(JMethod.CLINIT_METHOD_NAME)) {
            crtClass.setStatus(Status.INITIALIZED); // XXX: or initialized by
                                                    // error if it is called in
                                                    // athrow
        }
        if (m.equals(JMethod.getBootMehtod())) {
            return false;
        }

        int oldpos = locals;
        int crtpos = top;

        top = locals + this.m.getCode().maxLocals + 4;

        this.crtClass = (JClass) pop();
        this.m = (JBytecodeMethod) pop();
        this.pc = popi();
        this.locals = popi();

        top = oldpos;
        Arrays.fill(stack, oldpos, crtpos, null);
        return true;
    }

    /**
     * Stack unwinding until an exception handler to match the thrown exception
     * is found.
     */
    private void athrow() {
        ObjectRepr exn = (ObjectRepr) pop();
//        try {
//            System.out.println(exn);
//        } catch (Throwable t) {
//            System.out.println("sof: " + this.m.getFullName());
//        }
        boolean handled = false;
        ArrayList<String> stackTrace = new ArrayList<String>();

        while (true) {
            for (ExceptionDescriptor d : this.m.getCode().exns) {
                if (d.start_pc <= pc && pc <= d.end_pc) {
                    if (d.type == JClassConstant.ANY ||
                            isSuperclass(d.type.getName(), exn.getJClass()) == 1) {
                        pc = d.handler_pc;
                        handled = true;
                    }
                }
            }
            if (handled)
                break;
            stackTrace.add(getStackFrame());

            if (!this.ret()) {
                Object message = exn.getField(new JMemberConstant(
                        new JClassConstant("java/lang/Throwable"),
                        "detailMessage", "Ljava/lang/String;"));
                System.err.println(exn.getJClass().getName() + ": "
                        + JStringConstant.toString((ObjectRepr) message));
                for (String stackTraceElement : stackTrace) {
                    System.err.println(stackTraceElement);
                }
                return;
            }
        }
        push(exn);
    }

    private String getStackFrame() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t at ").append(
                crtClass == null ? "(null)" : crtClass.getName());
        sb.append(": ").append(m == null ? "(null)" : m.getMemberName());
        sb.append(" ").append(pc);
        return sb.toString();
    }

    private void printVMStackTrace(Throwable e) {
        System.err.println("Stack trace:");
        if (e != null) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        do {
            System.err.println(getStackFrame());
            if (this.top < 2) {
                break;
            }
        } while (ret());
    }

    /**
     * Determine if a class is the ancestor of the other.
     *
     * TODO: Can be spedup by some pre-computation.
     */
    private int isSuperclass(String namep, JClass child) {
        if (JType.isArray(child.getName())) {
            if (namep.equals("Cloneable")
                    || namep.equals("java/io/Serializable")
                    || namep.equals(JClassLoader.OBJECT_CLASS_NAME)) {
                return 1;
            }
            if (JType.isArray(namep)) {
                if (JType.isPrimitive(namep) && namep.equals(child))
                    // same primitive type
                    return 1;

                JClass childElemClass = jcl.getClassByName(JType.elemType(child
                        .getName()));
                if (isSuperclass(JType.elemType(namep), childElemClass) == 1)
                    return 1;
            }
        } else { // child is class or interface type
            if (namep.equals(child))
                return 1;

            if (child.getSuperClass() == null) // Object
                return 1;

            if (isSuperclass(namep, child.getSuperClass()) == 1)
                return 1;

            for (JClassConstant ic : child.getInterfaces()) {
                if (isSuperclass(namep, ic.getJClass()) == 1)
                    return 1;
            }
        }

        return 0;
    }

    /*
     * Read next value from code
     */
    private int nextShort() {
        int ret = DataInputStream.readShort(m.getCode().bytecode, pc);
        pc += 2;
        return ret;
    }

    private int nextInt() {
        int ret = (int) DataInputStream.readUInt(m.getCode().bytecode, pc);
        pc += 4;
        return ret;
    }

    private int nextByte() {
        return (int) ((int) (m.getCode().bytecode[pc++]) & 0xFF);
    }

    /*
     * Singleton instance
     */
    private static ExecutionEngine inst = new ExecutionEngine();

    public static ExecutionEngine getInstance() {
        return inst;
    }

    /**
     * Harcoded native methods - ugly hack
     */
    public void callNative(JNativeMethod m) {
        List<Object> args = new ArrayList<Object>();
        for (int size : m.getArgsSizes()) {
            args.add(pop(size-1));
        }
        Collections.reverse(args);

        // The actual call
        Object ret = null;
        try {
             ret = m.call(args.toArray());
        } catch (UnsupportedOperationException e) {
//            System.err.println("***" + m.getFullName());
            return;
        }

        int retSize = m.getRetSize();
        if (retSize > 0) {
            push(ret, retSize - 1);
        }
    }
}
