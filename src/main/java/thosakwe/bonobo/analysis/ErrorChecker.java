package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.misc.Pair;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.BonoboType;
import thosakwe.bonobo.language.objects.BonoboFunction;
import thosakwe.bonobo.language.objects.BonoboFunctionParameter;
import thosakwe.bonobo.language.objects.BonoboObjectImpl;
import thosakwe.bonobo.language.types.BonoboUnknownType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ErrorChecker {
    private final StaticAnalyzer analyzer;

    public ErrorChecker(StaticAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public List<BonoboException> visitLibrary(BonoboLibrary library) throws BonoboException {
        List<BonoboException> result = new ArrayList<>();

        Map<String, BonoboObject> exports = library.getExports();

        for (String name : exports.keySet()) {
            BonoboObject value = exports.get(name);

            if (value instanceof BonoboFunction)
                result.addAll(visitFunction((BonoboFunction) value));
        }

        return result;
    }

    public List<BonoboException> visitFunction(BonoboFunction function) throws BonoboException {
        List<BonoboException> result = new ArrayList<>();

        if (function.getSource() instanceof BonoboParser.TopLevelFuncDefContext)
            result.addAll(visitTopLevelFunction(function, (BonoboParser.TopLevelFuncDefContext) function.getSource()));

        return result;
    }

    private List<BonoboException> visitTopLevelFunction(BonoboFunction function, BonoboParser.TopLevelFuncDefContext source) throws BonoboException {
        List<BonoboException> result = new ArrayList<>();

        // Push scope and load parameters
        analyzer.pushScope();

        for (BonoboFunctionParameter parameter : function.getParameters()) {
            analyzer.getScope().putFinal(parameter.getName(), new BonoboObjectImpl(parameter.getType(), parameter.getSource()));
        }

        // Ensure function returns the right type
        BonoboParser.FuncBodyContext bodyContext = source.funcBody();

        if (bodyContext instanceof BonoboParser.StmtBodyContext) {
            Pair<BonoboType, List<BonoboException>> stmtResult = visitStatement(((BonoboParser.StmtBodyContext) bodyContext).stmt());
            result.addAll(stmtResult.b);
            BonoboType actuallyReturned = stmtResult.a;

            if (!actuallyReturned.isAssignableTo(function.getReturnType())) {
                result.add(BonoboException.invalidReturnForFunction(function, actuallyReturned, source));
            }
        } else if (bodyContext instanceof BonoboParser.BlockBodyContext) {
            // Walk through every statement, checking for typing errors
        }

        // Pop scope
        analyzer.popScope();
        return result;
    }

    private Pair<BonoboType, List<BonoboException>> visitStatement(BonoboParser.StmtContext ctx) throws BonoboException {
        List<BonoboException> errors = new ArrayList<>();

        try {
            if (ctx instanceof BonoboParser.ReturnStmtContext) {
                return new Pair<>(analyzer.analyzeExpression(((BonoboParser.ReturnStmtContext) ctx).expr()).getType(), errors);
            }
        } catch (BonoboException e) {
            errors.add(e);
        }

        return new Pair<>(BonoboUnknownType.INSTANCE, errors);
    }
}
