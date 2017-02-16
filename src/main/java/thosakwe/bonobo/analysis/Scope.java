package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.language.BonoboObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Scope {
    private Scope globalScope = null;
    private final List<Scope> children = new ArrayList<>();
    private final ParserRuleContext source;
    private final List<Symbol> symbols = new ArrayList<>();
    private boolean debug = false;
    private Scope parent = null;

    public Scope() {
        this(false, null);
    }

    public Scope(boolean debug, ParserRuleContext source) {
        this.debug = debug;
        this.source = source;
    }

    private Scope(Scope parent, Collection<Symbol> symbols, ParserRuleContext source) {
        this(parent.debug, source);
        this.parent = parent;
        this.symbols.addAll(symbols);
    }

    private void createGlobal() {
        if (getRoot().globalScope == null)
            getRoot().globalScope = new Scope(getRoot(), new ArrayList<>(), source);
    }

    public Symbol findOrCreate(String key) {
        Symbol resolved = this.getSymbol(key);

        if (resolved == null) {
            resolved = new Symbol(key);
            symbols.add(resolved);
        }

        return resolved;
    }


    public Scope fork(ParserRuleContext source) {
        Scope scope = new Scope(this, symbols, source);
        children.add(scope);
        return scope;
    }

    public Scope join() {
        if (parent == null)
            throw new NullPointerException("The root scope does not have a parent.");
        return parent;
    }

    public List<Symbol> getExports(boolean importPrivate) {
        return getUnique().stream()
                .filter(symbol -> importPrivate || !symbol.getName().startsWith("_"))
                .collect(Collectors.toList());
    }

    public Scope getGlobalScope() {
        return getRoot().globalScope;
    }

    public Scope getRoot() {
        Scope root = this;

        while (root.parent != null)
            root = root.parent;

        return root;
    }

    public List<Symbol> getUnique() {
        List<Symbol> unique = new ArrayList<>();
        List<String> names = new ArrayList<>();
        Scope scope = this;

        while (scope != null) {
            for (Symbol symbol : scope.symbols) {
                if (!names.contains(symbol.getName())) {
                    unique.add(symbol);
                    names.add(symbol.getName());
                }
            }

            scope = scope.parent;
        }

        return unique;
    }

    public ParserRuleContext getSource() {
        return source;
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

        if (debug) {
            System.out.printf("Setting %s symbol %s to %s%n", isFinal ? "constant" : "mutable", name, value.getType().getName());
        }

        Scope global = getGlobalScope();

        if (global != null) {
            List<Symbol> toRemove = global.symbols.stream()
                    .filter(sym -> sym.getName().equals(name))
                    .collect(Collectors.toList());
            toRemove.forEach(global.symbols::remove);
            global.symbols.add(symbol);
        } else if (debug) {
            System.out.println("No global scope found to push to.");
        }

        symbols.add(symbol);
        return symbol;
    }

    public List<Scope> getChildren() {
        return children;
    }

    public int size() {
        return symbols.size();
    }

    public static Scope startGlobal(boolean debug, ParserRuleContext source) {
        Scope scope = new Scope(debug, source);
        scope.createGlobal();
        return scope;
    }
}
