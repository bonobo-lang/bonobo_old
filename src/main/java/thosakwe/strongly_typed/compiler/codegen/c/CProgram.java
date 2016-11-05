package thosakwe.strongly_typed.compiler.codegen.c;

import thosakwe.strongly_typed.StronglyTyped;
import thosakwe.strongly_typed.analysis.Scope;
import thosakwe.strongly_typed.compiler.CodeBuilder;
import thosakwe.strongly_typed.lang.errors.CompilerError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CProgram implements CAstNode {
    private final List<CFunctionDeclaration> functionList = new ArrayList<>();
    private final List<String> imports = new ArrayList<>();

    public List<CFunctionDeclaration> getFunctionList() {
        return functionList;
    }

    public List<String> getImports() {
        return imports;
    }

    @Override
    public void apply(CodeBuilder builder, Scope symbolTable) throws CompilerError {
        builder.println(String.format("/* Compiled by StronglyTyped v%s. */", StronglyTyped.VERSION));
        builder.println();

        for (String im : imports) {
            builder.println(String.format("#include %s", im.startsWith("<") ? im : String.format("\"%s\"", im)));
            builder.println();
        }

        for (CFunctionDeclaration function : functionList.stream().filter(fn -> fn != null).collect(Collectors.toList())) {
            function.apply(builder, symbolTable);
            builder.println();
        }
    }
}
