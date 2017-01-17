package thosakwe.bonobo.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Scope {
    private Scope parent = null;
    private final List<Symbol> symbols = new ArrayList<>();

    Scope() {
    }

    private Scope(Scope parent, Collection<Symbol> symbols) {
        this();
        this.parent = parent;
        this.symbols.addAll(symbols);
    }

    Symbol findOrCreate(String key) {
        Symbol resolved = this.getSymbol(key);

        if (resolved == null) {
            resolved = new Symbol(key);
            symbols.add(resolved);
        }

        return resolved;
    }


    Scope fork() {
        return new Scope(this, symbols);
    }

    Scope join() {
        if (parent == null)
            throw new NullPointerException("The root scope does not have a parent.");
        return parent;
    }

    List<Symbol> getExports(boolean importPrivate) {
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

    Scope getRoot() {
        Scope root = this;

        while (root.parent != null)
            root = root.parent;

        return root;
    }

    Symbol getSymbol(String key) {
        for (Symbol symbol : symbols) {
            if (symbol.getName().equals(key))
                return symbol;
        }

        return parent != null ? parent.getSymbol(key) : null;
    }
}
