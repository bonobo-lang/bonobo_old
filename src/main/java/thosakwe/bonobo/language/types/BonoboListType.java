package thosakwe.bonobo.language.types;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboType;

import java.util.Collection;

public class BonoboListType extends BonoboType {
    public static final BonoboType TYPEOF = new BonoboAbstractClassImpl("List");

    private final BonoboType referenceType;

    public BonoboListType(BonoboType referenceType) {
        super(TYPEOF);
        this.referenceType = referenceType;
    }

    @Override
    public String getName() {
        return String.format("%s[]", referenceType.getName());
    }

    @Override
    public BonoboType typeForConstruct(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        throw BonoboException.noConstructor(this, source);
    }

    @Override
    public BonoboType typeForInvoke(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
        throw BonoboException.notAFunction(this, source);
    }

    @Override
    public BonoboType typeForIndex(BonoboType index, ParserRuleContext source) throws BonoboException {
        if (!index.isAssignableTo(BonoboIntegerType.INSTANCE))
            throw new BonoboException(String.format("Lists can only be indexed by integers. You tried using an instance of %s instead.", index.getName()), source);
        return referenceType;
    }

    @Override
    public BonoboType typeForPow(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return new BonoboListType(referenceType.typeForPow(otherType, source));
    }

    @Override
    public BonoboType typeForMultiply(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return new BonoboListType(referenceType.typeForMultiply(otherType, source));
    }

    @Override
    public BonoboType typeForDivide(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return new BonoboListType(referenceType.typeForDivide(otherType, source));
    }

    @Override
    public BonoboType typeForAdd(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return new BonoboListType(referenceType.typeForAdd(otherType, source));
    }

    @Override
    public BonoboType typeForSubtract(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return new BonoboListType(referenceType.typeForSubtract(otherType, source));
    }

    @Override
    public BonoboType typeForModulo(BonoboType otherType, ParserRuleContext source) throws BonoboException {
        return new BonoboListType(referenceType.typeForModulo(otherType, source));
    }

    @Override
    public boolean isAssignableTo(BonoboType otherType) {
        if (otherType instanceof BonoboListType) {
            return referenceType.isAssignableTo(((BonoboListType) otherType).referenceType);
        }

        return super.isAssignableTo(otherType);
    }

    public BonoboType getReferenceType() {
        return referenceType;
    }
}
