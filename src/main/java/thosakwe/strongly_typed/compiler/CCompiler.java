package thosakwe.strongly_typed.compiler;

import thosakwe.strongly_typed.analysis.Scope;
import thosakwe.strongly_typed.analysis.ScopeEventListener;
import thosakwe.strongly_typed.analysis.Symbol;
import thosakwe.strongly_typed.compiler.codegen.c.*;
import thosakwe.strongly_typed.grammar.StronglyTypedParser;
import thosakwe.strongly_typed.lang.STDatum;
import thosakwe.strongly_typed.lang.STNumber;
import thosakwe.strongly_typed.lang.STTupleType;
import thosakwe.strongly_typed.lang.STType;
import thosakwe.strongly_typed.lang.errors.CompilerError;

import java.util.ArrayList;
import java.util.List;

public class CCompiler extends STCompiler<CAstNode> {
    private final CodeBuilder builder = new CodeBuilder();
    private final VirtualHeap heap = new VirtualHeap();
    private final String sourceFile;
    private final boolean debug;

    public CCompiler(String sourceFile, boolean debug) {
        this.sourceFile = sourceFile;
        this.debug = debug;
    }

    private STNumber binaryOnConstantNumbers(Number left, Number right, String op) {
        // Todo: All operators
        printDebug(String.format("Compiling binary expression into constant number: %s %s %s...", left, op, right));
        if (op.equals("^")) return new STNumber(Math.pow(left.doubleValue(), right.doubleValue()));
        else if (op.equals("+")) return new STNumber(left.doubleValue() + right.doubleValue());
        else if (op.equals("*")) return new STNumber(left.doubleValue() * right.doubleValue());

        return null;
    }

    @Override
    public String compile(StronglyTypedParser.CompilationUnitContext ast) throws CompilerError {
        final ScopeEventListener listener = createEventListener();
        symbolTable.addEventListener(listener);
        final CProgram program = visitCompilationUnit(ast);
        program.apply(builder, symbolTable);
        symbolTable.removeEventListener(listener);
        return builder.toString();
    }

    private ScopeEventListener createEventListener() {
        return new ScopeEventListener() {
            @Override
            public void onDestroy(Scope scope) {
                // Leaving a scope means its symbols
                // can not be referenced outside of its
                // context.
                //
                // This means that we can know exactly when
                // to GC.
                //
                // Todo: Use the `heap` to reuse free memory

                for (Symbol symbol : scope.getSymbols()) {
                    if (symbol.getValue().isPointer()) {
                        builder.println(String.format("free(%s);", symbol.getName()));
                        builder.println(String.format("%s = NULL;", symbol.getName()));
                    } else {
                        builder.println(String.format("free(&%s);", symbol.getName()));
                    }
                }
            }
        };
    }

    private STType inferBinaryReturnType(StronglyTypedParser.ExprContext leftExpr, StronglyTypedParser.ExprContext rightExpr, String op) {
        final STDatum left = resolveExpr(leftExpr);
        final STDatum right = resolveExpr(rightExpr);
        printDebug(String.format("Inferring binary: '%s %s %s'...", leftExpr.getText(), op, rightExpr.getText()));
        printDebug(String.format("Left is a %s, right is a %s", left.getType().toCType(), right.getType().toCType()));

        if (!(left.getType() instanceof STTupleType) && (right.getType() instanceof STTupleType)) {
            // Operate with tuples on left for the sake of simplicity.
            return inferBinaryReturnType(rightExpr, leftExpr, op);
        }

        if (left.getType() instanceof STTupleType) {

        } else {
            // Todo: Is this too naive?
            return right.getType();
        }

        return null;
    }

    private STType inferReturnType(StronglyTypedParser.ExprContext expr) {
        if (expr instanceof StronglyTypedParser.AdditiveExprContext)
            return inferBinaryReturnType(((StronglyTypedParser.AdditiveExprContext) expr).left, ((StronglyTypedParser.AdditiveExprContext) expr).right, "+");

        if (expr instanceof StronglyTypedParser.IdentifierExprContext) {
            final String name = ((StronglyTypedParser.IdentifierExprContext) expr).ID().getText();
            final Symbol resolved = symbolTable.getSymbol(name);

            if (resolved == null) {
                errors.add(new CompilerError(
                        CompilerError.ERROR,
                        String.format("Cannot resolve symbol '%s'.", name),
                        expr,
                        sourceFile));
                return null;
            }

            return resolved.getValue().getType();
        }

        if (expr instanceof StronglyTypedParser.IntegerLiteralExprContext) return STType.INT32;

        if (expr instanceof StronglyTypedParser.MultiplicativeExprContext)
            return inferBinaryReturnType(((StronglyTypedParser.MultiplicativeExprContext) expr).left, ((StronglyTypedParser.MultiplicativeExprContext) expr).right, "*");

        if (expr instanceof StronglyTypedParser.StringExprContext) return STType.STRING;

        return null;
    }

    // Todo: Throw errors in analyzer
    private STType inferReturnType(StronglyTypedParser.FuncBodyContext ctx) {
        if (ctx instanceof StronglyTypedParser.BlockBodyContext) {
            final List<STType> types = new ArrayList<>();

            for (StronglyTypedParser.StmtContext stmt : ((StronglyTypedParser.BlockBodyContext) ctx).block().stmt()) {
                final STType returnType = inferReturnType(stmt);

                if (returnType != null) {
                    types.add(returnType);
                }
            }

            if (!types.isEmpty()) {
                final STType returnType = types.get(0);

                for (int i = 1; i < types.size(); i++) {
                    if (!types.get(i).equals(returnType)) {
                        errors.add(new CompilerError(
                                CompilerError.ERROR,
                                "All paths must return a value of the same type.",
                                ctx,
                                sourceFile));

                        return null;
                    }
                }

                return returnType;
            }
        } else if (ctx instanceof StronglyTypedParser.StmtBodyContext) {
            final StronglyTypedParser.StmtContext stmt = ((StronglyTypedParser.StmtBodyContext) ctx).stmt();

            if (stmt instanceof StronglyTypedParser.ReturnStmtContext)
                return inferReturnType(stmt);

            else if (stmt instanceof StronglyTypedParser.HelperFuncStmtContext) {
                return STType.INT32;
            } else {
                errors.add(new CompilerError(
                        CompilerError.ERROR,
                        "All functions must return a value",
                        stmt,
                        sourceFile));
                return null;
            }
        } else if (ctx instanceof StronglyTypedParser.ExprBodyContext) {
            return inferReturnType(((StronglyTypedParser.ExprBodyContext) ctx).expr());
        }

        return null;
    }

    private STType inferReturnType(StronglyTypedParser.StmtContext ctx) {
        // Todo: All types

        if (ctx instanceof StronglyTypedParser.ReturnStmtContext) {
            return inferReturnType(((StronglyTypedParser.ReturnStmtContext) ctx).expr());
        }

        return null;
    }

    private void printDebug(String msg) {
        if (debug)
            System.out.println(msg);
    }

    private STDatum resolveBinary(StronglyTypedParser.ExprContext leftExpr, StronglyTypedParser.ExprContext rightExpr, String op) {
        final STDatum left = resolveExpr(leftExpr);
        final STDatum right = resolveExpr(rightExpr);
        printDebug(String.format("Compiling binary: '%s %s %s'...", leftExpr.getText(), op, rightExpr.getText()));
        printDebug(String.format("Left is a %s, right is a %s", left.getType().toCType(), right.getType().toCType()));

        if (!(left.getType() instanceof STTupleType) && (right.getType() instanceof STTupleType)) {
            // Operate with tuples on left for the sake of simplicity.
            return resolveBinary(rightExpr, leftExpr, op);
        }

        if (left.getType() instanceof STTupleType) {

        } else if (left instanceof STNumber) {
            // Todo: Caret Operators

            if (right instanceof STNumber) {
                // If both sides are constant, then we can add them
                // at compile-time. Hooray for optimization!
                return binaryOnConstantNumbers(((STNumber) left).getValue(), ((STNumber) right).getValue(), op);
            }
        }

        return null;
    }

    private STDatum resolveExpr(StronglyTypedParser.ExprContext expr, boolean throwError) {
        if (expr instanceof StronglyTypedParser.AdditiveExprContext)
            return resolveBinary(((StronglyTypedParser.AdditiveExprContext) expr).left, ((StronglyTypedParser.AdditiveExprContext) expr).right, "+");

        if (expr instanceof StronglyTypedParser.IntegerLiteralExprContext) {
            return new STNumber(Integer.parseInt(expr.getText()));
        }


        if (expr instanceof StronglyTypedParser.MultiplicativeExprContext)
            return resolveBinary(((StronglyTypedParser.MultiplicativeExprContext) expr).left, ((StronglyTypedParser.MultiplicativeExprContext) expr).right, "*");

        return null;
    }

    private STDatum resolveExpr(StronglyTypedParser.ExprContext ctx) {
        return resolveExpr(ctx, true);
    }

    @Override
    public CProgram visitCompilationUnit(StronglyTypedParser.CompilationUnitContext ctx) {
        final CProgram result = new CProgram();

        result.getImports().add("<stdio.h>");

        for (StronglyTypedParser.TopLevelDefContext def : ctx.topLevelDef()) {
            if (def instanceof StronglyTypedParser.TopLevelFuncDefContext) {
                result.getFunctionList().add(visitTopLevelFuncDef((StronglyTypedParser.TopLevelFuncDefContext) def));
            }
        }

        return result;
    }

    private CExpression visitExpr(StronglyTypedParser.ExprContext expr) {

        if (expr instanceof StronglyTypedParser.IdentifierExprContext) {
            // Todo: Resolve these
            return new CIdentifierExpression(((StronglyTypedParser.IdentifierExprContext) expr).ID().getText());
        }

        if (expr instanceof StronglyTypedParser.StringExprContext) {
            return new CStringExpression(expr.getText().replaceAll("(^')|('$)", ""));
        }

        final STDatum resolved = resolveExpr(expr);
        return resolved != null ? resolved.toCExpression() : null;
    }

    private CBlock visitFuncBody(StronglyTypedParser.FuncBodyContext ctx) {
        final CBlock result = new CBlock();

        if (ctx instanceof StronglyTypedParser.ExprBodyContext) {
            final STDatum value = resolveExpr(((StronglyTypedParser.ExprBodyContext) ctx).expr());

            if (value == null) {
                errors.add(
                        new CompilerError(CompilerError.ERROR,
                                "Function returns invalid value",
                                ((StronglyTypedParser.ExprBodyContext) ctx).expr(),
                                sourceFile));
                return null;
            }

            result.getStatements().add(new CReturnStatement(value.toCExpression()));
        } else if (ctx instanceof StronglyTypedParser.BlockBodyContext) {
            for (StronglyTypedParser.StmtContext stmt : ((StronglyTypedParser.BlockBodyContext) ctx).block().stmt()) {
                final CStatement cstmt = visitStmt(stmt);

                if (cstmt != null)
                    result.getStatements().add(cstmt);
            }
        } else if (ctx instanceof StronglyTypedParser.StmtBodyContext) {
            final CStatement cstmt = visitStmt(((StronglyTypedParser.StmtBodyContext) ctx).stmt());

            if (cstmt != null)
                result.getStatements().add(cstmt);
        }

        return result;
    }

    @Override
    public CStatement visitHelperFuncStmt(StronglyTypedParser.HelperFuncStmtContext ctx) {
        final String func = ctx.helper.getText();

        if (func.equals("print")) {
            if (ctx.args.size() != 1) {
                errors.add(new CompilerError(
                        CompilerError.WARNING,
                        "print expects to be called with exactly one argument.",
                        ctx,
                        sourceFile
                ));

                return null;
            } else {
                final STType type = inferReturnType(ctx.args.get(0));

                if (type == STType.STRING) {
                    final CStringExpression str = (CStringExpression) visitExpr(ctx.args.get(0));

                    if (str != null)
                        return new CExpressionStatement(new CInvocationExpression(
                                new CIdentifierExpression("printf"),
                                new CExpression[]{str.append("\\n")}));
                    else {
                        errors.add(new CompilerError(
                                CompilerError.ERROR,
                                String.format("Invalid string for print: '%s'.", ctx.args.get(0).getText()),
                                ctx.args.get(0),
                                sourceFile
                        ));

                        return null;
                    }
                }

                String formatSpecifier = null;

                if (type == STType.INT32)
                    formatSpecifier = "%d";

                return new CExpressionStatement(new CInvocationExpression(
                        new CIdentifierExpression("printf"),
                        new CExpression[]{
                                new CStringExpression(String.format("%s\\n", formatSpecifier)),
                                visitExpr(ctx.args.get(0))}));
            }
        }

        return null;
    }

    private CStatement visitStmt(StronglyTypedParser.StmtContext stmt) {
        if (stmt instanceof StronglyTypedParser.HelperFuncStmtContext) {
            return visitHelperFuncStmt((StronglyTypedParser.HelperFuncStmtContext) stmt);
        }

        if (stmt instanceof StronglyTypedParser.ReturnStmtContext) {
            return new CReturnStatement(visitExpr(((StronglyTypedParser.ReturnStmtContext) stmt).expr()));
        }

        return null;
    }

    @Override
    public CFunctionDeclaration visitTopLevelFuncDef(StronglyTypedParser.TopLevelFuncDefContext ctx) {
        final CFunctionDeclaration result = new CFunctionDeclaration();
        final STType returnType = inferReturnType(ctx.funcBody());
        final String name = ctx.funcSignature().name.getText();

        if (returnType == null) {
            errors.add(
                    new CompilerError(CompilerError.ERROR,
                            String.format("Cannot infer return type of function '%s'.", name),
                            ctx.funcSignature(),
                            sourceFile));
        } else {
            result.setName(name);
            result.setReturnType(returnType.toCType());
            result.setBlock(visitFuncBody(ctx.funcBody()));
            return result;
        }

        return null;
    }

}
