package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboObject;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    private boolean _isFinal = false;
    private final String name;
    private final List<SymbolUsage> usages = new ArrayList<>();
    private BonoboObject value = null;

    public Symbol(String name, BonoboObject value, boolean isFinal) {
        this._isFinal = isFinal;
        this.name = name;
        this.value = value;
    }

    public Symbol(String name) {
        this(name, null, false);
    }

    public String getName() {
        return name;
    }

    public BonoboObject getValue() {
        return value;
    }

    public void setValue(BonoboObject value, ParserRuleContext source) throws BonoboException {
        if (isFinal())
            throw new BonoboException(String.format("Cannot overwrite immutable symbol \"%s\".", name), source);
        this.value = value;
    }

    public List<SymbolUsage> getUsages() {
        return usages;
    }

    public void markAsFinal() {
        _isFinal = true;
    }

    public boolean isFinal() {
        return _isFinal;
    }

    public String safeDelete(String src) {
        // Todo: safe delete
        throw new UnsupportedOperationException("safeDelete is not yet implemented.");
    }
}