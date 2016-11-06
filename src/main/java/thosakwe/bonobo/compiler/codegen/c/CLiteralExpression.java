package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.compiler.CodeBuilder;

public class CLiteralExpression extends CExpression {
    private final Integer size;
    private final String value;

    public CLiteralExpression(Integer size, String value) {
        this.size = size;
        this.value = value;
    }

    @Override
    public String compileToC(CodeBuilder builder) {
        return value;
    }

    @Override
    public Integer getSize() {
        return size;
    }
}
