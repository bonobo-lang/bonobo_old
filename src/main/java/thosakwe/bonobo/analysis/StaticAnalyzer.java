package thosakwe.bonobo.analysis;

import thosakwe.bonobo.grammar.BonoboBaseVisitor;
import thosakwe.bonobo.grammar.BonoboParser;

import java.util.ArrayList;
import java.util.List;

public class StaticAnalyzer extends BonoboBaseVisitor {
    private final List<CompilerError> errors = new ArrayList<>();
    private final List<CompilerError> warnings = new ArrayList<>();
    private Scope scope = new Scope();

    public StaticAnalyzer(SyntaxErrorListener listener, String sourceFile) {
        this.errors.addAll(listener.getErrors());
        // this.scope.sourceFile = sourceFile;
    }

    void pushScope() {
        scope = scope.fork();
    }

    void popScope() {
        scope = scope.join();
    }

    public List<CompilerError> getErrors() {
        return errors;
    }

    public List<CompilerError> getWarnings() {
        return warnings;
    }

    @Override
    public Object visitCompilationUnit(BonoboParser.CompilationUnitContext ctx) {
        return super.visitCompilationUnit(ctx);
    }
}
