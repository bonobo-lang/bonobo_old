package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.Position;
import thosakwe.bonobo.grammar.BonoboBaseListener;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.objects.BonoboFunction;
import thosakwe.bonobo.language.objects.BonoboFunctionParameter;
import thosakwe.bonobo.language.types.BonoboFunctionType;
import thosakwe.bonobo.language.types.BonoboUnknownType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeCompleter extends BonoboBaseListener {
    private final List<String> addedNames = new ArrayList<>();
    private final StaticAnalyzer analyzer;
    private final List<CompletionItem> completionItems = new ArrayList<>();
    private final Position currentPosition;
    private final BonoboParser parser;
    private final BonoboTextDocumentService textDocumentService;

    public CodeCompleter(BonoboTextDocumentService textDocumentService, BonoboParser parser, StaticAnalyzer analyzer, Position position) {
        this.analyzer = analyzer;
        this.currentPosition = position;
        this.parser = parser;
        this.textDocumentService = textDocumentService;
    }

    public CompletionList complete(BonoboParser.CompilationUnitContext ast) {
        try {

            try {
                ParserRuleContext ctx = treeAtPosition(currentPosition, ast);
                BonoboLibrary library = analyzer.analyzeCompilationUnit(ast);
                completeTree(ctx, library);
                // ErrorChecker errorChecker = new ErrorChecker(analyzer);
                // errorChecker.visitLibrary(library);

                for (String name : library.getExports().keySet()) {
                    BonoboObject value = library.getExports().get(name);
                    addCompletionItem(name, value);
                }
            } catch (BonoboException exc) {
                textDocumentService.printDebug("Error when trying to complete: " + exc.getMessage());
                exc.printStackTrace();
            }
        } catch (Exception exc) {
            System.err.println("Completion error: " + exc.getMessage());
            exc.printStackTrace();
        }

        // ParseTreeWalker.DEFAULT.walk(this, ast);
        textDocumentService.printDebug(String.format("Completed with %d item(s)%n", completionItems.size()));
        CompletionList completionList = new CompletionList();
        completionList.setIsIncomplete(completionItems.isEmpty());
        completionList.setItems(completionItems);
        return completionList;
    }

    private void addCompletionItem(String name, BonoboObject value) {
        if (value.getType() != BonoboUnknownType.INSTANCE && !addedNames.contains(name)) {
            completionItems.add(completeObject(name, value));
            addedNames.add(name);
        }
    }

    private void completeTree(ParserRuleContext ctx, BonoboLibrary library) throws BonoboException {
        Map<String, BonoboObject> symbols = currentlyAvailableSymbols(ctx, library);

        if (symbols != null) {
            for (String name : symbols.keySet()) {
                addCompletionItem(name, symbols.get(name));
            }
        }
    }

    private Map<String, BonoboObject> currentlyAvailableSymbols(ParserRuleContext ctx, BonoboLibrary library) throws BonoboException {
        // Find closest function
        BonoboParser.TopLevelFuncDefContext funcDefContext = null;
        ParserRuleContext current = ctx;

        while (!(current instanceof BonoboParser.TopLevelFuncDefContext)) {
            ParserRuleContext parent = current.getParent();

            if (parent instanceof BonoboParser.TopLevelFuncDefContext) {
                funcDefContext = (BonoboParser.TopLevelFuncDefContext) parent;
                break;
            } else if (parent == null) break;
            else current = parent;
        }

        Map<String, BonoboObject> result = new HashMap<>();

        if (funcDefContext == null) {
            textDocumentService.printDebug("Couldn't resolve nearest top-level function.");
        } else {
            String name = funcDefContext.funcSignature().name.getText();
            textDocumentService.printDebug(String.format("Nearest top-level func: %s", name));

            for (String key : library.getExports().keySet()) {
                BonoboObject value = library.getExports().get(key);

                if (key.equals(name) && value.getType().isAssignableTo(BonoboFunctionType.INSTANCE)) {
                    // Found the function we are in...
                    ParserRuleContext source = value.getSource();

                    if (source instanceof BonoboParser.TopLevelFuncDefContext) {
                        textDocumentService.printDebug(String.format("Entering top-level func for completion: %s", name));
                        analyzer.pushScope(funcDefContext);

                        // Visit every statement...
                        ErrorChecker errorChecker = new PositionAwareErrorChecker(analyzer, currentPosition);
                        errorChecker.visitFunction((BonoboFunction) value);

                        Scope lastScope = analyzer.popScope();

                        if (lastScope == null)
                            lastScope = analyzer.getScope();

                        if (lastScope != null) {
                            // result.putAll(completeScope(lastScope.getGlobalScope()));
                            result.putAll(completeScope(lastScope));
                        }

                        break;
                    } else {
                        textDocumentService.printDebug(String.format(
                                "Found a symbol %s, but its source is a(n) %s, not a TopLevelFuncDefContext.",
                                name,
                                source.getClass().getSimpleName()));
                    }
                }
            }
        }

        return result;
    }

    private Map<String, BonoboObject> completeScope(Scope scope) {
        Map<String, BonoboObject> result = new HashMap<>();

        if (scope != null && scope.getSource() != null && nodeIsInRange(currentPosition, scope.getSource())) {
            textDocumentService.printDebug(String.format("Completing scope with %d symbol(s) from %s %s", scope.size(), scope.getSource().getClass().getSimpleName(), scope.getSource().getText()));

            for (Symbol symbol : scope.getUnique()) {
                textDocumentService.printDebug(String.format("%s => %s", symbol.getName(), symbol.getValue().getType().getName()));
                result.put(symbol.getName(), symbol.getValue());
            }

            for (Scope child : scope.getChildren()) {
                result.putAll(completeScope(child));
            }
        } else if (scope != null) {
            if (scope.getSource() == null)
                textDocumentService.printDebug(String.format("%d symbol(s) out of range from null-sourced scope (global)?", scope.size()));
            else
                textDocumentService.printDebug(String.format("%d symbol(s) out of range from %s %s", scope.size(), scope.getSource().getClass().getSimpleName(), scope.getSource().getText()));
        } else {
            textDocumentService.printDebug("Completing null scope???");
        }

        return result;
    }

    public static boolean nodeIsInRange(Position position, ParserRuleContext ctx) {
        if (ctx == null || position == null) return false;
        int line = ctx.start.getLine(), charPos = ctx.start.getCharPositionInLine();
        line = line > 0 ? line - 1 : line;
        return line < position.getLine() || (line == position.getLine() && charPos <= position.getCharacter());

        /*line = ctx.stop.getLine();
        charPos = ctx.stop.getCharPositionInLine() + ctx.stop.getText().length();
        line = line > 0 ? line - 1 : line;
        boolean endWithinRange = line > position.getLine() || (line == position.getLine() && charPos >= position.getCharacter());*/

        //return startWithinRange && endWithinRange;
    }

    private ParserRuleContext treeAtPosition(Position position, ParserRuleContext ast) {
        ParserRuleContext result = null;
        // System.out.printf("%d child(ren)%n", ast.getChildCount());

        for (int i = 0; i < ast.getChildCount(); i++) {
            ParseTree child = ast.getChild(i);

            if (child instanceof ParserRuleContext) {
                ParserRuleContext ctx = (ParserRuleContext) child;

                if (nodeIsInRange(position, ctx)) {
                    result = ctx;
                    // System.out.printf("Found a(n) %s => %s...%n", ctx.getClass().getSimpleName(), ctx.getText());
                } else {
                    // System.out.printf("NOPE: %s => %s%n", ctx.getClass().getSimpleName(), ctx.getText());
                    // System.out.printf("POS: (%d:%d), CTX: (%d:%d)%n", position.getLine(), position.getCharacter(), line, ctx.start.getCharPositionInLine());
                }
            }
        }

        if (result != null)
            return treeAtPosition(position, result);
        return ast;
    }

    private CompletionItem completeObject(String name, BonoboObject value) {
        // textDocumentService.printDebug(String.format("Completing %s%n", name));
        CompletionItem completionItem = new CompletionItem();
        completionItem.setDetail(value.getType().getName());
        completionItem.setKind(getCompletionKindForObject(value));
        completionItem.setLabel(name);

        if (value instanceof BonoboFunction) {
            StringBuilder buf = new StringBuilder();
            BonoboFunction function = (BonoboFunction) value;

            buf.append(name);
            buf.append("(");

            for (int i = 0; i < function.getParameters().size(); i++) {
                if (i > 0)
                    buf.append(", ");

                BonoboFunctionParameter parameter = function.getParameters().get(i);
                buf.append(parameter.getName());
            }

            buf.append(")");

            completionItem.setInsertText(buf.toString());
        }

        // textDocumentService.printDebug(completionItem.toString());
        return completionItem;
    }

    private CompletionItemKind getCompletionKindForObject(BonoboObject value) {
        if (value instanceof BonoboFunction)
            return CompletionItemKind.Function;

        return CompletionItemKind.Variable;
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        for (int token : parser.getExpectedTokensWithinCurrentRule().toArray()) {
            String data = parser.getVocabulary().getLiteralName(token);
            CompletionItem completionItem = new CompletionItem();
            completionItem.setKind(CompletionItemKind.Keyword);
            completionItem.setLabel(data);
            completionItems.add(completionItem);
        }

        super.enterEveryRule(ctx);
    }
}
