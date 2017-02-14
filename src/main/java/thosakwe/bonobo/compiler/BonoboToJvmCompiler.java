package thosakwe.bonobo.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.BonoboType;
import thosakwe.bonobo.language.objects.BonoboFunction;
import thosakwe.bonobo.language.objects.BonoboFunctionParameter;
import thosakwe.bonobo.language.types.BonoboByteType;
import thosakwe.bonobo.language.types.BonoboDoubleType;
import thosakwe.bonobo.language.types.BonoboIntegerType;
import thosakwe.bonobo.language.types.BonoboListType;

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
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
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
        MethodVisitor meth = clazz.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, function.getName(), computeFunctionSignature(function), null, null);
        // TODO: Compiler should not let main have args
        // TODO: Visit block
        // TODO: Return type

        // Max one stack elem, and one local variable
        meth.visitMaxs(function.getParameters().size(), 1);
        meth.visitEnd();
    }

    private String computeFunctionSignature(BonoboFunction function) {
        if (function.getName() != null && function.getName().equals("main"))
            return "([Ljava/lang/String;)V";

        StringBuilder buf = new StringBuilder();

        buf.append("(");

        for (BonoboFunctionParameter parameter : function.getParameters()) {
            buf.append(computeJvmType(parameter.getType()));
        }

        buf.append(")");

        buf.append(computeJvmType(function.getReturnType()));

        return buf.toString();
    }

    private String computeJvmType(BonoboType type) {
        if (type == BonoboByteType.INSTANCE)
            return "B";
        if (type == BonoboDoubleType.INSTANCE)
            return "D";
        if (type == BonoboIntegerType.INSTANCE)
            return "I";

        if (type instanceof BonoboListType)
            return String.format("[%s", computeJvmType(((BonoboListType) type).getReferenceType()));

        return "Ljava/lang/Object;";
    }
}
