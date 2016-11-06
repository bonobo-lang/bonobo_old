package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.analysis.Scope;
import thosakwe.bonobo.compiler.CodeBuilder;
import thosakwe.bonobo.compiler.CompilerError;

public class CVariableDeclarationStatement extends CStatement {
    private final String name;
    private final CExpression value;
    private final String type;

    public CVariableDeclarationStatement(String name, String type, CExpression value) {
        super();
        this.name = name;
        this.value = value;
        this.type = type;
    }

    @Override
    public void apply(CodeBuilder builder, Scope symbolTable) throws CompilerError {
        builder.println(String.format("%s %s = %s;", type, name, value.compileToC(builder)));
    }
}
