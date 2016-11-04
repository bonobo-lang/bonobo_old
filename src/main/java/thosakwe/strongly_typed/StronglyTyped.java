package thosakwe.strongly_typed;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.Pair;
import thosakwe.strongly_typed.grammar.StronglyTypedLexer;
import thosakwe.strongly_typed.grammar.StronglyTypedParser;
import thosakwe.strongly_typed.grammar.SyntaxErrorListener;

import java.io.IOException;

public class StronglyTyped {
    public static final String VERSION = "1.0.0-SNAPSHOT";

    public static Pair<StronglyTypedParser, SyntaxErrorListener> parseFile(String filename) throws IOException {
        final ANTLRInputStream inputStream = new ANTLRFileStream(filename);
        final StronglyTypedLexer lexer = new StronglyTypedLexer(inputStream);
        final CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        final StronglyTypedParser parser = new StronglyTypedParser(tokenStream);
        final SyntaxErrorListener listener = new SyntaxErrorListener(filename);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);
        return new Pair<>(parser, listener);
    }
}
