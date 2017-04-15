package thosakwe.bonobo.compiler;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import thosakwe.bonobo.analysis.ErrorChecker;
import thosakwe.bonobo.analysis.StaticAnalyzer;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboLibrary;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 2/14/2017.
 */
public class BonoboToJvmCompiler extends BonoboCompiler {
    private final List<Exception> errors = new ArrayList<>();

    public BonoboToJvmCompiler(boolean debug) {
        super(debug);
    }

    @Override
    public void compile(BonoboLibrary library, StaticAnalyzer analyzer, ErrorChecker errorChecker, OutputStream outputStream) throws IOException {
        ClassWriter clazz = library.compile(analyzer, errorChecker, errors, isDebug());

        if (!errors.isEmpty()) {
            System.err.printf("Compilation failed with %d error(s):%n", errors.size());

            for (Exception exc : errors) {
                System.err.printf("  * %s%n", exc.getMessage());
            }

            throw new IllegalStateException(String.format("Compilation failed with %d error(s).", errors.size()));
        }

        outputStream.write(clazz.toByteArray());
    }


    private Object compileExpression(BonoboParser.ExprContext ctx, BonoboLibrary library, MethodVisitor meth, ClassWriter clazz) {
        if (ctx instanceof BonoboParser.StringLiteralExprContext) {
            String text = ctx.getText();
            text = text.replaceAll("((^')|(^\"))|(('$)|(\"$))", "");
            // TODO: Escapes
            return text;
        } else if (ctx instanceof BonoboParser.IntegerLiteralExprContext) {
            return Integer.parseInt(ctx.getText());
        } else if (ctx instanceof BonoboParser.DoubleLiteralExprContext) {
            return Double.parseDouble(ctx.getText());
        }

        errors.add(new UnsupportedOperationException(String.format("Cannot compile expressions of type %s yet.", ctx.getClass().getSimpleName())));
        return null;
    }


}
