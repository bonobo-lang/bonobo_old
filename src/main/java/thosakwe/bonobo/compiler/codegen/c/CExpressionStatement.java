package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.analysis.Scope;
import thosakwe.bonobo.compiler.CodeBuilder;

public class CExpressionStatement extends CStatement {
    private final CExpression expression;

    public CExpressionStatement(CExpression expression) {
        this.expression = expression;
    }

    @Override
    public void apply(CodeBuilder builder, Scope symbolTable) {
        builder.println(String.format("%s;", expression.compileToC(builder)));
    }
}
