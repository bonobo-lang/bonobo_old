package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.analysis.Scope;
import thosakwe.bonobo.compiler.CodeBuilder;
import thosakwe.bonobo.compiler.CompilerError;

import java.io.Serializable;

public class CComment extends CStatement {
    private final Serializable value;

    public CComment(String value) {
        super();
        this.value = value;
    }

    @Override
    public void apply(CodeBuilder builder, Scope symbolTable) throws CompilerError {
        builder.printf("// %s%n", value);
    }
}
