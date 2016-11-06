package thosakwe.bonobo.compiler.codegen.c;

public class CNullExpression extends CLiteralExpression {
    public CNullExpression(Integer size) {
        super(size, "NULL");
    }
}
