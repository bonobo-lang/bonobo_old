package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.compiler.CodeBuilder;

public class CAssignmentExpression extends CExpression {
    private final CExpression left;
    private final CExpression right;
    private final String op;

    public CAssignmentExpression(CExpression left, CExpression right, String op) {
        super();
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    public String compileToC(CodeBuilder builder) {
        return String.format("%s %s %s", left.compileToC(builder), op, right.compileToC(builder));
    }

    @Override
    public Integer getSize() {
        return right.getSize();
    }
}
