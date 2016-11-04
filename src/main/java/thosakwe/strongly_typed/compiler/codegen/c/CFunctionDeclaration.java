package thosakwe.strongly_typed.compiler.codegen.c;

import org.antlr.v4.runtime.misc.Pair;
import thosakwe.strongly_typed.compiler.CodeBuilder;

import java.util.ArrayList;
import java.util.List;

public class CFunctionDeclaration implements CAstNode {
    private CBlock block;
    private String name, returnType;
    private List<Pair<String, String>> parameters = new ArrayList<>();

    @Override
    public void apply(CodeBuilder builder) {
        builder.print(String.format("%s %s(", returnType, name));

        for (int i = 0; i < parameters.size(); i++) {
            final Pair<String, String> param = parameters.get(i);

            if (i > 0)
                builder.write(", ");

            builder.write(String.format("%s %s", param.b, param.a));
        }

        builder.println(")");
        block.apply(builder);
    }

    public CBlock getBlock() {
        return block;
    }

    public void setBlock(CBlock block) {
        this.block = block;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    /**
     * pair.a = name, pair.b = type
     * @return List of function params
     */
    public List<Pair<String, String>> getParameters() {
        return parameters;
    }
}
