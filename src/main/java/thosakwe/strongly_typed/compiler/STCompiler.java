package thosakwe.strongly_typed.compiler;

import thosakwe.strongly_typed.analysis.Scope;
import thosakwe.strongly_typed.grammar.StronglyTypedBaseVisitor;
import thosakwe.strongly_typed.grammar.StronglyTypedParser;
import thosakwe.strongly_typed.lang.errors.CompilerError;

import java.util.ArrayList;
import java.util.List;

public abstract class STCompiler<T> extends StronglyTypedBaseVisitor<T> {
    protected final List<CompilerError> errors = new ArrayList<>();
    protected final Scope symbolTable = new Scope();

    public abstract String compile(StronglyTypedParser.CompilationUnitContext ast) throws CompilerError;

    public List<CompilerError> getErrors() {
        return errors;
    }
}
