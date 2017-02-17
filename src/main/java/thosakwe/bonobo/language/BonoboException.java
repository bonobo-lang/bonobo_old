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
                String.format("Type \"%s\" does not support operator %s.", type.getName(), operator),
                source);
    }

    public static BonoboException cannotInstantiateAbstractType(String name, ParserRuleContext source) {
        return new BonoboException(String.format("Cannot instantiate abstract type %s.", name), source);
    }

    public static BonoboException notAFunction(BonoboType type, ParserRuleContext source) {
        return new BonoboException(String.format("Cannot call instances of %s as functions.", type.getName()), source);
    }

    public static BonoboException noConstructor(BonoboType type, ParserRuleContext source) {
        return new BonoboException(String.format("Type %s has no constructor.", type.getName()), source);
    }

    public static BonoboException wrongTypeForOperation(BonoboType type, BonoboType otherType, String operator, ParserRuleContext source) {
        return new BonoboException(
                String.format(
                        "Cannot performation operation %s on type %s with an instance of %s.",
                        operator,
                        type.getName(),
                        otherType.getName()),
                source);
    }

    public static BonoboException invalidReturnForFunction(BonoboFunction function, BonoboType actuallyReturned, ParserRuleContext source) {
        return new BonoboException(
                String.format(
                        "Function \"%s\" is declared to return %s, but actually returns data of type %s.",
                        function.getName(),
                        function.getReturnType().getName(),
                        actuallyReturned.getName()),
                source);
    }

    public static BonoboException unresolvedIdentifier(String name, ParserRuleContext source) {
        return new BonoboException(String.format("The name \"%s\" does not exist in the current context.", name), source);
    }

    public static BonoboException cannotCast(BonoboType from, BonoboType to, ParserRuleContext source) {
        return new BonoboException(String.format(
                "Cannot cast %s to %s, as %s does not derive from %s.",
                from.getName(), to.getName(), from.getName(), to.getName()),
                source);
    }

    public static BonoboException noCommonTypeFor(String container, ParserRuleContext source) {
        return new BonoboException(String.format("Cannot resolve common type from values in this %s. Ensure that all members share a common base type.", container), source);
    }

    public static BonoboException incomparableTypes(BonoboType left, BonoboType right, ParserRuleContext source) {
        return new BonoboException(
                String.format(
                        "Instances of %s and %s cannot be compared, as they do not share a base type.",
                        left.getName(),
                        right.getName()),
                source);
    }

    public static BonoboException logicalComparison(ParserRuleContext source) {
        return new BonoboException("Logical comparisons may only be performed on two booleans.", source);
    }

    private static BonoboException unresolvedMember(BonoboType type, String name, String accessorType, ParserRuleContext source) {
        return new BonoboException(String.format("Type %s has no %s named \"%s\".", type.getName(), accessorType, name), source);
    }

    public static BonoboException unresolvedGetter(BonoboType type, String name, ParserRuleContext source) {
        return unresolvedMember(type, name, "getter", source);
    }

    public static BonoboException unresolvedSetter(BonoboType type, String name, ParserRuleContext source) {
        return unresolvedMember(type, name, "setter", source);
    }
}
