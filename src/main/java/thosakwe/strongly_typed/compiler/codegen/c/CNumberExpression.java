package thosakwe.strongly_typed.compiler.codegen.c;

import thosakwe.strongly_typed.compiler.CodeBuilder;

public class CNumberExpression extends CExpression {
    private final Number value;

    public CNumberExpression(Number value) {
        super();
        this.value = value;
    }

    @Override
    public String compileToC(CodeBuilder builder) {
        if (value.doubleValue() == value.intValue())
            return String.format("%d", value.intValue());
        return value.toString();
    }


    @Override
    public Integer getSize() {
        if (value instanceof Integer)
            return 32;
        else if (value instanceof Float)
            return 32;
        else if (value instanceof Long)
            return 64;
        else if (value instanceof Double)
            return 64;
        else return -1;
    }
}
