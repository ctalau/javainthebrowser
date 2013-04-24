package jvm.execution;

public class OPCodes {
    public static final byte OP_nop = (byte) (0x00);
    public static final byte OP_aconst_null = (byte) (0x01);
    public static final byte OP_iconst_m1 = (byte) (0x02);
    public static final byte OP_iconst_0 = (byte) (0x03);
    public static final byte OP_iconst_1 = (byte) (0x04);
    public static final byte OP_iconst_2 = (byte) (0x05);
    public static final byte OP_iconst_3 = (byte) (0x06);
    public static final byte OP_iconst_4 = (byte) (0x07);
    public static final byte OP_iconst_5 = (byte) (0x08);
    public static final byte OP_lconst_0 = (byte) (0x09);
    public static final byte OP_lconst_1 = (byte) (0x0a);
    public static final byte OP_fconst_0 = (byte) (0x0b);
    public static final byte OP_fconst_1 = (byte) (0x0c);
    public static final byte OP_fconst_2 = (byte) (0x0d);
    public static final byte OP_dconst_0 = (byte) (0x0e);
    public static final byte OP_dconst_1 = (byte) (0x0f);
    public static final byte OP_bipush = (byte) (0x10);
    public static final byte OP_sipush = (byte) (0x11);
    public static final byte OP_ldc = (byte) (0x12);
    public static final byte OP_ldc_w = (byte) (0x13);
    public static final byte OP_ldc2_w = (byte) (0x14);
    public static final byte OP_iload = (byte) (0x15);
    public static final byte OP_lload = (byte) (0x16);
    public static final byte OP_fload = (byte) (0x17);
    public static final byte OP_dload = (byte) (0x18);
    public static final byte OP_aload = (byte) (0x19);
    public static final byte OP_iload_0 = (byte) (0x1a);
    public static final byte OP_iload_1 = (byte) (0x1b);
    public static final byte OP_iload_2 = (byte) (0x1c);
    public static final byte OP_iload_3 = (byte) (0x1d);
    public static final byte OP_lload_0 = (byte) (0x1e);
    public static final byte OP_lload_1 = (byte) (0x1f);
    public static final byte OP_lload_2 = (byte) (0x20);
    public static final byte OP_lload_3 = (byte) (0x21);
    public static final byte OP_fload_0 = (byte) (0x22);
    public static final byte OP_fload_1 = (byte) (0x23);
    public static final byte OP_fload_2 = (byte) (0x24);
    public static final byte OP_fload_3 = (byte) (0x25);
    public static final byte OP_dload_0 = (byte) (0x26);
    public static final byte OP_dload_1 = (byte) (0x27);
    public static final byte OP_dload_2 = (byte) (0x28);
    public static final byte OP_dload_3 = (byte) (0x29);
    public static final byte OP_aload_0 = (byte) (0x2a);
    public static final byte OP_aload_1 = (byte) (0x2b);
    public static final byte OP_aload_2 = (byte) (0x2c);
    public static final byte OP_aload_3 = (byte) (0x2d);
    public static final byte OP_iaload = (byte) (0x2e);
    public static final byte OP_laload = (byte) (0x2f);
    public static final byte OP_faload = (byte) (0x30);
    public static final byte OP_daload = (byte) (0x31);
    public static final byte OP_aaload = (byte) (0x32);
    public static final byte OP_baload = (byte) (0x33);
    public static final byte OP_caload = (byte) (0x34);
    public static final byte OP_saload = (byte) (0x35);
    public static final byte OP_istore = (byte) (0x36);
    public static final byte OP_lstore = (byte) (0x37);
    public static final byte OP_fstore = (byte) (0x38);
    public static final byte OP_dstore = (byte) (0x39);
    public static final byte OP_astore = (byte) (0x3a);
    public static final byte OP_istore_0 = (byte) (0x3b);
    public static final byte OP_istore_1 = (byte) (0x3c);
    public static final byte OP_istore_2 = (byte) (0x3d);
    public static final byte OP_istore_3 = (byte) (0x3e);
    public static final byte OP_lstore_0 = (byte) (0x3f);
    public static final byte OP_lstore_1 = (byte) (0x40);
    public static final byte OP_lstore_2 = (byte) (0x41);
    public static final byte OP_lstore_3 = (byte) (0x42);
    public static final byte OP_fstore_0 = (byte) (0x43);
    public static final byte OP_fstore_1 = (byte) (0x44);
    public static final byte OP_fstore_2 = (byte) (0x45);
    public static final byte OP_fstore_3 = (byte) (0x46);
    public static final byte OP_dstore_0 = (byte) (0x47);
    public static final byte OP_dstore_1 = (byte) (0x48);
    public static final byte OP_dstore_2 = (byte) (0x49);
    public static final byte OP_dstore_3 = (byte) (0x4a);
    public static final byte OP_astore_0 = (byte) (0x4b);
    public static final byte OP_astore_1 = (byte) (0x4c);
    public static final byte OP_astore_2 = (byte) (0x4d);
    public static final byte OP_astore_3 = (byte) (0x4e);
    public static final byte OP_iastore = (byte) (0x4f);
    public static final byte OP_lastore = (byte) (0x50);
    public static final byte OP_fastore = (byte) (0x51);
    public static final byte OP_dastore = (byte) (0x52);
    public static final byte OP_aastore = (byte) (0x53);
    public static final byte OP_bastore = (byte) (0x54);
    public static final byte OP_castore = (byte) (0x55);
    public static final byte OP_sastore = (byte) (0x56);
    public static final byte OP_pop = (byte) (0x57);
    public static final byte OP_pop2 = (byte) (0x58);
    public static final byte OP_dup = (byte) (0x59);
    public static final byte OP_dup_x1 = (byte) (0x5a);
    public static final byte OP_dup_x2 = (byte) (0x5b);
    public static final byte OP_dup2 = (byte) (0x5c);
    public static final byte OP_dup2_x1 = (byte) (0x5d);
    public static final byte OP_dup2_x2 = (byte) (0x5e);
    public static final byte OP_swap = (byte) (0x5f);
    public static final byte OP_iadd = (byte) (0x60);
    public static final byte OP_ladd = (byte) (0x61);
    public static final byte OP_fadd = (byte) (0x62);
    public static final byte OP_dadd = (byte) (0x63);
    public static final byte OP_isub = (byte) (0x64);
    public static final byte OP_lsub = (byte) (0x65);
    public static final byte OP_fsub = (byte) (0x66);
    public static final byte OP_dsub = (byte) (0x67);
    public static final byte OP_imul = (byte) (0x68);
    public static final byte OP_lmul = (byte) (0x69);
    public static final byte OP_fmul = (byte) (0x6a);
    public static final byte OP_dmul = (byte) (0x6b);
    public static final byte OP_idiv = (byte) (0x6c);
    public static final byte OP_ldiv = (byte) (0x6d);
    public static final byte OP_fdiv = (byte) (0x6e);
    public static final byte OP_ddiv = (byte) (0x6f);
    public static final byte OP_irem = (byte) (0x70);
    public static final byte OP_lrem = (byte) (0x71);
    public static final byte OP_frem = (byte) (0x72);
    public static final byte OP_drem = (byte) (0x73);
    public static final byte OP_ineg = (byte) (0x74);
    public static final byte OP_lneg = (byte) (0x75);
    public static final byte OP_fneg = (byte) (0x76);
    public static final byte OP_dneg = (byte) (0x77);
    public static final byte OP_ishl = (byte) (0x78);
    public static final byte OP_lshl = (byte) (0x79);
    public static final byte OP_ishr = (byte) (0x7a);
    public static final byte OP_lshr = (byte) (0x7b);
    public static final byte OP_iushr = (byte) (0x7c);
    public static final byte OP_lushr = (byte) (0x7d);
    public static final byte OP_iand = (byte) (0x7e);
    public static final byte OP_land = (byte) (0x7f);
    public static final byte OP_ior = (byte) (0x80);
    public static final byte OP_lor = (byte) (0x81);
    public static final byte OP_ixor = (byte) (0x82);
    public static final byte OP_lxor = (byte) (0x83);
    public static final byte OP_iinc = (byte) (0x84);
    public static final byte OP_i2l = (byte) (0x85);
    public static final byte OP_i2f = (byte) (0x86);
    public static final byte OP_i2d = (byte) (0x87);
    public static final byte OP_l2i = (byte) (0x88);
    public static final byte OP_l2f = (byte) (0x89);
    public static final byte OP_l2d = (byte) (0x8a);
    public static final byte OP_f2i = (byte) (0x8b);
    public static final byte OP_f2l = (byte) (0x8c);
    public static final byte OP_f2d = (byte) (0x8d);
    public static final byte OP_d2i = (byte) (0x8e);
    public static final byte OP_d2l = (byte) (0x8f);
    public static final byte OP_d2f = (byte) (0x90);
    public static final byte OP_i2b = (byte) (0x91);
    public static final byte OP_i2c = (byte) (0x92);
    public static final byte OP_i2s = (byte) (0x93);
    public static final byte OP_lcmp = (byte) (0x94);
    public static final byte OP_fcmpl = (byte) (0x95);
    public static final byte OP_fcmpg = (byte) (0x96);
    public static final byte OP_dcmpl = (byte) (0x97);
    public static final byte OP_dcmpg = (byte) (0x98);
    public static final byte OP_ifeq = (byte) (0x99);
    public static final byte OP_ifne = (byte) (0x9a);
    public static final byte OP_iflt = (byte) (0x9b);
    public static final byte OP_ifge = (byte) (0x9c);
    public static final byte OP_ifgt = (byte) (0x9d);
    public static final byte OP_ifle = (byte) (0x9e);
    public static final byte OP_if_icmpeq = (byte) (0x9f);
    public static final byte OP_if_icmpne = (byte) (0xa0);
    public static final byte OP_if_icmplt = (byte) (0xa1);
    public static final byte OP_if_icmpge = (byte) (0xa2);
    public static final byte OP_if_icmpgt = (byte) (0xa3);
    public static final byte OP_if_icmple = (byte) (0xa4);
    public static final byte OP_if_acmpeq = (byte) (0xa5);
    public static final byte OP_if_acmpne = (byte) (0xa6);
    public static final byte OP_goto = (byte) (0xa7);
    public static final byte OP_jsr = (byte) (0xa8);
    public static final byte OP_ret = (byte) (0xa9);
    public static final byte OP_tableswitch = (byte) (0xaa);
    public static final byte OP_lookupswitch = (byte) (0xab);
    public static final byte OP_ireturn = (byte) (0xac);
    public static final byte OP_lreturn = (byte) (0xad);
    public static final byte OP_freturn = (byte) (0xae);
    public static final byte OP_dreturn = (byte) (0xaf);
    public static final byte OP_areturn = (byte) (0xb0);
    public static final byte OP_return = (byte) (0xb1);
    public static final byte OP_getstatic = (byte) (0xb2);
    public static final byte OP_putstatic = (byte) (0xb3);
    public static final byte OP_getfield = (byte) (0xb4);
    public static final byte OP_putfield = (byte) (0xb5);
    public static final byte OP_invokevirtual = (byte) (0xb6);
    public static final byte OP_invokespecial = (byte) (0xb7);
    public static final byte OP_invokestatic = (byte) (0xb8);
    public static final byte OP_invokeinterface = (byte) (0xb9);
    public static final byte OP_xxxunusedxxx1 = (byte) (0xba);
    public static final byte OP_new = (byte) (0xbb);
    public static final byte OP_newarray = (byte) (0xbc);
    public static final byte OP_anewarray = (byte) (0xbd);
    public static final byte OP_arraylength = (byte) (0xbe);
    public static final byte OP_athrow = (byte) (0xbf);
    public static final byte OP_checkcast = (byte) (0xc0);
    public static final byte OP_instanceof = (byte) (0xc1);
    public static final byte OP_monitorenter = (byte) (0xc2);
    public static final byte OP_monitorexit = (byte) (0xc3);
    public static final byte OP_wide = (byte) (0xc4);
    public static final byte OP_multianewarray = (byte) (0xc5);
    public static final byte OP_ifnull = (byte) (0xc6);
    public static final byte OP_ifnonnull = (byte) (0xc7);
    public static final byte OP_goto_w = (byte) (0xc8);
    public static final byte OP_jsr_w = (byte) (0xc9);
    public static final byte OP_breakpoint = (byte) (0xca);
    public static final byte OP_impdep1 = (byte) (0xfe);
    public static final byte OP_impdep2 = (byte) (0xff);

    private static final String[] names = { "nop", "aconst_null", "iconst_m1",
            "iconst_0", "iconst_1", "iconst_2", "iconst_3", "iconst_4",
            "iconst_5", "lconst_0", "lconst_1", "fconst_0", "fconst_1",
            "fconst_2", "dconst_0", "dconst_1", "bipush", "sipush", "ldc",
            "ldc_w", "ldc2_w", "iload", "lload", "fload", "dload", "aload",
            "iload_0", "iload_1", "iload_2", "iload_3", "lload_0", "lload_1",
            "lload_2", "lload_3", "fload_0", "fload_1", "fload_2", "fload_3",
            "dload_0", "dload_1", "dload_2", "dload_3", "aload_0", "aload_1",
            "aload_2", "aload_3", "iaload", "laload", "faload", "daload",
            "aaload", "baload", "caload", "saload", "istore", "lstore",
            "fstore", "dstore", "astore", "istore_0", "istore_1", "istore_2",
            "istore_3", "lstore_0", "lstore_1", "lstore_2", "lstore_3",
            "fstore_0", "fstore_1", "fstore_2", "fstore_3", "dstore_0",
            "dstore_1", "dstore_2", "dstore_3", "astore_0", "astore_1",
            "astore_2", "astore_3", "iastore", "lastore", "fastore", "dastore",
            "aastore", "bastore", "castore", "sastore", "pop", "pop2", "dup",
            "dup_x1", "dup_x2", "dup2", "dup2_x1", "dup2_x2", "swap", "iadd",
            "ladd", "fadd", "dadd", "isub", "lsub", "fsub", "dsub", "imul",
            "lmul", "fmul", "dmul", "idiv", "ldiv", "fdiv", "ddiv", "irem",
            "lrem", "frem", "drem", "ineg", "lneg", "fneg", "dneg", "ishl", "lshl",
            "ishr", "lshr", "iushr", "lushr", "iand", "land", "ior", "lor",
            "ixor", "lxor", "iinc", "i2l", "i2f", "i2d", "l2i", "l2f", "l2d",
            "f2i", "f2l", "f2d", "d2i", "d2l", "d2f", "i2b", "i2c", "i2s",
            "lcmp", "fcmpl", "fcmpg", "dcmpl", "dcmpg", "ifeq", "ifne", "iflt",
            "ifge", "ifgt", "ifle", "if_icmpeq", "if_icmpne", "if_icmplt",
            "if_icmpge", "if_icmpgt", "if_icmple", "if_acmpeq", "if_acmpne",
            "goto", "jsr", "ret", "tableswitch", "lookupswitch", "ireturn",
            "lreturn", "freturn", "dreturn", "areturn", "return", "getstatic",
            "putstatic", "getfield", "putfield", "invokevirtual",
            "invokespecial", "invokestatic", "invokeinterface",
            "xxxunusedxxx1", "new", "newarray", "anewarray", "arraylength",
            "athrow", "checkcast", "instanceof", "monitorenter", "monitorexit",
            "wide", "multianewarray", "ifnull", "ifnonnull", "goto_w", "jsr_w",
            "breakpoint", "impdep1", "impdep2" };
    public static String getName(byte opcode){
        return names[opcode & 0xFF];
    }
}
