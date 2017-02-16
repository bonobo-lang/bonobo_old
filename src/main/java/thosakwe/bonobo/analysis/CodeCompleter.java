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
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.objects.BonoboFunction;
import thosakwe.bonobo.language.objects.BonoboFunctionParameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodeCompleter extends BonoboBaseListener {
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
            ParserRuleContext ctx = treeAtPosition(currentPosition, ast);
            completeTree(ctx);
            BonoboLibrary library = analyzer.analyzeCompilationUnit(ast);

            for (String name : library.getExports().keySet()) {
                BonoboObject value = library.getExports().get(name);
                completionItems.add(completeObject(name, value));
            }
        } catch (Exception exc) {
            System.err.println("Completion error: " + exc.getMessage());
            exc.printStackTrace();
        }

        ParseTreeWalker.DEFAULT.walk(this, ast);
        textDocumentService.printDebug(String.format("Completed with %d item(s)%n", completionItems.size()));
        CompletionList completionList = new CompletionList();
        completionList.setIsIncomplete(!completionItems.isEmpty());
        completionList.setItems(completionItems);
        return completionList;
    }

    private void completeTree(ParserRuleContext ctx) {
        Map<String, BonoboObject> symbols = currentlyAvailableSymbols(ctx);

        if (symbols != null) {
            symbols.forEach(this::completeObject);
        }
    }

    private Map<String, BonoboObject> currentlyAvailableSymbols(ParserRuleContext ctx) {
        Map<String, BonoboObject> result = new HashMap<>();
        analyzer.analyzeContext(ctx);
        Scope lastScope = analyzer.getLastPoppedScope();

        if (lastScope != null) {
            lastScope = analyzer.getScope();
        }

        result.putAll(completeScope(lastScope));
        return result;
    }

    private Map<String, BonoboObject> completeScope(Scope scope) {
        Map<String, BonoboObject> result = new HashMap<>();

        if (nodeIsInRange(currentPosition, scope.getSource())) {
            for (Symbol symbol : scope.getUnique()) {
                result.put(symbol.getName(), symbol.getValue());
            }

            for (Scope child : scope.getChildren()) {
                result.putAll(completeScope(child));
            }
        }

        return result;
    }

    private boolean nodeIsInRange(Position position, ParserRuleContext ctx) {
        if (ctx == null) return false;

        int line = ctx.start.getLine();
        line = line > 0 ? line - 1 : line;
        return line < position.getLine() || (line == position.getLine() && ctx.start.getCharPositionInLine() <= position.getCharacter());
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
        textDocumentService.printDebug(String.format("Completing %s%n", name));
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

        textDocumentService.printDebug(completionItem.toString());
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
