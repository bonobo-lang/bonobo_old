package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.analysis.Scope;
import thosakwe.bonobo.compiler.CodeBuilder;
import thosakwe.bonobo.compiler.CompilerError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CMultipleStatements extends CStatement {
    private final List<CStatement> children = new ArrayList<>();

    public CMultipleStatements() {

    }

    public CMultipleStatements(CStatement[] children) {
        super();
        Collections.addAll(this.children, children);
    }

    @Override
    public void apply(CodeBuilder builder, Scope symbolTable) throws CompilerError {
        for (CStatement child : children)
            child.apply(builder, symbolTable);
    }

    public List<CStatement> getChildren() {
        return children;
    }
}
