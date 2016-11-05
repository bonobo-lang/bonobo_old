package thosakwe.strongly_typed.compiler.codegen.c;

import thosakwe.strongly_typed.analysis.Scope;
import thosakwe.strongly_typed.compiler.CodeBuilder;
import thosakwe.strongly_typed.lang.errors.CompilerError;

public interface CAstNode {
    void apply(CodeBuilder builder, Scope symbolTable) throws CompilerError;
}
