package thosakwe.bonobo.language;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.grammar.BonoboParser;

import java.util.HashMap;
import java.util.Map;

public class BonoboLibrary {
    private final Map<String, BonoboObject> exports = new HashMap<>();
    private final BonoboParser.CompilationUnitContext source;

    public BonoboLibrary(BonoboParser.CompilationUnitContext source) {
        this.source = source;
    }

    public Map<String, BonoboObject> getExports() {
        return exports;
    }

    public BonoboParser.CompilationUnitContext getSource() {
        return source;
    }

    public void addExport(String name, BonoboObject value, ParserRuleContext source) throws BonoboException {
        if (exports.containsKey(name))
            throw new BonoboException(String.format("Another value has already been exported as \"%s\".", name), source);
        exports.put(name, value);
    }
}
