package thosakwe.strongly_typed.compiler.codegen.c;

import thosakwe.strongly_typed.compiler.CodeBuilder;

public abstract class CExpression {
    public abstract String compileToC(CodeBuilder builder);
}
