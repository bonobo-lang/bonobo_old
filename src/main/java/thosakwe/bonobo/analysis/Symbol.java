package thosakwe.bonobo.analysis;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    private boolean _isFinal = false;
    private final String name;
    private final List<SymbolUsage> usages = new ArrayList<>();

    public Symbol(String name, boolean isFinal) {
        this._isFinal = isFinal;
        this.name = name;
    }

    public Symbol(String name) {
        this(name, false);
    }

    public String getName() {
        return name;
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