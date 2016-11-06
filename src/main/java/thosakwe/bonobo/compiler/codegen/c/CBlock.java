package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.analysis.Scope;
import thosakwe.bonobo.compiler.CodeBuilder;
import thosakwe.bonobo.compiler.CompilerError;

import java.util.ArrayList;
import java.util.List;

public class CBlock implements CAstNode {
    private final List<CStatement> statements = new ArrayList<>();

    public void add(CStatement statement) {
        statements.add(statement);
    }

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
