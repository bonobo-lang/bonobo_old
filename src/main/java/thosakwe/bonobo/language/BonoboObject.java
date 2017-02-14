package thosakwe.bonobo.language;

import org.antlr.v4.runtime.ParserRuleContext;

public abstract class BonoboObject {
    public abstract BonoboType getType();

    public abstract ParserRuleContext getSource();
}
