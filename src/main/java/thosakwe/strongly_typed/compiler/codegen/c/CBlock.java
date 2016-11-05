package thosakwe.strongly_typed.compiler.codegen.c;

import thosakwe.strongly_typed.analysis.Scope;
import thosakwe.strongly_typed.compiler.CodeBuilder;
import thosakwe.strongly_typed.lang.errors.CompilerError;

import java.util.ArrayList;
import java.util.List;

public class CBlock implements CAstNode {
    private final List<CStatement> statements = new ArrayList<>();

    @Override
    public void apply(CodeBuilder builder, Scope symbolTable) throws CompilerError {
        builder.indent();

        for (CStatement stmt: statements) {
            stmt.apply(builder, symbolTable);
        }

        builder.outdent();
    }

    public List<CStatement> getStatements() {
        return statements;
    }
}
