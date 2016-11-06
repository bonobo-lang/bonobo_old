package thosakwe.bonobo.compiler;

import thosakwe.bonobo.analysis.Scope;
import thosakwe.bonobo.grammar.BonoboBaseVisitor;
import thosakwe.bonobo.grammar.BonoboParser;

import java.util.ArrayList;
import java.util.List;

public abstract class STCompiler<T> extends BonoboBaseVisitor<T> {
    protected final List<CompilerError> errors = new ArrayList<>();
    protected final Scope symbolTable = new Scope();

    public abstract String compile(BonoboParser.CompilationUnitContext ast) throws CompilerError;

    public List<CompilerError> getErrors() {
        return errors;
    }
}
