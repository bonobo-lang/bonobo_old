package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.compiler.CodeBuilder;

public class CReferenceExpression extends CExpression {
    private final CExpression expression;

    public CReferenceExpression(CExpression expression) {
        this.expression = expression;
    }

    @Override
    public String compileToC(CodeBuilder builder) {
        return String.format("&(%s)", expression.compileToC(builder));
    }

    @Override
    public Integer getSize() {
        return 32;
    }
}
