package thosakwe.strongly_typed.analysis;

import thosakwe.strongly_typed.grammar.StronglyTypedBaseVisitor;
import thosakwe.strongly_typed.grammar.StronglyTypedParser;
import thosakwe.strongly_typed.grammar.SyntaxErrorListener;
import thosakwe.strongly_typed.lang.errors.CompilerError;

import java.util.ArrayList;
import java.util.List;

public class StaticAnalyzer extends StronglyTypedBaseVisitor {
    private final List<CompilerError> errors = new ArrayList<>();
    private final List<CompilerError> warnings = new ArrayList<>();
    private final Scope symbolTable = new Scope();

    public StaticAnalyzer(SyntaxErrorListener listener, String sourceFile) {
        this.errors.addAll(listener.getErrors());
        this.symbolTable.sourceFile = sourceFile;
    }

    public List<CompilerError> getErrors() {
        return errors;
    }

    public List<CompilerError> getWarnings() {
        return warnings;
    }

    @Override
    public Object visitCompilationUnit(StronglyTypedParser.CompilationUnitContext ctx) {
        for (StronglyTypedParser.TopLevelDefContext def : ctx.topLevelDef()) {
            if (def instanceof StronglyTypedParser.TopLevelFuncDefContext) {
                warnings.add(new CompilerError(CompilerError.WARNING, def.getText(), def, symbolTable.sourceFile));
            }
        }
        return super.visitCompilationUnit(ctx);
    }
}
