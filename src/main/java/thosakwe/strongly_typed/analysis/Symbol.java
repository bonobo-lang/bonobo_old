package thosakwe.strongly_typed.analysis;

import thosakwe.strongly_typed.lang.STDatum;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    private boolean _isFinal = false;
    private final String name;
    private final List<SymbolUsage> usages = new ArrayList<>();
    private STDatum value;

    public Symbol(String name, STDatum value, boolean isFinal) {
        this._isFinal = isFinal;
        this.name = name;
        this.value = value;
    }

    public Symbol(String name, STDatum value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public List<SymbolUsage> getUsages() {
        return usages;
    }

    public STDatum getValue() {
        return value;
    }

    public void markAsFinal() {
        _isFinal = true;
    }

    public boolean isFinal() {
        return _isFinal;
    }

    public String safeDelete(String src) {
        // Todo: Safe delete
        return src;
    }

    public void setValue(STDatum value) {
        this.value = value;
    }
}