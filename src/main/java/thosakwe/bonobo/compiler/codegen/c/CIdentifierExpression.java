package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.compiler.CodeBuilder;

public class CIdentifierExpression extends CExpression {
    private final String name;

    public CIdentifierExpression(String name) {
        this.name = name;
    }

    @Override
    public String compileToC(CodeBuilder builder) {
        return name;
    }

    @Override
    public Integer getSize() {
        return -1;
    }
}
