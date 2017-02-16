package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;
import org.eclipse.lsp4j.Position;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboType;
import thosakwe.bonobo.language.objects.BonoboFunction;
import thosakwe.bonobo.language.types.BonoboUnknownType;

import java.util.ArrayList;
import java.util.List;

public class PositionAwareErrorChecker extends ErrorChecker {
    private final Position currentPosition;

    public PositionAwareErrorChecker(StaticAnalyzer analyzer, Position currentPosition) {
        super(analyzer);
        this.currentPosition = currentPosition;
    }

    private boolean validatePosition(ParserRuleContext ctx) throws BonoboException {
        return CodeCompleter.nodeIsInRange(currentPosition, ctx);
    }

    @Override
    public List<BonoboException> visitTopLevelFunction(BonoboFunction function, BonoboParser.TopLevelFuncDefContext source) throws BonoboException {
        if (!validatePosition(source))
            return new ArrayList<>();
        return super.visitTopLevelFunction(function, source);
    }

    @Override
    public List<BonoboException> visitBlock(BonoboParser.BlockContext block, BonoboFunction function) throws BonoboException {
        if (!validatePosition(block))
            return new ArrayList<>();
        return super.visitBlock(block, function);
    }

    @Override
    public Pair<BonoboType, List<BonoboException>> visitStatement(BonoboParser.StmtContext ctx, BonoboFunction function) throws BonoboException {
        if (!validatePosition(ctx))
            return new Pair<>(BonoboUnknownType.INSTANCE, new ArrayList<>());
        return super.visitStatement(ctx, function);
    }
}
