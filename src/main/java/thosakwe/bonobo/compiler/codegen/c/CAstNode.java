package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.analysis.Scope;
import thosakwe.bonobo.compiler.CodeBuilder;
import thosakwe.bonobo.compiler.CompilerError;

public interface CAstNode {
    void apply(CodeBuilder builder, Scope symbolTable) throws CompilerError;
}
