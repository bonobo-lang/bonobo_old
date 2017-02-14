package thosakwe.bonobo.language.objects;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboType;

public class BonoboFunctionParameter {
    private final String name;
    private final BonoboType type;
    private final ParserRuleContext source;

    public BonoboFunctionParameter(String name, BonoboType type, ParserRuleContext source) {
        this.name = name;
        this.type = type;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public BonoboType getType() {
        return type;
    }

    public ParserRuleContext getSource() {
        return source;
    }
}
