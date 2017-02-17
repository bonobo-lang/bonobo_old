package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.ParserRuleContext;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.*;
import thosakwe.bonobo.language.objects.BonoboFunction;
import thosakwe.bonobo.language.objects.BonoboFunctionParameter;
import thosakwe.bonobo.language.objects.BonoboObjectImpl;
import thosakwe.bonobo.language.types.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticAnalyzer extends BaseStaticAnalyzer {
    public StaticAnalyzer(boolean debug, BonoboParser.CompilationUnitContext source) {
        super(debug, source);
    }

    public BonoboLibrary analyzeCompilationUnit(BonoboParser.CompilationUnitContext ctx) throws BonoboException {
        BonoboLibrary library = new BonoboLibrary(ctx);

        for (BonoboParser.TopLevelDefContext topLevelDefContext : ctx.topLevelDef()) {
            if (topLevelDefContext instanceof BonoboParser.TopLevelFuncDefContext) {
                BonoboFunction function = analyzeTopLevelFunctionDefinition((BonoboParser.TopLevelFuncDefContext) topLevelDefContext);
                getScope().putFinal(function.getName(), function);

                if (function.getName() != null && !function.getName().startsWith("_"))
                    library.addExport(function.getName(), function, function.getSource());
            } else if (topLevelDefContext instanceof BonoboParser.ConstDefContext) {
                Map<String, BonoboObject> variables = analyzeConstantDefinition((BonoboParser.ConstDefContext) topLevelDefContext);

                for (String name : variables.keySet()) {
                    BonoboObject value = variables.get(name);
                    getScope().putFinal(name, value);

                    if (!name.startsWith("_"))
                        library.addExport(name, value, value.getSource());
                }
            }
        }

        return library;
    }


    public Map<String, BonoboObject> analyzeConstantDefinition(BonoboParser.ConstDefContext ctx) throws BonoboException {
        Map<String, BonoboObject> result = new HashMap<>();

        for (BonoboParser.VariableDeclarationContext declarationContext : ctx.variableDeclaration()) {
            String name = declarationContext.name.getText();
            BonoboObject value = analyzeExpression(declarationContext.expr());
            result.put(name, value);
        }

        return result;
    }

    public BonoboFunction analyzeTopLevelFunctionDefinition(BonoboParser.TopLevelFuncDefContext ctx) throws BonoboException {
        BonoboFunction function = new BonoboFunction(ctx);
        BonoboParser.FuncSignatureContext signature = ctx.funcSignature();

        // Get name
        if (signature != null) {
            function.setName(signature.name.getText());

            // Add params
            for (BonoboParser.ParamSpecContext paramSpec : signature.params) {
                function.getParameters().add(analyzeParameterSpecification(paramSpec));
            }

            // Get return type if specified
            if (signature.returnType != null) {
                function.setReturnType(analyzeType(signature.returnType));
            }
        }

        // TODO: If not explicitly specified, try to infer a return type

        return function;
    }

    public BonoboFunctionParameter analyzeParameterSpecification(BonoboParser.ParamSpecContext ctx) throws BonoboException {
        String name = null;
        BonoboType type = null;

        if (ctx instanceof BonoboParser.SimpleParamSpecContext) {
            // TODO: Infer type of simple; for now assume int
            BonoboParser.SimpleParamSpecContext context = (BonoboParser.SimpleParamSpecContext) ctx;
            name = context.ID().getText();
            type = BonoboUnknownType.INSTANCE;
        } else if (ctx instanceof BonoboParser.TypedParamSpecContext) {
            BonoboParser.TypedParamSpecContext context = (BonoboParser.TypedParamSpecContext) ctx;
            name = context.ID().getText();
            type = analyzeType(context.type());
        } else if (ctx instanceof BonoboParser.FunctionParamSpecContext) {
            // TODO: Build type for funcSignature
            BonoboParser.FunctionParamSpecContext context = (BonoboParser.FunctionParamSpecContext) ctx;
            name = context.funcSignature().name.getText();
            type = BonoboUnknownType.INSTANCE;
        }

        return new BonoboFunctionParameter(name, type, ctx);
    }

    public BonoboType analyzeType(BonoboParser.TypeContext ctx) throws BonoboException {
        if (ctx instanceof BonoboParser.NamedTypeContext) {
            String name = ((BonoboParser.NamedTypeContext) ctx).ID().getText();

            switch (name) {
                case "num":
                    return BonoboNumberType.INSTANCE;
                case "int":
                    return BonoboIntegerType.INSTANCE;
                case "double":
                    return BonoboDoubleType.INSTANCE;
                case "string":
                    return BonoboStringType.INSTANCE;
                default:
                    // TODO: Resolve variable types
                    return BonoboUnknownType.INSTANCE;
            }
        } else if (ctx instanceof BonoboParser.ListTypeContext) {
            return new BonoboListType(analyzeType(((BonoboParser.ListTypeContext) ctx).type()));
        }

        return BonoboUnknownType.INSTANCE;
    }

    public BonoboObject analyzeExpression(BonoboParser.ExprContext ctx) throws BonoboException {
        if (ctx instanceof BonoboParser.IdentifierExprContext) {
            String name = ((BonoboParser.IdentifierExprContext) ctx).ID().getText();
            Symbol resolved = getScope().getSymbol(name);

            if (resolved == null || resolved.getValue() == null)
                throw BonoboException.unresolvedIdentifier(name, ctx);
            return resolved.getValue();
        }

        if (ctx instanceof BonoboParser.IntegerLiteralExprContext) {
            return new BonoboObjectImpl(BonoboIntegerType.INSTANCE, ctx);
        }

        if (ctx instanceof BonoboParser.DoubleLiteralExprContext) {
            return new BonoboObjectImpl(BonoboDoubleType.INSTANCE, ctx);
        }

        if (ctx instanceof BonoboParser.StringLiteralExprContext) {
            return new BonoboObjectImpl(BonoboStringType.INSTANCE, ctx);
        }

        if (ctx instanceof BonoboParser.MemberExprContext) {
            BonoboParser.MemberExprContext memberExprContext = (BonoboParser.MemberExprContext) ctx;
            BonoboObject target = analyzeExpression(memberExprContext.expr());
            String name = memberExprContext.ID().getText();

            for (BonoboClassMember member : target.getType().getMembers()) {
                if (member.getName().equals(name)) {
                    return new BonoboObjectImpl(member.getValue(ctx), ctx);
                }
            }

            throw BonoboException.unresolvedGetter(target.getType(), name, ctx);
        }

        if (ctx instanceof BonoboParser.TypeCastExprContext) {
            BonoboParser.TypeCastExprContext typeCastExprContext = (BonoboParser.TypeCastExprContext) ctx;
            BonoboObject object = analyzeExpression(typeCastExprContext.expr());
            BonoboType type = analyzeType(typeCastExprContext.type());

            if (!object.getType().isAssignableTo(type) && !type.isAssignableTo(object.getType()))
                throw BonoboException.cannotCast(object.getType(), type, ctx);
            return new BonoboObjectImpl(type, ctx);
        }

        if (ctx instanceof BonoboParser.IndexerExprContext) {
            BonoboParser.IndexerExprContext indexerExprContext = (BonoboParser.IndexerExprContext) ctx;
            BonoboObject target = analyzeExpression(indexerExprContext.target);
            BonoboObject index = analyzeExpression(indexerExprContext.index);
            return new BonoboObjectImpl(target.getType().typeForGetIndex(index.getType(), ctx), ctx);
        }

        if (ctx instanceof BonoboParser.RangeLiteralExprContext) {
            BonoboParser.RangeLiteralExprContext rangeLiteralExprContext = (BonoboParser.RangeLiteralExprContext) ctx;
            BonoboObject lower = analyzeExpression(rangeLiteralExprContext.lower), upper = analyzeExpression(rangeLiteralExprContext.upper);
            BonoboType commonType = lower.getType().getCommonParentType(upper.getType());

            if (commonType == null)
                throw BonoboException.noCommonTypeFor("list literal", ctx);
            return new BonoboObjectImpl(new BonoboListType(commonType), ctx);
        }

        if (ctx instanceof BonoboParser.EmptyListLiteralExprContext) {
            return new BonoboObjectImpl(new BonoboListType(analyzeType(((BonoboParser.EmptyListLiteralExprContext) ctx).type())), ctx);
        }

        if (ctx instanceof BonoboParser.ListLiteralExprContext) {
            BonoboType commonParentType = null;

            for (BonoboParser.ExprContext exprContext : ((BonoboParser.ListLiteralExprContext) ctx).expr()) {
                BonoboType type = analyzeExpression(exprContext).getType();

                if (commonParentType == null)
                    commonParentType = type;
                else {
                    BonoboType newCommon = commonParentType.getCommonParentType(type);

                    if (newCommon == null) {
                        throw BonoboException.noCommonTypeFor("list literal", ctx);
                    } else if (commonParentType != newCommon) {
                        commonParentType = newCommon;
                    }
                }
            }

            return new BonoboObjectImpl(new BonoboListType(commonParentType), ctx);
        }

        if (ctx instanceof BonoboParser.InvocationExprContext) {
            BonoboParser.InvocationExprContext invocationExprContext = (BonoboParser.InvocationExprContext) ctx;
            BonoboObject callee = analyzeExpression(((BonoboParser.InvocationExprContext) ctx).callee);
            List<BonoboType> args = new ArrayList<>();

            for (BonoboParser.ExprContext arg : invocationExprContext.args) {
                args.add(analyzeExpression(arg).getType());
            }

            return new BonoboObjectImpl(callee.getType().typeForInvoke(args, ctx), ctx);
        }

        if (ctx instanceof BonoboParser.AdjacentExprsContext) {
            BonoboParser.AdjacentExprsContext binary = (BonoboParser.AdjacentExprsContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);
            return new BonoboObjectImpl(left.getType().typeForMultiply(right.getType(), ctx), ctx);
        }

        if (ctx instanceof BonoboParser.AssignmentExprContext) {
            // TODO: Assignment operators should all function differently!!!
            BonoboParser.AssignmentExprContext assignmentExprContext = (BonoboParser.AssignmentExprContext) ctx;

            if (assignmentExprContext.left instanceof BonoboParser.IdentifierExprContext) {
                String name = ((BonoboParser.IdentifierExprContext) assignmentExprContext.left).ID().getText();
                Symbol resolved = getScope().getSymbol(name);

                if (resolved == null)
                    throw BonoboException.unresolvedIdentifier(name, assignmentExprContext.left);
                resolved.setValue(analyzeExpression(assignmentExprContext.right), ctx);
                return resolved.getValue();
            } else if (assignmentExprContext.left instanceof BonoboParser.MemberExprContext) {
                BonoboParser.MemberExprContext memberExprContext = (BonoboParser.MemberExprContext) assignmentExprContext.left;
                BonoboObject target = analyzeExpression(memberExprContext.expr());
                String name = memberExprContext.ID().getText();
                BonoboObject right = analyzeExpression(assignmentExprContext.right);

                for (BonoboClassMember member : target.getType().getMembers()) {
                    if (member.getName().equals(name)) {
                        return new BonoboObjectImpl(member.setValue(right.getType(), assignmentExprContext.right), ctx);
                    }
                }

                throw BonoboException.unresolvedSetter(target.getType(), name, assignmentExprContext.left);
            } else if (assignmentExprContext.left instanceof BonoboParser.IndexerExprContext) {
                BonoboParser.IndexerExprContext indexerExprContext = (BonoboParser.IndexerExprContext) assignmentExprContext.left;
                BonoboObject target = analyzeExpression(indexerExprContext.target),
                        index = analyzeExpression(indexerExprContext.index),
                        right = analyzeExpression(assignmentExprContext.right);
                return new BonoboObjectImpl(target.getType().typeForSetIndex(index.getType(), right.getType(), ctx), ctx);
            } else {
                BonoboObject left = analyzeExpression(assignmentExprContext.left);
                throw new BonoboException(String.format("Cannot assign to %s.", left.getType().getName()), ctx);
            }
        }

        if (ctx instanceof BonoboParser.PowerExprContext) {
            BonoboParser.PowerExprContext binary = (BonoboParser.PowerExprContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);
            return new BonoboObjectImpl(left.getType().typeForPow(right.getType(), ctx), ctx);
        }

        if (ctx instanceof BonoboParser.MultiplicationExprContext) {
            BonoboParser.MultiplicationExprContext binary = (BonoboParser.MultiplicationExprContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);
            return new BonoboObjectImpl(left.getType().typeForMultiply(right.getType(), ctx), ctx);
        }

        if (ctx instanceof BonoboParser.DivisionExprContext) {
            BonoboParser.DivisionExprContext binary = (BonoboParser.DivisionExprContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);
            return new BonoboObjectImpl(left.getType().typeForDivide(right.getType(), ctx), ctx);
        }

        if (ctx instanceof BonoboParser.AdditionExprContext) {
            BonoboParser.AdditionExprContext binary = (BonoboParser.AdditionExprContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);
            return new BonoboObjectImpl(left.getType().typeForAdd(right.getType(), ctx), ctx);
        }

        if (ctx instanceof BonoboParser.SubtractionExprContext) {
            BonoboParser.SubtractionExprContext binary = (BonoboParser.SubtractionExprContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);
            return new BonoboObjectImpl(left.getType().typeForSubtract(right.getType(), ctx), ctx);
        }

        if (ctx instanceof BonoboParser.ModuloExprContext) {
            BonoboParser.ModuloExprContext binary = (BonoboParser.ModuloExprContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);
            return new BonoboObjectImpl(left.getType().typeForModulo(right.getType(), ctx), ctx);
        }

        if (ctx instanceof BonoboParser.PlusMinusExprContext) {
            BonoboParser.PlusMinusExprContext binary = (BonoboParser.PlusMinusExprContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);
            BonoboType plusType = left.getType().typeForAdd(right.getType(), ctx),
                    minusType = left.getType().typeForAdd(right.getType(), ctx);
            BonoboType commonParentType = plusType.getCommonParentType(minusType);

            if (commonParentType == null)
                throw BonoboException.noCommonTypeFor("plus/minus expression", ctx);
            return new BonoboObjectImpl(new BonoboListType(commonParentType), ctx);
        }

        if (ctx instanceof BonoboParser.EqualsExprContext) {
            BonoboParser.EqualsExprContext binary = (BonoboParser.EqualsExprContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);
            BonoboType commonParentType = left.getType().getCommonParentType(right.getType());

            if (commonParentType == null)
                throw BonoboException.incomparableTypes(left.getType(), right.getType(), ctx);
            return new BonoboObjectImpl(BonoboBooleanType.INSTANCE, ctx);
        }

        if (ctx instanceof BonoboParser.NotEqualsExprContext) {
            BonoboParser.NotEqualsExprContext binary = (BonoboParser.NotEqualsExprContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);
            BonoboType commonParentType = left.getType().getCommonParentType(right.getType());

            if (commonParentType == null)
                throw BonoboException.incomparableTypes(left.getType(), right.getType(), ctx);
            return new BonoboObjectImpl(BonoboBooleanType.INSTANCE, ctx);
        }

        if (ctx instanceof BonoboParser.AndExprContext) {
            BonoboParser.AndExprContext binary = (BonoboParser.AndExprContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);

            if (!(left.getType().isAssignableTo(BonoboBooleanType.INSTANCE)
                    && right.getType().isAssignableTo(BonoboBooleanType.INSTANCE)))
                throw BonoboException.logicalComparison(ctx);
            return new BonoboObjectImpl(BonoboBooleanType.INSTANCE, ctx);
        }

        if (ctx instanceof BonoboParser.OrExprContext) {
            BonoboParser.OrExprContext binary = (BonoboParser.OrExprContext) ctx;
            BonoboObject left = analyzeExpression(binary.left), right = analyzeExpression(binary.right);

            if (!(left.getType().isAssignableTo(BonoboBooleanType.INSTANCE)
                    && right.getType().isAssignableTo(BonoboBooleanType.INSTANCE)))
                throw BonoboException.logicalComparison(ctx);
            return new BonoboObjectImpl(BonoboBooleanType.INSTANCE, ctx);
        }

        if (ctx instanceof BonoboParser.ParenthesizedExprContext) {
            return analyzeExpression(((BonoboParser.ParenthesizedExprContext) ctx).expr());
        }

        // TODO: All expressions
        return new BonoboObjectImpl(BonoboUnknownType.INSTANCE, ctx);
    }

    public void analyzeContext(ParserRuleContext ctx) {

    }
}
