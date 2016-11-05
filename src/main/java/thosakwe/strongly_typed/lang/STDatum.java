package thosakwe.strongly_typed.lang;

import thosakwe.strongly_typed.compiler.codegen.c.CExpression;

public abstract class STDatum {
    private final STType type;

    public STDatum(STType type) {
        this.type = type;
    }

    public STType getType() {
        return type;
    }

    public boolean isPointer() {
        return type.isPointerType();
    }

    public abstract CExpression toCExpression();
}
