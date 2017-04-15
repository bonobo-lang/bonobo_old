package thosakwe.bonobo.language.objects;

import org.antlr.v4.runtime.ParserRuleContext;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import thosakwe.bonobo.analysis.ErrorChecker;
import thosakwe.bonobo.analysis.StaticAnalyzer;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.BonoboType;
import thosakwe.bonobo.language.types.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BonoboFunction extends BonoboObject {

    private final List<BonoboFunctionParameter> parameters = new ArrayList<>();
    private String name = null;
    private final ParserRuleContext source;
    private BonoboType returnType = BonoboUnknownType.INSTANCE;

    public BonoboFunction(ParserRuleContext source) {
        this.source = source;
    }

    public List<BonoboFunctionParameter> getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BonoboType getReturnType() {
        return returnType;
    }

    public void setReturnType(BonoboType returnType) {
        this.returnType = returnType;
    }

    @Override
    public BonoboType getType() {
        return new BonoboAbstractClassImpl(computeSignature()) {
            @Override
            public BonoboType typeForInvoke(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
                // TODO: Map when called with lists
                if (arguments.size() > parameters.size()) {
                    throw new BonoboException(
                            String.format("Function %s called with too many arguments; expected %d.",
                                    computeSignature(), parameters.size()),
                            source);
                } else if (arguments.size() == parameters.size()) {
                    // Called with the right number of arguments, thank God!
                    return returnType;
                } else {
                    // Curry that bwoi
                    BonoboFunction curried = new BonoboFunction(source);
                    curried.setName(name);
                    curried.setReturnType(returnType);

                    // Add missing params
                    int diff = parameters.size() - arguments.size();

                    for (int i = 0; i < diff; i++) {
                        curried.parameters.add(parameters.get(parameters.size() - diff));
                    }

                    return curried.getType();
                }
            }
        };
    }

    private String computeSignature() {
        StringBuilder buf = new StringBuilder();

        buf.append("fn (");

        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0)
                buf.append(", ");
            BonoboFunctionParameter parameter = parameters.get(i);
            buf.append(String.format("%s:%s", parameter.getName(), parameter.getType().getName()));
        }

        buf.append(")");
        buf.append(String.format(" => %s", returnType.getName()));

        return buf.toString();
    }

    @Override
    public ParserRuleContext getSource() {
        return source;
    }

    @Override
    public Object compile(ClassWriter clazz, StaticAnalyzer analyzer, ErrorChecker errorChecker, BonoboLibrary library, List<Exception> errors, boolean debug) {
        MethodVisitor meth = clazz.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, getName(), computeFunctionSignature(), null, null);
        // TODO: Compiler should not let main have args

        if (getName().equals("main") && getParameters().size() != 0) {
            errors.add(new IllegalArgumentException("`main` function must take 0 parameters."));
            return null;
        }

        // TODO: Visit block
        BonoboParser.TopLevelFuncDefContext ctx = (BonoboParser.TopLevelFuncDefContext) getSource();
        compileFunctionBody(ctx.funcBody(), meth, clazz, analyzer, errorChecker, library, errors, debug);

        // TODO: Return type

        // Max one stack elem, and one local variable
        meth.visitMaxs(ClassWriter.COMPUTE_MAXS, 1);
        meth.visitEnd();
        return null;
    }

    private void compileFunctionBody(BonoboParser.FuncBodyContext ctx, MethodVisitor meth, ClassWriter clazz, StaticAnalyzer analyzer, ErrorChecker errorChecker, BonoboLibrary library, List<Exception> errors, boolean debug) {
        if (ctx instanceof BonoboParser.BlockBodyContext) {
            for (BonoboParser.StmtContext stmt : ((BonoboParser.BlockBodyContext) ctx).block().stmt())
                compileStatement(stmt, meth, clazz, analyzer, errorChecker, library, errors, debug);
        } else if (ctx instanceof BonoboParser.StmtBodyContext) {
            compileStatement(((BonoboParser.StmtBodyContext) ctx).stmt(), meth, analyzer, errorChecker, clazz, library, errors, debug, true);
        }
    }

    private void compileStatement(BonoboParser.StmtContext stmt, MethodVisitor meth, ClassWriter clazz, StaticAnalyzer analyzer, ErrorChecker errorChecker, BonoboLibrary library, List<Exception> errors, boolean debug) {
        compileStatement(stmt, meth, analyzer, errorChecker, clazz, library, errors, debug, false);
    }

    private void compileStatement(BonoboParser.StmtContext stmt, MethodVisitor meth, StaticAnalyzer analyzer, ErrorChecker errorChecker, ClassWriter clazz, BonoboLibrary library, List<Exception> errors, boolean debug, boolean forceReturn) {
        try {
            if (stmt instanceof BonoboParser.HelperFuncStmtContext) {
                BonoboParser.HelperFuncStmtContext ctx = (BonoboParser.HelperFuncStmtContext) stmt;
                String helperName = ctx.helper.getText();
                boolean shouldPrint = !helperName.startsWith("debug") || debug;

                if (shouldPrint) {
                    if (!helperName.endsWith("f")) {
                        // Regular sysout
                        meth.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                        BonoboObject obj = analyzer.analyzeExpression(ctx.expr(0));
                        meth.visitLdcInsn(obj.compile(clazz, analyzer, errorChecker, library, errors, debug));
                        meth.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
                    } else {
                        // TODO: Printf
                    }
                }
            }
        } catch (Exception exc) {
            errors.add(exc);
        }
    }

    private String computeFunctionSignature() {
        if (getName() != null && getName().equals("main"))
            return "([Ljava/lang/String;)V";

        StringBuilder buf = new StringBuilder();

        buf.append("(");

        for (BonoboFunctionParameter parameter : getParameters()) {
            buf.append(computeJvmType(parameter.getType()));
        }

        buf.append(")");

        buf.append(computeJvmType(getReturnType()));

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
