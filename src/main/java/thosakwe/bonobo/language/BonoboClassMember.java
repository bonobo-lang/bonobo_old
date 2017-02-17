package thosakwe.bonobo.language;

import org.antlr.v4.runtime.ParserRuleContext;

public class BonoboClassMember {
    private final BonoboType owner;
    private final String name;
    private BonoboType getterOutputType = null, setterInputType = null;

    public BonoboClassMember(BonoboType owner, String name) {
        this.name = name;
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public BonoboType getGetterOutputType() {
        return getterOutputType;
    }

    public void setGetterOutputType(BonoboType getterOutputType) {
        this.getterOutputType = getterOutputType;
    }

    public BonoboType getSetterInputType() {
        return setterInputType;
    }

    public void setSetterInputType(BonoboType setterInputType) {
        this.setterInputType = setterInputType;
    }

    public BonoboType getValue(ParserRuleContext source) throws BonoboException {
        if (getterOutputType == null)
            throw new BonoboException(String.format("%s has no getter named \"%s\".", owner.getName(), name), source);
        return getterOutputType;
    }

    public BonoboType setValue(BonoboType inputType, ParserRuleContext source) throws BonoboException {
        if (setterInputType == null)
            throw new BonoboException(String.format("%s has no setter named \"%s\".", owner.getName(), name), source);
        if (!inputType.isAssignableTo(setterInputType))
            throw new BonoboException(
                    String.format("Setter \"%s\" expects a(n) %s, not a(n) %s.", name, setterInputType.getName(), inputType.getName()),
                    source);
        return inputType;
    }
}
