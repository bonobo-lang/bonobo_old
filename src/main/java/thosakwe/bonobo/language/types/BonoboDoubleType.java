package thosakwe.bonobo.language.types;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboType;

import java.util.Collection;

public class BonoboDoubleType extends BonoboType {
    public static final BonoboDoubleType INSTANCE = new BonoboDoubleType();

    private BonoboDoubleType() {
        super(BonoboNumberType.INSTANCE);
    }

    @Override
    public String getName() {
        return "Double";
    }

    @Override
    public BonoboType typeForConstruct(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        throw BonoboException.cannotInstantiateAbstractType(getName(), source);
    }

    @Override
    public BonoboType typeForInvoke(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        throw BonoboException.notAFunction(this, source);
    }

    private BonoboType typeForArithmetic(String operator, BonoboType otherType, ParserRuleContext source) throws BonoboException {
        if (otherType.isAssignableTo(BonoboIntegerType.INSTANCE) || otherType.isAssignableTo(INSTANCE))
            return INSTANCE;
        throw BonoboException.wrongTypeForOperation(this, otherType, operator, source);
    }

    @Override
    public BonoboType typeForPow(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return typeForArithmetic("^", otherType, source);
    }

    @Override
    public BonoboType typeForMultiply(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return typeForArithmetic("*", otherType, source);
    }

    @Override
    public BonoboType typeForDivide(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return typeForArithmetic("/", otherType, source);
    }

    @Override
    public BonoboType typeForAdd(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return typeForArithmetic("+", otherType, source);
    }

    @Override
    public BonoboType typeForSubtract(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return typeForArithmetic("-", otherType, source);
    }

    @Override
    public BonoboType typeForModulo(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return BonoboNumberType.INSTANCE.modulo(this, otherType, source);
    }
}
