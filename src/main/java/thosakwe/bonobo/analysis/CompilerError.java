package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;
import thosakwe.bonobo.JsonSerializable;

import java.util.HashMap;
import java.util.Map;

public class CompilerError extends Throwable implements JsonSerializable {
    public static final String ERROR = "error";
    public static final String WARNING = "warning";

    private final String message;
    private final ParserRuleContext source;
    private final String sourceFile;
    private final Pair<Integer, Integer> stop;
    private final String type;

    public CompilerError(String type, String message, ParserRuleContext source, String sourceFile) {
        this.message = message;
        this.source = source;
        this.sourceFile = sourceFile;
        this.type = type;

        if (source != null)
            this.stop = new Pair<>(source.stop.getLine(), source.stop.getCharPositionInLine() + source.stop.getText().length());
        else this.stop = null;
    }

    public int getColumn() {
        return source.start.getCharPositionInLine() + 1;
    }

    public int getLine() {
        return source.start.getLine();
    }

    public String getMessage() {
        return message;
    }

    public Integer[][] getRange() {
        final Integer[][] result = new Integer[][]{new Integer[2], new Integer[2]};
        final Pair<Integer, Integer> stop = getStop();
        result[0][0] = getLine();
        result[0][1] = getColumn();
        result[1][0] = stop.a;
        result[1][1] = stop.b;
        return result;
    }

    public ParserRuleContext getSource() {
        return source;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public Pair<Integer, Integer> getStop() {
        return stop;
    }

    public String getType() {
        return type;
    }

    @Override
    public Map<String, Object> toJson() {
        final Map<String, Object> result = new HashMap<>();
        result.put("column", getColumn());
        result.put("line", getLine());
        result.put("message", getMessage());
        result.put("range", getRange());
        result.put("filePath", getSourceFile());
        result.put("type", getType());
        return result;
    }
}
