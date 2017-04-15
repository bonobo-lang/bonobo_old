package thosakwe.bonobo.language;

import org.antlr.v4.runtime.ParserRuleContext;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import thosakwe.bonobo.analysis.ErrorChecker;
import thosakwe.bonobo.analysis.StaticAnalyzer;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.objects.BonoboFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BonoboLibrary {
    private final Map<String, BonoboObject> exports = new HashMap<>();
    private final BonoboParser.CompilationUnitContext source;

    public BonoboLibrary(BonoboParser.CompilationUnitContext source) {
        this.source = source;
    }

    public Map<String, BonoboObject> getExports() {
        return exports;
    }

    public BonoboParser.CompilationUnitContext getSource() {
        return source;
    }

    public void addExport(String name, BonoboObject value, ParserRuleContext source) throws BonoboException {
        if (exports.containsKey(name))
            throw new BonoboException(String.format("Another value has already been exported as \"%s\".", name), source);
        exports.put(name, value);
    }

    public ClassWriter compile(StaticAnalyzer analyzer, ErrorChecker errorChecker, List<Exception> errors, boolean debug) {
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


        for (String name : getExports().keySet()) {
            BonoboObject value = getExports().get(name);

            if (value instanceof BonoboFunction) {
                value.compile(clazz, analyzer, errorChecker, this, errors, debug);
            }
        }

        return clazz;
    }
}
