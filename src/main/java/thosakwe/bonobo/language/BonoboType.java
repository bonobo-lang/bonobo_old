package thosakwe.bonobo.language;


import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.types.BonoboAbstractClassImpl;
import thosakwe.bonobo.language.types.BonoboUnknownType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class BonoboType {

    private final List<BonoboClassMember> members = new ArrayList<>();
    private BonoboType parentType = null;

    public BonoboType() {}

    public BonoboType(BonoboType parentType) {
        this.parentType = parentType;
    }

    public abstract String getName();

    public BonoboType getParentType() {
        return parentType;
    }

    public List<BonoboClassMember> getMembers() {
        return members;
    }

    public boolean isAssignableTo(BonoboType otherType) {
        if (otherType == this || otherType == BonoboUnknownType.INSTANCE) return true;

        BonoboType search = this;

        while (search != null) {
            if (search == otherType) return true;
            search = search.getParentType();
        }

        return false;
    }

    public BonoboType getCommonParentType(BonoboType otherType) {
        BonoboType search = this;

        while (search != null) {
            // System.out.printf("Searching: %s, otherType: %s%n", search.getName(), otherType.getName());
            if (isAssignableTo(search) && otherType.isAssignableTo(search)) {
                // System.out.printf("Common parent type: %s%n", search.getName());
                return search;
            }
            search = search.getParentType();
        }

        return null;
    }

    public abstract BonoboType typeForConstruct(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException;

    public abstract BonoboType typeForInvoke(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException;

    public abstract BonoboType typeForPow(BonoboType otherType, ParserRuleContext source) throws BonoboException;

    public abstract BonoboType typeForMultiply(BonoboType otherType, ParserRuleContext source) throws BonoboException;

    public abstract BonoboType typeForDivide(BonoboType otherType, ParserRuleContext source) throws BonoboException;

    public abstract BonoboType typeForAdd(BonoboType otherType, ParserRuleContext source) throws BonoboException;

    public abstract BonoboType typeForSubtract(BonoboType otherType, ParserRuleContext source) throws BonoboException;

    public abstract BonoboType typeForModulo(BonoboType otherType, ParserRuleContext source) throws BonoboException;
}
