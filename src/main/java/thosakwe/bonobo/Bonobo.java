package thosakwe.bonobo;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import thosakwe.bonobo.grammar.BonoboLexer;
import thosakwe.bonobo.grammar.BonoboParser;

import java.io.IOException;

public class Bonobo {
    public static String VERSION = "1.0.0-SNAPSHOT";

    public static BonoboParser parseFile(String filename) throws IOException {
        ANTLRInputStream inputStream = new ANTLRFileStream(filename);
        BonoboLexer lexer = new BonoboLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        return new BonoboParser(tokenStream);
    }
}
