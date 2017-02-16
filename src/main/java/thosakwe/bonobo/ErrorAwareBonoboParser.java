package thosakwe.bonobo;

import org.antlr.v4.runtime.TokenStream;
import thosakwe.bonobo.analysis.SyntaxErrorListener;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;

import java.util.List;

public class ErrorAwareBonoboParser extends BonoboParser {
    private final SyntaxErrorListener errorListener;

    public ErrorAwareBonoboParser(TokenStream input, SyntaxErrorListener errorListener) {
        super(input);
        removeErrorListeners();
        addErrorListener(this.errorListener = errorListener);
    }

    public List<BonoboException> getErrors() {
        return errorListener.getErrors();
    }
}
