package thosakwe.strongly_typed.compiler.codegen.c;

import thosakwe.strongly_typed.compiler.CodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class CBlock implements CAstNode {
    private final List<CStatement> statements = new ArrayList<>();

    @Override
    public void apply(CodeBuilder builder) {
        builder.println("{");
        builder.indent();
        statements.forEach(stmt -> stmt.apply(builder));
        builder.outdent();
        builder.println("}");
    }

    public List<CStatement> getStatements() {
        return statements;
    }
}
