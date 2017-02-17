package thosakwe.bonobo.language.types;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboType;

import java.util.Collection;

public class BonoboAbstractClassImpl extends BonoboType {
    public static final BonoboAbstractClassImpl TYPEOF = new BonoboAbstractClassImpl("Type");
    private final String name;
    private final BonoboType parentType;

    public BonoboAbstractClassImpl(String name) {
        this(name, null);
    }

    public BonoboAbstractClassImpl(String name, BonoboType parentType) {
        this.name = name;
        this.parentType = parentType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BonoboType getParentType() {
        return parentType;
    }

    private BonoboType typeForArithmetic(String operator, BonoboType otherType, ParserRuleContext source) throws BonoboException {
        throw BonoboException.unsupportedOperator(this, operator, source);
    }

    @Override
    public BonoboType typeForConstruct(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        throw BonoboException.cannotInstantiateAbstractType(name, source);
    }

    @Override
    public BonoboType typeForInvoke(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        throw BonoboException.notAFunction(BonoboAbstractClassImpl.TYPEOF, source);
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
        return typeForArithmetic("-", otherType, source);
    }
}
