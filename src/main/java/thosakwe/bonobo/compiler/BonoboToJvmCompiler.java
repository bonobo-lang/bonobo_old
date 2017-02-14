package thosakwe.bonobo.compiler;

import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.objects.BonoboFunction;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created on 2/14/2017.
 */
public class BonoboToJvmCompiler extends BonoboCompiler {
    public BonoboToJvmCompiler(boolean debug) {
        super(debug);
    }

    @Override
    public void compile(BonoboLibrary library, OutputStream outputStream) throws IOException {
        ClassWriter clazz = new ClassWriter(0);
        clazz.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, "Main", null, "java/lang/Object", null);

        // Implicit constructor
        MethodVisitor constructor = clazz.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        // Push `this`
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        // Invoke `super`
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitInsn(Opcodes.RETURN);
        // Max one stack elem, and one local variable
        constructor.visitMaxs(1, 1);
        constructor.visitEnd();


        for (String name : library.getExports().keySet()) {
            BonoboObject value = library.getExports().get(name);

            if (value instanceof BonoboFunction) {
                compileTopLevelFunction((BonoboFunction) value, library, clazz);
            }
        }

        outputStream.write(clazz.toByteArray());
    }

    private void compileTopLevelFunction(BonoboFunction function, BonoboLibrary library, ClassWriter clazz) {
        MethodVisitor meth = clazz.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, function.getName(), "()V", null, null);

        // TODO: Params, and custom params for main
        // TODO: Compiler should not let main have args
        // TODO: Visit block
        // TODO: Return type

        // Max one stack elem, and one local variable
        meth.visitMaxs(1, 1);
        meth.visitEnd();
    }
}
