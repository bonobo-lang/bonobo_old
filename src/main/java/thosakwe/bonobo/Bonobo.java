package thosakwe.bonobo;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Pair;
import thosakwe.bonobo.grammar.BonoboLexer;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.analysis.SyntaxErrorListener;

import java.io.IOException;

public class Bonobo {
    public static final String VERSION = "1.0.0-SNAPSHOT";

    public static Pair<BonoboParser, SyntaxErrorListener> parseFile(String filename) throws IOException {
        final ANTLRInputStream inputStream = new ANTLRFileStream(filename);
        final BonoboLexer lexer = new BonoboLexer(inputStream);
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        final BonoboParser parser = new BonoboParser(tokenStream);
        final SyntaxErrorListener listener = new SyntaxErrorListener(filename);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);
        return new Pair<>(parser, listener);
    }
}
