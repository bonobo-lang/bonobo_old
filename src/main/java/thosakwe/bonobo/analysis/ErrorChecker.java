package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Pair;
import thosakwe.bonobo.Bonobo;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.BonoboType;
import thosakwe.bonobo.language.objects.BonoboFunction;
import thosakwe.bonobo.language.objects.BonoboFunctionParameter;
import thosakwe.bonobo.language.objects.BonoboObjectImpl;
import thosakwe.bonobo.language.types.BonoboListType;
import thosakwe.bonobo.language.types.BonoboUnknownType;
import thosakwe.bonobo.language.types.BonoboVoidType;

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

    public List<BonoboException> visitTopLevelFunction(BonoboFunction function, BonoboParser.TopLevelFuncDefContext source) throws BonoboException {
        List<BonoboException> result = new ArrayList<>();

        // Push scope and load parameters
        analyzer.pushScope(source);

        for (BonoboFunctionParameter parameter : function.getParameters()) {
            analyzer.getScope().putFinal(parameter.getName(), new BonoboObjectImpl(parameter.getType(), parameter.getSource()));
        }

        // Ensure function returns the right type
        BonoboParser.FuncBodyContext bodyContext = source.funcBody();

        if (bodyContext instanceof BonoboParser.StmtBodyContext) {
            Pair<BonoboType, List<BonoboException>> stmtResult = visitStatement(((BonoboParser.StmtBodyContext) bodyContext).stmt(), function);
            result.addAll(stmtResult.b);
            BonoboType actuallyReturned = stmtResult.a;

            if (!actuallyReturned.isAssignableTo(function.getReturnType())) {
                result.add(BonoboException.invalidReturnForFunction(function, actuallyReturned, source.funcSignature()));
            }
        } else if (bodyContext instanceof BonoboParser.BlockBodyContext) {
            // Walk through every statement, checking for typing errors
            List<BonoboException> walked = visitBlock(((BonoboParser.BlockBodyContext) bodyContext).block(), function);

            if (walked != null)
                result.addAll(walked);
        }

        // Pop scope
        analyzer.popScope();
        return result;
    }

    public List<BonoboException> visitBlock(BonoboParser.BlockContext block, BonoboFunction function) throws BonoboException {
        List<BonoboException> result = new ArrayList<>();

        for (BonoboParser.StmtContext stmtContext : block.stmt()) {
            Pair<BonoboType, List<BonoboException>> stmtResult = visitStatement(stmtContext, function);
            result.addAll(stmtResult.b);

            if (stmtContext instanceof BonoboParser.ReturnStmtContext) {
                BonoboType actuallyReturned = stmtResult.a;

                if (!actuallyReturned.isAssignableTo(function.getReturnType())) {
                    ParserRuleContext source = function.getSource();

                    if (source instanceof BonoboParser.TopLevelFuncDefContext)
                        source = ((BonoboParser.TopLevelFuncDefContext) source).funcSignature();

                    result.add(BonoboException.invalidReturnForFunction(function, actuallyReturned, source));
                }
            }
        }

        return result;
    }

    public Pair<BonoboType, List<BonoboException>> visitStatement(BonoboParser.StmtContext ctx, BonoboFunction function) throws BonoboException {
        List<BonoboException> errors = new ArrayList<>();

        try {
            if (ctx instanceof BonoboParser.ForEachStmtContext) {
                String name = ((BonoboParser.ForEachStmtContext) ctx).name.getText();
                BonoboObject iterable = analyzer.analyzeExpression(((BonoboParser.ForEachStmtContext) ctx).expr());
                analyzer.pushScope(ctx);

                if (!iterable.getType().isAssignableTo(BonoboListType.TYPEOF)) {
                    errors.add(new BonoboException(
                            String.format("Type \"%s\" is not iterable.", iterable.getType().getName()),
                            ((BonoboParser.ForEachStmtContext) ctx).expr()));
                    analyzer.getScope().putFinal(name, new BonoboObjectImpl(BonoboUnknownType.INSTANCE, ((BonoboParser.ForEachStmtContext) ctx).expr()));
                } else {
                    BonoboListType listType = (BonoboListType) iterable.getType();
                    analyzer.getScope().putFinal(name, new BonoboObjectImpl(listType.getReferenceType(), ((BonoboParser.ForEachStmtContext) ctx).expr()));
                }

                // Visit the block
                errors.addAll(visitBlock(((BonoboParser.ForEachStmtContext) ctx).block(), function));

                analyzer.popScope();
            }

            if (ctx instanceof BonoboParser.HelperFuncStmtContext) {
                return new Pair<>(BonoboVoidType.INSTANCE, errors);
            }

            if (ctx instanceof BonoboParser.VarDeclStmtContext) {
                boolean isFinal = ((BonoboParser.VarDeclStmtContext) ctx).specifier.getText().equals("let");

                for (BonoboParser.VariableDeclarationContext decl : ((BonoboParser.VarDeclStmtContext) ctx).variableDeclaration()) {
                    String name = decl.name.getText();

                    try {
                        BonoboObject value = analyzer.analyzeExpression(decl.expr());
                        analyzer.getScope().put(name, value, isFinal);
                    } catch (BonoboException e) {
                        errors.add(e);
                        analyzer.getScope().put(name, new BonoboObjectImpl(BonoboUnknownType.INSTANCE, decl), isFinal);
                    }
                }
            }

            if (ctx instanceof BonoboParser.ReturnStmtContext) {
                return new Pair<>(analyzer.analyzeExpression(((BonoboParser.ReturnStmtContext) ctx).expr()).getType(), errors);
            }
        } catch (BonoboException e) {
            errors.add(e);
        }

        return new Pair<>(BonoboUnknownType.INSTANCE, errors);
    }
}
