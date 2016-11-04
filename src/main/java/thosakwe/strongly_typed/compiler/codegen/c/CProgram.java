package thosakwe.strongly_typed.compiler.codegen.c;

import thosakwe.strongly_typed.compiler.CodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class CProgram implements CAstNode {
    private final List<CFunctionDeclaration> functions = new ArrayList<>();
    private final List<String> imports = new ArrayList<>();

    public List<CFunctionDeclaration> getFunctions() {
        return functions;
    }

    public List<String> getImports() {
        return imports;
    }

    @Override
    public void apply(CodeBuilder builder) {
        for (String im : imports) {
            builder.println(String.format("import %s", im.startsWith("<") ? im : String.format("\"%s\"", im)));
        }

        for (CFunctionDeclaration function : functions) {
            builder.println();
            function.apply(builder);
        }
    }
}
