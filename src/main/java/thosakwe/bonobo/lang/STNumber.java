package thosakwe.bonobo.lang;

import thosakwe.bonobo.compiler.codegen.c.CExpression;
import thosakwe.bonobo.compiler.codegen.c.CNumberExpression;

public class STNumber extends STDatum {
    private final Number value;

    public STNumber(Number value) {
        super(typeForValue(value));
        this.value = value;
    }

    public Number getValue() {
        return value;
    }

    @Override
    public CExpression toCExpression() {
        return new CNumberExpression(value);
    }

    private static STType typeForValue(Number value) {
        // Todo: All number types

        if (value instanceof Integer)
            return STType.INT32;
        return null;
    }
}
