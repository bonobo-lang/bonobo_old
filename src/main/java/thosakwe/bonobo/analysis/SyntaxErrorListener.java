package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.*;
import thosakwe.bonobo.language.BonoboException;

import java.util.ArrayList;
import java.util.List;

public class SyntaxErrorListener extends BaseErrorListener {
    private final List<BonoboException> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        final Token offending = e != null ? e.getOffendingToken() : ((offendingSymbol instanceof Token) ? ((Token) offendingSymbol) : null);

        if (offending == null)
            super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);

        else {
            errors.add(new BonoboException(msg, new ParserRuleContext() {
                @Override
                public Token getStart() {
                    return offending;
                }

                @Override
                public Token getStop() {
                    return offending;
                }

                @Override
                public String getText() {
                    return offending.getText();
                }
            }));
        }
    }

    public List<BonoboException> getErrors() {
        return errors;
    }
}
