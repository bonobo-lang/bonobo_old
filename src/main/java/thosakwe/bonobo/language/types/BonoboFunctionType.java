package thosakwe.bonobo.language.types;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboType;

import java.util.Collection;

public class BonoboFunctionType extends BonoboType {
    public static final BonoboFunctionType INSTANCE = new BonoboFunctionType();

    @Override
    public String getName() {
        return "Function";
    }

    @Override
    public BonoboType typeForConstruct(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        throw BonoboException.cannotInstantiateAbstractType(getName(), source);
    }

    @Override
    public BonoboType typeForInvoke(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        // TODO: Create types for invocations
        return BonoboUnknownType.INSTANCE;
    }

    @Override
    public BonoboType typeForPow(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        throw BonoboException.unsupportedOperator(this, "^", source);
    }

    @Override
    public BonoboType typeForMultiply(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        throw BonoboException.unsupportedOperator(this, "*", source);
    }

    @Override
    public BonoboType typeForDivide(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        throw BonoboException.unsupportedOperator(this, "/", source);
    }

    @Override
    public BonoboType typeForAdd(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        throw BonoboException.unsupportedOperator(this, "+", source);
    }

    @Override
    public BonoboType typeForSubtract(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        throw BonoboException.unsupportedOperator(this, "-", source);
    }

    @Override
    public BonoboType typeForModulo(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        throw BonoboException.unsupportedOperator(this, "%", source);
    }
}
