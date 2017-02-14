package thosakwe.bonobo.compiler;

import thosakwe.bonobo.language.BonoboLibrary;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created on 2/14/2017.
 */
public abstract class BonoboCompiler {
    private final boolean debug;

    protected BonoboCompiler(boolean debug) {
        this.debug = debug;
    }

    public abstract void compile(BonoboLibrary library, OutputStream outputStream) throws IOException;

    protected void printDebug(String message) {
        if (debug)
            System.out.println(message);
    }

    public boolean isDebug() {
        return debug;
    }
}
