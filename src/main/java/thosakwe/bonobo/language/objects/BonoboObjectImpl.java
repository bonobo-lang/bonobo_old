package thosakwe.bonobo.language.objects;

import org.antlr.v4.runtime.ParserRuleContext;
import org.objectweb.asm.ClassWriter;
import thosakwe.bonobo.analysis.ErrorChecker;
import thosakwe.bonobo.analysis.StaticAnalyzer;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.BonoboType;

import java.util.List;

public class BonoboObjectImpl extends BonoboObject {
    private final BonoboType type;
    private final ParserRuleContext source;

    public BonoboObjectImpl(BonoboType type, ParserRuleContext source) {
        this.type = type;
        this.source = source;
    }

    @Override
    public BonoboType getType() {
        return type;
    }

    @Override
    public ParserRuleContext getSource() {
        return source;
    }

    @Override
    public Object compile(ClassWriter clazz, StaticAnalyzer analyzer, ErrorChecker errorChecker, BonoboLibrary library, List<Exception> errors, boolean debug) {
        // TODO: Compile normal objects
        throw new UnsupportedOperationException("TODO: Compile normal objects");
    }
}
