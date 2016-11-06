package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.compiler.CodeBuilder;

public abstract class CExpression {
    public abstract String compileToC(CodeBuilder builder);

    public abstract Integer getSize();
}
