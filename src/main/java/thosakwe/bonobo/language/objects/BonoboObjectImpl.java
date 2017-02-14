package thosakwe.bonobo.language.objects;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.BonoboType;

public class BonoboObjectImpl extends BonoboObject {
    private final BonoboType type;
    private final ParserRuleContext source;

    public BonoboObjectImpl(BonoboType type, ParserRuleContext source) {
        this.type = type;
        this.source = source;
    }

    @Override
    public BonoboType getType() {
        return type;
    }

    @Override
    public ParserRuleContext getSource() {
        return source;
    }
}
