package thosakwe.strongly_typed.analysis;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.strongly_typed.compiler.CodeBuilder;
import thosakwe.strongly_typed.lang.STDatum;
import thosakwe.strongly_typed.lang.errors.CompilerError;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Scope {
    private Scope child = null;
    private final List<ScopeEventListener> eventListeners = new ArrayList<>();
    private final List<Symbol> symbols = new ArrayList<>();
    private Scope parent = null;
    private STDatum thisContext = null;
    String sourceFile = null;

    public void addEventListener(ScopeEventListener listener) {
        this.eventListeners.add(listener);
    }

    public Scope create() {
        final Scope innermost = getInnerMostScope();
        final Scope child = new Scope();
        child.parent = innermost;
        innermost.child = child;

        for (ScopeEventListener listener : eventListeners) {
            listener.onCreate(child);
        }

        return child;
    }

    public Scope getInnerMostScope() {
        Scope innermost = this;

        while (innermost.child != null)
            innermost = innermost.child;

        return innermost;
    }


    public Symbol getSymbol(String name) {
        Scope currentScope = getInnerMostScope();

        // Now, backtrack to top
        do {
            for (Symbol symbol : currentScope.symbols) {
                if (symbol.getName().equals(name))
                    return symbol;
            }

            currentScope = currentScope.parent;
        } while (currentScope != null);

        return null;
    }

    public STDatum getValue(String name) {
        final Symbol resolved = getSymbol(name);

        if (resolved == null)
            return null;
        else return resolved.getValue();
    }

    public void destroy() {
        final Scope innermost = getInnerMostScope();

        if (innermost.parent != null) {
            innermost.parent.child = null;
        }

        for (ScopeEventListener listener : eventListeners) {
            listener.onDestroy(innermost);
        }
    }

    public void dumpSymbols() {
        int level = 1;
        Scope currentScope = this;
        System.out.println("DUMPING SYMBOLS:");

        do {
            if (!currentScope.symbols.isEmpty())
                System.out.printf("Level %d (%d symbol(s)):%n", level++, currentScope.symbols.size());
            else System.out.printf("Level %d (empty)%n", level++);

            for (Symbol symbol : currentScope.symbols) {
                System.out.printf("  - %s: ", symbol.getName());
                System.out.println(symbol.getValue());
            }

            currentScope = currentScope.child;
        } while (currentScope != null);
    }

    public void load(Scope other, boolean importPrivate) {
        create();

        // Go top to bottom
        Scope currentScope = other;
        do {
            final Scope innermost = getInnerMostScope();

            innermost.symbols
                    .addAll(currentScope.symbols.stream()
                            .filter(symbol -> !symbol.getName().startsWith("_") || importPrivate)
                            .collect(Collectors.toList()));

            currentScope = currentScope.child;

            if (currentScope != null)
                create();
        } while (currentScope != null);
    }

    public void load(Scope other) {
        load(other, false);
    }

    public void removeEventListener(ScopeEventListener listener) {
        this.eventListeners.remove(listener);
    }

    public Symbol setFinal(String name, STDatum value, ParserRuleContext source) throws CompilerError {
        return setValue(name, value, source, true);
    }

    public Symbol setValue(String name, STDatum value, ParserRuleContext source, boolean isFinal) throws CompilerError {
        final Symbol resolved = getSymbol(name);

        if (resolved == null) {
            final Symbol symbol = new Symbol(name, value, isFinal);
            getInnerMostScope().symbols.add(symbol);
            return symbol;
        } else if (!resolved.isFinal()) {
            resolved.setValue(value);
            return resolved;
        }
        else
            throw new CompilerError(CompilerError.ERROR, String.format("Cannot overwrite final variable '%s'.", resolved.getName()), source, getInnerMostScope().sourceFile);

    }

    public Symbol setValue(String name, STDatum value, ParserRuleContext source) throws CompilerError {
        return setValue(name, value, source, false);
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public Symbol resolveOrCreate(String name) {
        final Symbol resolved = getSymbol(name);

        if (resolved != null)
            return resolved;
        else {
            final Symbol symbol = new Symbol(name, null);
            getInnerMostScope().symbols.add(symbol);
            return symbol;
        }
    }

    public void createNew(String name, STDatum value, ParserRuleContext source, boolean isFinal) throws CompilerError {
        final List<Symbol> symbols = getInnerMostScope().symbols;
        Symbol predefined = null;

        for (Symbol symbol : symbols) {
            if (symbol.getName().equals(name))
                predefined = symbol;
        }

        if (predefined != null)
            throw new CompilerError(CompilerError.ERROR, String.format("Symbol '%s' is already defined with this scope.", name), source, getInnerMostScope().sourceFile);
        else {
            symbols.add(new Symbol(name, value, isFinal));
        }
    }

    public void createNew(String name, STDatum value, ParserRuleContext source) throws CompilerError {
        createNew(name, value, source, false);
    }

    public STDatum getThisContext() {
        Scope currentScope = getInnerMostScope();

        while (currentScope != null) {
            if (currentScope.thisContext != null)
                return currentScope.thisContext;

            currentScope = currentScope.parent;
        }

        return null;
    }

    public void setThisContext(STDatum thisContext) {
        this.thisContext = thisContext;
    }

    public List<Symbol> allUnique(boolean importPrivate) {
        final List<Symbol> result = new ArrayList<>();
        final List<String> added = new ArrayList<>();
        Scope currentScope = getInnerMostScope();

        while (currentScope != null) {
            for (Symbol symbol : currentScope.symbols) {
                if (!added.contains(symbol.getName())) {
                    if (!symbol.getName().startsWith("_") || importPrivate) {
                        added.add(symbol.getName());
                        result.add(symbol);
                    }
                }
            }

            currentScope = currentScope.parent;
        }

        return result;
    }

    public List<Symbol> allUnique() {
        return allUnique(false);
    }
/*
    public void load(FrayLibrary from, boolean importPrivate) {
        getInnerMostScope().symbols.addAll((importPrivate ? from.getExportedSymbols() : from.getPublicSymbols()));
    }

    public void load(FrayLibrary from) {
        load(from, false);
    }
    */

    public Symbol put(String name, STDatum value,  boolean isFinal) {
        final Symbol result = new Symbol(name, value, isFinal);
        getInnerMostScope().symbols.add(result);
        return result;
    }

    public Symbol put(String name, STDatum value) {
        return put(name, value, false);
    }

    public Symbol putFinal(String name, STDatum value) {
        return put(name, value,  true);
    }
}