package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.analysis.Scope;
import thosakwe.bonobo.compiler.CodeBuilder;
import thosakwe.bonobo.compiler.CompilerError;

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
