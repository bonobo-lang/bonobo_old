package thosakwe.bonobo.analysis;

import thosakwe.bonobo.grammar.BonoboBaseVisitor;
import thosakwe.bonobo.compiler.SyntaxErrorListener;
import thosakwe.bonobo.compiler.CompilerError;

import java.util.ArrayList;
import java.util.List;

public class StaticAnalyzer extends BonoboBaseVisitor {
    private final List<CompilerError> errors = new ArrayList<>();
    private final List<CompilerError> warnings = new ArrayList<>();
    private final Scope symbolTable = new Scope();

    public StaticAnalyzer(SyntaxErrorListener listener, String sourceFile) {
        this.errors.addAll(listener.getErrors());
        this.symbolTable.sourceFile = sourceFile;
    }

    public List<CompilerError> getErrors() {
        return errors;
    }

    public List<CompilerError> getWarnings() {
        return warnings;
    }
}
