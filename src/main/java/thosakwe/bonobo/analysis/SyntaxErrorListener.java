package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Pair;
import thosakwe.bonobo.analysis.CompilerError;

import java.util.ArrayList;
import java.util.List;

public class SyntaxErrorListener extends BaseErrorListener {
    private final List<CompilerError> errors = new ArrayList<>();
    private final String sourceFile;

    public SyntaxErrorListener(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public List<CompilerError> getErrors() {
        return errors;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        if (offendingSymbol instanceof Token) {
            errors.add(new CompilerError(CompilerError.ERROR, msg, null, sourceFile) {
                @Override
                public int getColumn() {
                    return charPositionInLine + 1;
                }

                @Override
                public int getLine() {
                    return line;
                }

                @Override
                public Pair<Integer, Integer> getStop() {
                    final Token token = (Token) offendingSymbol;
                    return new Pair<>(token.getLine(), token.getCharPositionInLine() + token.getText().length());
                }
            });
        } else if(offendingSymbol != null) {
            System.out.printf("OFF SYMBOL IS A %s; MSG: %s%n", offendingSymbol.getClass().getName(), msg);
        }
    }
}
