package thosakwe.strongly_typed.lang;

import thosakwe.strongly_typed.compiler.codegen.c.CExpression;
import thosakwe.strongly_typed.compiler.codegen.c.CIntegerExpression;
import thosakwe.strongly_typed.lang.STDatum;
import thosakwe.strongly_typed.lang.STType;

public class STInteger extends STDatum {
    private final Integer value;

    public STInteger(Integer value) {
        super(STType.INT32);
        this.value = value;
    }

    @Override
    public CExpression toCExpression() {
        return new CIntegerExpression(value);
    }
}
