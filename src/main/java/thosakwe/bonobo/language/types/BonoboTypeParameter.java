package thosakwe.bonobo.language.types;

import thosakwe.bonobo.language.BonoboType;

public class BonoboTypeParameter {
    private final String name;
    private BonoboType resolvedType = null;

    public BonoboTypeParameter(String name) {
        this(name, null);
    }

    public BonoboTypeParameter(String name, BonoboType resolvedType) {
        this.name = name;
        this.resolvedType = resolvedType;
    }

    public String getName() {
        return name;
    }

    public BonoboType getResolvedType() {
        return resolvedType;
    }

    public void setResolvedType(BonoboType resolvedType) {
        this.resolvedType = resolvedType;
    }

    public BonoboTypeParameter duplicate() {
        return new BonoboTypeParameter(name, resolvedType);
    }
}
