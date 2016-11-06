package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.Bonobo;
import thosakwe.bonobo.analysis.Scope;
import thosakwe.bonobo.compiler.CodeBuilder;
import thosakwe.bonobo.compiler.CompilerError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CProgram implements CAstNode {
    private final List<CFunctionDeclaration> functions = new ArrayList<>();
    private final List<String> includes = new ArrayList<>();

    public List<CFunctionDeclaration> getFunctions() {
        return functions;
    }

    public List<String> getIncludes() {
        return includes;
    }

    @Override
    public void apply(CodeBuilder builder, Scope symbolTable) throws CompilerError {
        builder.println(String.format("/* Compiled by Bonobo v%s. */", Bonobo.VERSION));
        builder.println();

        for (String include : includes) {
            builder.println(String.format("#include %s", include.startsWith("<") ? include : String.format("\"%s\"", include)));
            builder.println();
        }

        for (CFunctionDeclaration function : functions.stream().filter(fn -> fn != null).collect(Collectors.toList())) {
            function.apply(builder, symbolTable);
            builder.println();
        }
    }
}
