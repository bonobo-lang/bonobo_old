package thosakwe.bonobo;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import thosakwe.bonobo.analysis.SyntaxErrorListener;
import thosakwe.bonobo.grammar.BonoboLexer;
import thosakwe.bonobo.grammar.BonoboParser;

import java.io.IOException;

public class Bonobo {
    public static String VERSION = "1.0.0-SNAPSHOT";

    private static ErrorAwareBonoboParser parse(ANTLRInputStream inputStream) {
        SyntaxErrorListener errorListener = new SyntaxErrorListener();
        BonoboLexer lexer = new BonoboLexer(inputStream);
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        return new ErrorAwareBonoboParser(tokenStream, errorListener);
    }

    public static ErrorAwareBonoboParser parseFile(String filename) throws IOException {
        return parse(new ANTLRFileStream(filename));
    }

    public static ErrorAwareBonoboParser parseText(String text) {
        return parse(new ANTLRInputStream(text));
    }
}
