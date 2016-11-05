package thosakwe.strongly_typed.compiler.codegen.c;

import thosakwe.strongly_typed.compiler.CodeBuilder;

public class CIntegerExpression extends CExpression {
    private final Integer value;

    public CIntegerExpression(Integer value) {
        super();
        this.value = value;
    }

    @Override
    public String compileToC(CodeBuilder builder) {
        return value.toString();
    }
}
