package thosakwe.strongly_typed.compiler.codegen.c;

import thosakwe.strongly_typed.compiler.CodeBuilder;

public class CStringExpression extends CExpression {
    private String value;

    public CStringExpression(String value) {
        this.value = value;
    }

    @Override
    public String compileToC(CodeBuilder builder) {
        // Todo: Escape everything
        final String safeString = value
                .replaceAll("\'", "'")
                .replaceAll("\"", "\\\"");

        return String.format("\"%s\"", safeString);
    }

    public CStringExpression append(String appendant) {
        return new CStringExpression(value + appendant);
    }
}
