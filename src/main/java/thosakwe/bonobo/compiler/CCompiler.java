package thosakwe.bonobo.compiler;

import thosakwe.bonobo.analysis.Scope;
import thosakwe.bonobo.analysis.ScopeEventListener;
import thosakwe.bonobo.analysis.Symbol;
import thosakwe.bonobo.compiler.codegen.c.*;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.lang.STDatum;
import thosakwe.bonobo.lang.STNumber;
import thosakwe.bonobo.lang.STTupleType;
import thosakwe.bonobo.lang.STType;

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

        switch (op) {
            case "^":
                return new STNumber(Math.pow(left.doubleValue(), right.doubleValue()));
            case "%":
                return new STNumber(left.doubleValue() % right.doubleValue());
            case "*":
                return new STNumber(left.doubleValue() * right.doubleValue());
            case "/":
                return new STNumber(left.doubleValue() / right.doubleValue());
            case "+":
                return new STNumber(left.doubleValue() + right.doubleValue());
            case "-":
                return new STNumber(left.doubleValue() - right.doubleValue());
        }

        return null;
    }

    @Override
    public String compile(BonoboParser.CompilationUnitContext ast) throws CompilerError {
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
                        // builder.println(String.format("free(&%s);", symbol.getName()));
                    }
                }
            }

            @Override
            public void onRelease(Scope scope, CBlock block) {
                if (debug)
                    block.add(new CComment(String.format("Releasing scope #%d...", scope.hashCode())));

                for (Symbol symbol : scope.getSymbols()) {
                    if (symbol.getValue().isPointer()) {
                        block.add(new CExpressionStatement(new CInvocationExpression(
                                new CIdentifierExpression("free"),
                                new CExpression[]{new CIdentifierExpression(symbol.getName())})));
                        block.add(new CExpressionStatement(
                                new CAssignmentExpression(
                                        new CIdentifierExpression(symbol.getName()),
                                        new CNullExpression(symbol.getValue().toCExpression().getSize()),
                                        "=")
                        ));
                        block.add(new CComment(String.format("Released '%s': %s", symbol.getName(), symbol.getValue().getClass().getSimpleName())));
                    }
                }
            }
        };
    }

    private STType inferBinaryReturnType(BonoboParser.ExprContext leftExpr, BonoboParser.ExprContext rightExpr, String op) {
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

    private STType inferReturnType(BonoboParser.ExprContext expr) {
        if (expr instanceof BonoboParser.AdditiveExprContext)
            return inferBinaryReturnType(((BonoboParser.AdditiveExprContext) expr).left, ((BonoboParser.AdditiveExprContext) expr).right, ((BonoboParser.AdditiveExprContext) expr).op.getText());

        if (expr instanceof BonoboParser.IdentifierExprContext) {
            final String name = ((BonoboParser.IdentifierExprContext) expr).ID().getText();
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

        if (expr instanceof BonoboParser.IntegerLiteralExprContext) return STType.INT32;

        if (expr instanceof BonoboParser.ModuloExprContext)
            return inferBinaryReturnType(((BonoboParser.ModuloExprContext) expr).left, ((BonoboParser.ModuloExprContext) expr).right, "%");

        if (expr instanceof BonoboParser.MultiplicativeExprContext)
            return inferBinaryReturnType(((BonoboParser.MultiplicativeExprContext) expr).left, ((BonoboParser.MultiplicativeExprContext) expr).right, ((BonoboParser.MultiplicativeExprContext) expr).op.getText());

        if (expr instanceof BonoboParser.PowerExprContext)
            return inferBinaryReturnType(((BonoboParser.PowerExprContext) expr).left, ((BonoboParser.PowerExprContext) expr).right, "^");

        if (expr instanceof BonoboParser.StringExprContext) return STType.STRING;

        return null;
    }

    // Todo: Throw errors in analyzer
    // Todo: Custom visitor to collect identifiers and types
    // class TypeInferencer extends...
    private STType inferReturnType(BonoboParser.FuncBodyContext ctx) {
        if (ctx instanceof BonoboParser.BlockBodyContext) {
            final List<STType> types = new ArrayList<>();

            for (BonoboParser.StmtContext stmt : ((BonoboParser.BlockBodyContext) ctx).block().stmt()) {
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
        } else if (ctx instanceof BonoboParser.StmtBodyContext) {
            final BonoboParser.StmtContext stmt = ((BonoboParser.StmtBodyContext) ctx).stmt();

            if (stmt instanceof BonoboParser.ReturnStmtContext)
                return inferReturnType(stmt);

            else if (stmt instanceof BonoboParser.HelperFuncStmtContext) {
                return STType.INT32;
            } else {
                errors.add(new CompilerError(
                        CompilerError.ERROR,
                        "All functions must return a value",
                        stmt,
                        sourceFile));
                return null;
            }
        } else if (ctx instanceof BonoboParser.ExprBodyContext) {
            return inferReturnType(((BonoboParser.ExprBodyContext) ctx).expr());
        }

        return null;
    }

    private STType inferReturnType(BonoboParser.StmtContext ctx) {
        // Todo: All types

        if (ctx instanceof BonoboParser.ReturnStmtContext) {
            return inferReturnType(((BonoboParser.ReturnStmtContext) ctx).expr());
        }

        return null;
    }

    private STType inferReturnType(BonoboParser.TypeContext returnType) {
        if (returnType instanceof BonoboParser.IntTypeContext)
            return STType.INT32;
        else if (returnType instanceof BonoboParser.TupleTypeContext)
            return new STTupleType(inferReturnType(((BonoboParser.TupleTypeContext) returnType).type()));
        return null;
    }

    private void printDebug(String msg) {
        if (debug) {
            System.out.println(msg);
        }
    }

    private STDatum resolveBinary(BonoboParser.ExprContext leftExpr, BonoboParser.ExprContext rightExpr, String op) {
        printDebug(String.format("Compiling binary: '%s %s %s'...", leftExpr.getText(), op, rightExpr.getText()));
        final STDatum left = resolveExpr(leftExpr);
        final STDatum right = resolveExpr(rightExpr);

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
                final STDatum result = binaryOnConstantNumbers(((STNumber) left).getValue(), ((STNumber) right).getValue(), op);

                if (result != null) {
                    printDebug(String.format("Result of binary operation on constants: %s", result.toCExpression().compileToC(builder)));
                    return result;
                }
            }
        }

        return null;
    }

    private STDatum resolveExpr(BonoboParser.ExprContext expr, boolean throwError) {
        if (expr instanceof BonoboParser.AdditiveExprContext)
            return resolveBinary(((BonoboParser.AdditiveExprContext) expr).left, ((BonoboParser.AdditiveExprContext) expr).right, ((BonoboParser.AdditiveExprContext) expr).op.getText());

        if (expr instanceof BonoboParser.IdentifierExprContext)
            return symbolTable.getValue(((BonoboParser.IdentifierExprContext) expr).ID().getText());

        if (expr instanceof BonoboParser.IntegerLiteralExprContext) {
            return new STNumber(Integer.parseInt(expr.getText()));
        }

        if (expr instanceof BonoboParser.ModuloExprContext)
            return resolveBinary(((BonoboParser.ModuloExprContext) expr).left, ((BonoboParser.ModuloExprContext) expr).right, "%");

        if (expr instanceof BonoboParser.MultiplicativeExprContext)
            return resolveBinary(((BonoboParser.MultiplicativeExprContext) expr).left, ((BonoboParser.MultiplicativeExprContext) expr).right, ((BonoboParser.MultiplicativeExprContext) expr).op.getText());

        if (expr instanceof BonoboParser.PowerExprContext)
            return resolveBinary(((BonoboParser.PowerExprContext) expr).left, ((BonoboParser.PowerExprContext) expr).right, "^");

        return null;
    }

    private STDatum resolveExpr(BonoboParser.ExprContext ctx) {
        return resolveExpr(ctx, true);
    }

    @Override
    public CProgram visitCompilationUnit(BonoboParser.CompilationUnitContext ctx) {
        final CProgram result = new CProgram();

        result.getIncludes().add("<stdio.h>");

        for (BonoboParser.TopLevelDefContext def : ctx.topLevelDef()) {
            if (def instanceof BonoboParser.TopLevelFuncDefContext) {
                result.getFunctions().add(visitTopLevelFuncDef((BonoboParser.TopLevelFuncDefContext) def));
            }
        }

        if (errors.isEmpty()) return result;
        else {
            final CProgram errorDumper = new CProgram();
            errorDumper.getIncludes().add("<stdio.h>");
            final CFunctionDeclaration main = new CFunctionDeclaration();
            main.setReturnType("int");
            main.setName("main");
            final CBlock block = new CBlock();

            for (CompilerError error : errors) {
                final String msg = String.format("%s: %s (%s:%d:%d)\\n", error.getType(), error.getMessage(), error.getSourceFile(), error.getLine(), error.getColumn());
                block.add(new CExpressionStatement(new CInvocationExpression(
                        new CIdentifierExpression("fprintf"),
                        new CExpression[]{new CIdentifierExpression("stderr"), new CStringExpression(msg)}
                )));
            }

            block.add(new CReturnStatement(new CNumberExpression(1)));
            main.setBlock(block);
            errorDumper.getFunctions().add(main);
            return errorDumper;
        }
    }

    private CExpression visitExpr(BonoboParser.ExprContext expr) {

        if (expr instanceof BonoboParser.IdentifierExprContext) {
            // Todo: Resolve these
            return new CIdentifierExpression(((BonoboParser.IdentifierExprContext) expr).ID().getText());
        }

        if (expr instanceof BonoboParser.StringExprContext) {
            return new CStringExpression(expr.getText().replaceAll("(^')|('$)", ""));
        }

        final STDatum resolved = resolveExpr(expr);
        return resolved != null ? resolved.toCExpression() : null;
    }

    private CBlock visitFuncBody(BonoboParser.FuncBodyContext ctx) {
        final CBlock result = new CBlock();

        if (ctx instanceof BonoboParser.ExprBodyContext) {
            final STDatum value = resolveExpr(((BonoboParser.ExprBodyContext) ctx).expr());

            if (value == null) {
                errors.add(
                        new CompilerError(CompilerError.ERROR,
                                "Function returns invalid value",
                                ((BonoboParser.ExprBodyContext) ctx).expr(),
                                sourceFile));
                return null;
            }

            result.add(new CReturnStatement(value.toCExpression()));
        } else if (ctx instanceof BonoboParser.BlockBodyContext) {
            symbolTable.create();

            for (BonoboParser.StmtContext stmt : ((BonoboParser.BlockBodyContext) ctx).block().stmt()) {
                final CStatement cstmt = visitStmt(stmt);

                if (stmt instanceof BonoboParser.ReturnStmtContext) {
                    symbolTable.release(result);
                    result.add(cstmt);
                    return result;
                }

                if (cstmt != null)
                    result.add(cstmt);
            }

            symbolTable.release(result);
        } else if (ctx instanceof BonoboParser.StmtBodyContext) {
            final CStatement cstmt = visitStmt(((BonoboParser.StmtBodyContext) ctx).stmt());

            if (cstmt != null)
                result.add(cstmt);
        }

        return result;
    }

    @Override
    public CStatement visitHelperFuncStmt(BonoboParser.HelperFuncStmtContext ctx) {
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

    private CStatement visitStmt(BonoboParser.StmtContext stmt) {

        if (stmt instanceof BonoboParser.HelperFuncStmtContext) {
            return visitHelperFuncStmt((BonoboParser.HelperFuncStmtContext) stmt);
        }

        if (stmt instanceof BonoboParser.ReturnStmtContext) {
            return new CReturnStatement(visitExpr(((BonoboParser.ReturnStmtContext) stmt).expr()));
        }

        if (stmt instanceof BonoboParser.VarDeclStmtContext) {
            final boolean isFinal = ((BonoboParser.VarDeclStmtContext) stmt).specifier.getText().equals("let");
            final CMultipleStatements stmts = new CMultipleStatements();

            for (BonoboParser.VariableDeclarationContext decl : ((BonoboParser.VarDeclStmtContext) stmt).variableDeclaration()) {
                final String name = decl.name.getText();

                // Check if variable exists in local scope
                if (symbolTable.getInnerMostScope().contains(name)) {
                    errors.add(new CompilerError(
                            CompilerError.ERROR,
                            String.format("Symbol '%s' already exists in this context.", name),
                            decl,
                            sourceFile
                    ));
                } else {
                    final STDatum value = resolveExpr(decl.expr());

                    try {
                        symbolTable.setValue(name, value, decl, isFinal);
                        stmts.getChildren().add(new CVariableDeclarationStatement(name, value.getType().toCType(), value.toCExpression()));
                    } catch (CompilerError exc) {
                        errors.add(exc);
                    }
                }
            }

            return stmts;
        }

        return null;
    }

    @Override
    public CFunctionDeclaration visitTopLevelFuncDef(BonoboParser.TopLevelFuncDefContext ctx) {
        final CFunctionDeclaration result = new CFunctionDeclaration();
        final STType returnType = ctx.funcSignature().returnType != null ?
                inferReturnType(ctx.funcSignature().returnType)
                : inferReturnType(ctx.funcBody());
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
