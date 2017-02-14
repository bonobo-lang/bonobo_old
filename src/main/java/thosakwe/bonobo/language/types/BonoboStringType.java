package thosakwe.bonobo.language.types;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboType;

public class BonoboStringType extends BonoboListType {
    public static final BonoboStringType INSTANCE = new BonoboStringType();

    public BonoboStringType() {
        super(BonoboByteType.INSTANCE);
    }

    @Override
    public BonoboType typeForAdd(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return INSTANCE;
    }
}
