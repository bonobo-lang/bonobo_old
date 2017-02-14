package thosakwe.bonobo.analysis;

import thosakwe.bonobo.language.BonoboObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Scope {
    private Scope parent = null;
    private final List<Symbol> symbols = new ArrayList<>();

    public Scope() {}

    private Scope(Scope parent, Collection<Symbol> symbols) {
        this();
        this.parent = parent;
        this.symbols.addAll(symbols);
    }

    public Symbol findOrCreate(String key) {
        Symbol resolved = this.getSymbol(key);

        if (resolved == null) {
            resolved = new Symbol(key);
            symbols.add(resolved);
        }

        return resolved;
    }


    public Scope fork() {
        return new Scope(this, symbols);
    }

    public Scope join() {
        if (parent == null)
            throw new NullPointerException("The root scope does not have a parent.");
        return parent;
    }

    public List<Symbol> getExports(boolean importPrivate) {
        List<Symbol> exports = new ArrayList<>();
        List<String> names = new ArrayList<>();
        Scope scope = this;

        while (scope != null) {
            for (Symbol symbol : scope.symbols) {
                if ((importPrivate || symbol.getName().indexOf('_') != 0)
                        && !names.contains(symbol.getName())) {
                    exports.add(symbol);
                    names.add(symbol.getName());
                }
            }

            scope = scope.parent;
        }

        return exports;
    }

    public Scope getRoot() {
        Scope root = this;

        while (root.parent != null)
            root = root.parent;

        return root;
    }

    public Symbol getSymbol(String key) {
        for (Symbol symbol : symbols) {
            if (symbol.getName().equals(key))
                return symbol;
        }

        return parent != null ? parent.getSymbol(key) : null;
    }

    public Symbol putFinal(String name, BonoboObject value) {
        return put(name, value, true);
    }

    public Symbol put(String name, BonoboObject value) {
        return put(name, value, false);
    }


    public Symbol put(String name, BonoboObject value, boolean isFinal) {
        // TODO: Check if already exists
        Symbol symbol = new Symbol(name, value, isFinal);
        symbols.add(symbol);
        return symbol;
    }
}
