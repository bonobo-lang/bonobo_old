package thosakwe.bonobo.language.objects;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.BonoboType;
import thosakwe.bonobo.language.types.BonoboFunctionType;
import thosakwe.bonobo.language.types.BonoboUnknownType;

import java.util.ArrayList;
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
        return BonoboFunctionType.INSTANCE;
    }

    @Override
    public ParserRuleContext getSource() {
        return source;
    }
}
