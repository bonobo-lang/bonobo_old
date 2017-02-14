package thosakwe.bonobo.language.types;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboType;

import java.util.Collection;

public class BonoboUnknownType extends BonoboType {
    public static final BonoboUnknownType INSTANCE = new BonoboUnknownType();

    @Override
    public String getName() {
        return "<unknown type>";
    }

    @Override
    public BonoboType typeForConstruct(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        return INSTANCE;
    }

    @Override
    public BonoboType typeForInvoke(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        return INSTANCE;
    }

    @Override
    public BonoboType typeForPow(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return INSTANCE;
    }

    @Override
    public BonoboType typeForMultiply(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return INSTANCE;
    }

    @Override
    public BonoboType typeForDivide(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return INSTANCE;
    }

    @Override
    public BonoboType typeForAdd(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return INSTANCE;
    }

    @Override
    public BonoboType typeForSubtract(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return INSTANCE;
    }

    @Override
    public BonoboType typeForModulo(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return INSTANCE;
    }
}
