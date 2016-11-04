package thosakwe.strongly_typed;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Pair;
import thosakwe.strongly_typed.grammar.JsonBaseVisitor;
import thosakwe.strongly_typed.grammar.JsonLexer;
import thosakwe.strongly_typed.grammar.JsonParser;

import java.util.*;
import java.util.stream.Collectors;

public class Json {
    public static Object parse(String text) {
        final ANTLRInputStream inputStream = new ANTLRInputStream(text);
        final JsonLexer lexer = new JsonLexer(inputStream);
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        final JsonParser parser = new JsonParser(tokenStream);
        final JsonParser.JsonContext ast = parser.json();

        return new JsonBaseVisitor() {
            @Override
            public Object visitJson(JsonParser.JsonContext ctx) {
                return visitValue(ctx.value());
            }

            @Override
            public Map<String, Object> visitObject(JsonParser.ObjectContext ctx) {
                final Map<String, Object> result = new HashMap<>();
                ctx.pair().stream().map(this::visitPair).forEach(pair -> result.put(pair.a, pair.b));
                return result;
            }

            @Override
            public Pair<String, Object> visitPair(JsonParser.PairContext ctx) {
                final String key = ctx.STRING().getText().replaceAll("(^\")|(\"$)", "");
                final Object value = visitValue(ctx.value());
                return new Pair<>(key, value);
            }

            @Override
            public List visitArray(JsonParser.ArrayContext ctx) {
                return ctx.value().stream().map(this::visitValue).collect(Collectors.toList());
            }

            @Override
            public Object visitValue(JsonParser.ValueContext ctx) {
                if (ctx.NUMBER() != null) {
                    return Integer.parseInt(ctx.NUMBER().getText());
                } else if (ctx.STRING() != null) {
                    return ctx.STRING().getText().replaceAll("(^\")|(\"$)", "");
                } else if (ctx.getText().equals("true")) {
                    return true;
                } else if (ctx.getText().equals("false")) {
                    return false;
                } else if (ctx.object() != null) {
                    return visitObject(ctx.object());
                } else if (ctx.array() != null) {
                    return visitArray(ctx.array());
                } else return null;
            }
        }.visitJson(ast);
    }

    private static String stringify(List collection) {
        final StringBuilder builder = new StringBuilder("[");

        for (int i = 0; i < collection.size(); i++) {
            if (i > 0)
                builder.append(",");
            builder.append(stringify(collection.get(i)));
        }

        return builder.toString() + "]";
    }

    private static String stringifyMap(Map map) {
        final StringBuilder builder = new StringBuilder("{");
        final Set keySet = map.keySet();
        int i = 0;

        for (Object key : keySet) {
            if (i++ > 0) {
                builder.append(",");
            }

            builder.append(String.format("\"%s\":%s", key, stringify(map.get(key))));
        }

        return builder.toString() + "}";
    }

    private static String stringify(Object[] arr) {
        final List<Object> list = new ArrayList<>();
        Collections.addAll(list, arr);
        return stringify(list);
    }

    public static String stringify(Object object) {
        if (object instanceof JsonSerializable) {
            return stringify(((JsonSerializable) object).toJson());
        } else if (object instanceof String) {
            return String.format("\"%s\"", ((String) object).replaceAll("\"", "\\\""));
        } else if (object instanceof Integer || object instanceof Boolean) {
            return object.toString();
        } else if (object instanceof List) {
            return stringify((List) object);
        } else if (object instanceof Object[]) {
            return stringify((Object[]) object);
        } else if (object instanceof Map) {
            return stringifyMap((Map) object);
        } else if (object == null) {
            return "null";
        }

        throw new IllegalArgumentException(String.format("Cannot serialize %s to JSON.", object));
    }
}
