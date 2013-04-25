package extractjre;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;


public class EmitInterfaces {

    String root = "rt2/"; // Root of the unzipped rt.jar (only classes that we want to include)
    String outRoot = "out/";

    public static void main(String[] args) throws IOException {
        EmitInterfaces e = new EmitInterfaces();
        e.emitJavaSource(new PrintStream(new FileOutputStream("FileSystemContent.java")), false);
    }

    public void emitJavaSource(PrintStream out, boolean strip) throws IOException {
        out.println("package gwtjava.io.fs;");
        out.println("import java.util.HashMap;");
        out.println("class FileSystemContent {");
        out.println("  public static HashMap<String, String> files = new HashMap<String, String>();");
        out.println("  static {");
        emitJavaSource(new File(root), out, strip);
        out.println("  }");
        out.println("}");
    }

    public void emitJavaSource(File dir, PrintStream out, boolean strip) throws IOException {
        if (dir.isFile()) {
            try {
                String classPath = dir.getPath().substring(root.length());
                byte [] bytecode;
                if (strip) {
                    bytecode = emit(classPath);
                } else {
                    RandomAccessFile in= new RandomAccessFile(root + classPath, "r");
                    bytecode =  new byte[(int) in.length()];
                    in.readFully(bytecode);
                    in.close();
                }
                if (bytecode != null) {
                    out.println("    files.put(\"" + classPath + "\", \"" + hexEncode(bytecode) + "\");");
                }
            } catch (Throwable e){
                System.err.println(dir.getAbsolutePath());
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            for (File child : dir.listFiles()) {
                emitJavaSource(child, out, strip);
            }
        }
    }

    public void emitClassFiles(File dir) throws IOException {
        if (dir.isFile()) {
            try {
                String classPath = dir.getAbsolutePath().substring(root.length());
                byte [] bytecode = emit(classPath);
                if (bytecode != null) {
                    File outFile =new File(outRoot + classPath);
                    outFile.getParentFile().mkdirs();

                    DataOutputStream dout=new DataOutputStream(new FileOutputStream(outFile));
                    dout.write(bytecode);
                    dout.flush();
                    dout.close();
                }
            } catch (Throwable e){
                System.err.println(dir.getAbsolutePath());
                e.printStackTrace();
                System.exit(-1);
            }
        } else {
            for (File child : dir.listFiles()) {
                emitClassFiles(child);
            }
        }
    }

    public static String hexEncode(byte [] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    public byte [] emit(String classPath) throws IOException {
        InputStream in= new FileInputStream(root + classPath);

        ClassReader cr=new ClassReader(in);
        ClassNode classNode=new ClassNode();
        cr.accept(classNode, 0);
        in.close();

        if ((classNode.access & Opcodes.ACC_PUBLIC) == 0 && classPath.contains("$")) {
            return null;
        }

        ArrayList<MethodNode> methods = new ArrayList<MethodNode>();
        for (MethodNode mn : (List<MethodNode>)classNode.methods) {
            if ((mn.access & Opcodes.ACC_PUBLIC) != 0) {
                if (!mn.name.equals("<init>")) {
                    if ((mn.access & Opcodes.ACC_ABSTRACT) == 0) {
                        mn.access |= Opcodes.ACC_NATIVE;
                    }
                }
                mn.instructions.clear();
                methods.add(mn);
            }
        }
        classNode.methods = methods;

        ArrayList<FieldNode> fields = new ArrayList<FieldNode>();
        for (FieldNode fn : (List<FieldNode>)classNode.fields) {
            if ((fn.access & Opcodes.ACC_PUBLIC) != 0) {
                fn.access &= ~Opcodes.ACC_FINAL;
                fields.add(fn);
            }
        }
        classNode.fields = fields;

        ArrayList<InnerClassNode> innerClasses = new ArrayList<InnerClassNode>();
        for (InnerClassNode icn : (List<InnerClassNode>)classNode.innerClasses) {
            if ((icn.access & Opcodes.ACC_PUBLIC) != 0) {
                innerClasses.add(icn);
            }
        }
        classNode.innerClasses = innerClasses;

        ClassWriter cw=new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        classNode.accept(cw);

        return cw.toByteArray();
    }
}
