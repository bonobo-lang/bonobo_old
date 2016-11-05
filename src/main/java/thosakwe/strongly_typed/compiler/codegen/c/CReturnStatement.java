package thosakwe.strongly_typed.compiler.codegen.c;

import thosakwe.strongly_typed.analysis.Scope;
import thosakwe.strongly_typed.compiler.CodeBuilder;
import thosakwe.strongly_typed.lang.errors.CompilerError;

public class CReturnStatement extends CStatement {
    private final CExpression returnValue;

    public CReturnStatement(CExpression returnValue) {
        super();
        this.returnValue = returnValue;
    }

    @Override
    public void apply(CodeBuilder builder, Scope symbolTable) throws CompilerError {
        builder.println(String.format("return %s;", returnValue.compileToC(builder)));
    }
}
