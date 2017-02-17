package thosakwe.bonobo.language.types;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboType;

import java.util.ArrayList;
import java.util.List;

public class BonoboGenericType extends BonoboAbstractClassImpl {
    private final List<BonoboTypeParameter> typeParameters = new ArrayList<>();
    private final String name;

    public BonoboGenericType(String name) {
        super(name);
        this.name = name;
    }

    public BonoboGenericType(String name, BonoboType parentType) {
        super(name, parentType);
        this.name = name;
    }

    @Override
    public String getName() {
        StringBuilder buf = new StringBuilder();

        buf.append(name);

        if (typeParameters.isEmpty()) {
            buf.append("<");

            for (int i = 0; i < typeParameters.size(); i++) {
                if (i > 0)
                    buf.append(",");

                BonoboTypeParameter parameter = typeParameters.get(i);
                buf.append(parameter.getResolvedType() != null ? parameter.getResolvedType().getName() : parameter.getName());
            }

            buf.append(">");
        }

        return buf.toString();
    }

    public List<BonoboTypeParameter> getTypeParameters() {
        return typeParameters;
    }

    BonoboGenericType resolveTypeForParameter(String name, BonoboType type, ParserRuleContext source) throws BonoboException {
        BonoboGenericType result = new BonoboGenericType(this.name);
        boolean assigned = false;

        for (BonoboTypeParameter parameter : typeParameters) {
            if (parameter.getName().equals(name)) {
                if (parameter.getResolvedType() != null)
                    throw new BonoboException(String.format("Type parameter \"%s\" has already been resolved for %s.", name, this.name), source);
                else {
                    BonoboTypeParameter p = parameter.duplicate();
                    p.setResolvedType(type);
                    result.typeParameters.add(p);
                    assigned = true;
                }
            } else result.typeParameters.add(parameter.duplicate());
        }

        if (assigned)
            return result;
        throw new BonoboException(String.format("Generic type %s declares no type parameter \"%s\".", getName(), name), source);
    }
}
