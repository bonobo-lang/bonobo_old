package thosakwe.strongly_typed.compiler.codegen.c;

import thosakwe.strongly_typed.compiler.CodeBuilder;

public interface CAstNode {
    void apply(CodeBuilder builder);
}
