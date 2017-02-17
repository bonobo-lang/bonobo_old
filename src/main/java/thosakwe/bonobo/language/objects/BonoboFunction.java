package thosakwe.bonobo.language.objects;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.BonoboType;
import thosakwe.bonobo.language.types.BonoboAbstractClassImpl;
import thosakwe.bonobo.language.types.BonoboFunctionType;
import thosakwe.bonobo.language.types.BonoboUnknownType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class BonoboFunction extends BonoboObject {

    private final List<BonoboFunctionParameter> parameters = new ArrayList<>();
    private String name = null;
    private final ParserRuleContext source;
    private BonoboType returnType = BonoboUnknownType.INSTANCE;

    public BonoboFunction(ParserRuleContext source) {
        this.source = source;
    }

    public List<BonoboFunctionParameter> getParameters() {
        return parameters;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BonoboType getReturnType() {
        return returnType;
    }

    public void setReturnType(BonoboType returnType) {
        this.returnType = returnType;
    }

    @Override
    public BonoboType getType() {
        return new BonoboAbstractClassImpl(computeSignature()) {
            @Override
            public BonoboType typeForInvoke(Collection<BonoboType> arguments, ParserRuleContext source) throws BonoboException {
                // TODO: Map when called with lists
                if (arguments.size() > parameters.size()) {
                    throw new BonoboException(
                            String.format("Function %s called with too many arguments; expected %d.",
                                    computeSignature(), parameters.size()),
                            source);
                } else if (arguments.size() == parameters.size()) {
                    // Called with the right number of arguments, thank God!
                    return returnType;
                } else {
                    // Curry that bwoi
                    BonoboFunction curried = new BonoboFunction(source);
                    curried.setName(name);
                    curried.setReturnType(returnType);

                    // Add missing params
                    int diff = parameters.size() - arguments.size();

                    for (int i = 0; i < diff; i++) {
                        curried.parameters.add(parameters.get(parameters.size() - diff));
                    }

                    return curried.getType();
                }
            }
        };
    }

    private String computeSignature() {
        StringBuilder buf = new StringBuilder();

        buf.append("fn (");

        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0)
                buf.append(", ");
            BonoboFunctionParameter parameter = parameters.get(i);
            buf.append(String.format("%s:%s", parameter.getName(), parameter.getType().getName()));
        }

        buf.append(")");
        buf.append(String.format(" => %s", returnType.getName()));

        return buf.toString();
    }

    @Override
    public ParserRuleContext getSource() {
        return source;
    }
}
