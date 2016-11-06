package thosakwe.bonobo.compiler.codegen.c;

import thosakwe.bonobo.compiler.CodeBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CInvocationExpression extends CExpression {
    private final CExpression callee;
    private final List<CExpression> arguments = new ArrayList<>();

    public CInvocationExpression(CExpression callee) {
        this.callee = callee;
    }

    public CInvocationExpression(CExpression callee, CExpression[] arguments) {
        this.callee = callee;
        Collections.addAll(this.arguments, arguments);
    }

    @Override
    public String compileToC(CodeBuilder builder) {
        final StringBuilder buf = new StringBuilder();
        final String target = callee.compileToC(builder);
        final List<String> args = arguments.stream()
                .filter(expression -> expression != null)
                .map(expression -> expression.compileToC(builder))
                .collect(Collectors.toList());

        buf.append(String.format("%s(", target));

        for (int i = 0; i < args.size(); i++) {
            if (i > 0)
                buf.append(", ");
            buf.append(args.get(i));
        }

        buf.append(")");
        return buf.toString();
    }

    @Override
    public Integer getSize() {
        return -1;
    }
}
