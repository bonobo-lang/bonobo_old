package thosakwe.bonobo.language;

import org.antlr.v4.runtime.ParserRuleContext;
import org.objectweb.asm.ClassWriter;
import thosakwe.bonobo.analysis.ErrorChecker;
import thosakwe.bonobo.analysis.StaticAnalyzer;

import java.util.List;

public abstract class BonoboObject {
    public abstract BonoboType getType();

    public abstract ParserRuleContext getSource();

    public abstract Object compile(ClassWriter clazz, StaticAnalyzer analyzer, ErrorChecker errorChecker, BonoboLibrary library, List<Exception> errors, boolean debug);
}
