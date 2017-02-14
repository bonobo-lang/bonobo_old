package thosakwe.bonobo.language;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.Bonobo;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.objects.BonoboFunction;
import thosakwe.bonobo.language.types.BonoboDoubleType;
import thosakwe.bonobo.language.types.BonoboListType;

public class BonoboException extends Exception {
    private final ParserRuleContext source;

    public BonoboException(String message, ParserRuleContext source) {
        super(message);
        this.source = source;
    }

    public ParserRuleContext getSource() {
        return source;
    }


    public static BonoboException unsupportedOperator(BonoboType type, String operator, ParserRuleContext source) {
        return new BonoboException(
                String.format("Type \"%s\" does not support operator \"%s\".", type.getName(), operator),
                source);
    }

    public static BonoboException cannotInstantiateAbstractType(String name, ParserRuleContext source) {
        return new BonoboException(String.format("Cannot instantiate abstract type \"%s\".", name), source);
    }

    public static BonoboException notAFunction(BonoboType type, ParserRuleContext source) {
        return new BonoboException(String.format("Cannot call instances of %s as functions.", type.getName()), source);
    }

    public static BonoboException noConstructor(BonoboType type, ParserRuleContext source) {
        return new BonoboException(String.format("Type \"%s\" has no constructor.", type.getName()), source);
    }

    public static BonoboException wrongTypeForOperator(BonoboType type, BonoboType otherType, String operator, ParserRuleContext source) {
        return new BonoboException(
                String.format(
                        "Cannot call operator \"%s\" on type \"%s\" with an instance of \"%s\".",
                        operator,
                        type.getName(),
                        otherType.getName()),
                source);
    }

    public static BonoboException invalidReturnForFunction(BonoboFunction function, BonoboType actuallyReturned, ParserRuleContext source) {
        return new BonoboException(
                String.format(
                        "Function \"%s\" is declared to return type \"%s\", but actually returns type \"%s\".",
                        function.getName(),
                        function.getReturnType().getName(),
                        actuallyReturned.getName()),
                source);
    }

    public static BonoboException unresolvedIdentifier(String name, ParserRuleContext source) {
        return new BonoboException(String.format("The name \"%s\" does not exist in the current context.", name), source);
    }
}
