package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;
import thosakwe.bonobo.Bonobo;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.objects.BonoboFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created on 2/14/2017.
 */
public class BonoboTextDocumentService implements TextDocumentService {
    private final boolean debug;
    private final BonoboLanguageServerContext serverContext;

    public BonoboTextDocumentService(boolean debug, BonoboLanguageServerContext context) {
        this.debug = debug;
        this.serverContext = context;
    }

    private BonoboLibrary analyze(String uri) throws IOException {
        try {
            BonoboParser parser = Bonobo.parseFile(uri);
            return analyze(parser);
        } catch (IOException exc) {
            return new BonoboLibrary(null);
        }
    }

    private BonoboLibrary analyze(TextDocumentPositionParams textDocumentPositionParams) {
        try {
            BonoboParser parser = Bonobo.parseFile(textDocumentPositionParams.getTextDocument().getUri());
            return analyze(parser);
        } catch (IOException exc) {
            return new BonoboLibrary(null);
        }
    }

    private BonoboLibrary analyze(BonoboParser parser) {
        try {
            StaticAnalyzer analyzer = new StaticAnalyzer(debug);
            return analyzer.analyzeCompilationUnit(parser.compilationUnit());
        } catch (BonoboException exc) {
            return new BonoboLibrary(parser.compilationUnit());
        }
    }

    private void diagnose(String uri, BonoboParser.CompilationUnitContext ast) {
        try {
            BonoboLibrary library = analyze(uri);
            ErrorChecker errorChecker = new ErrorChecker()
            Diagnostic diagnostic = new Diagnostic();
            diagnostic.setMessage("WTF LOL");
            diagnostic.setRange(getNodeRange(ast));
            diagnostic.setSeverity(DiagnosticSeverity.Error);
            diagnostic.setSource(ast.getText());
            serverContext.getLanguageClient().logMessage(new MessageParams(MessageType.Warning, "WTF"));
            PublishDiagnosticsParams diagnosticsParams = new PublishDiagnosticsParams();
            List<Diagnostic> diagnosticsList = new ArrayList<>();
            diagnosticsParams.setUri(uri);
            diagnosticsList.add(diagnostic);
            diagnosticsParams.setDiagnostics(diagnosticsList);
            serverContext.getLanguageClient().publishDiagnostics(diagnosticsParams);
        } catch (Exception exc) {
            // Ignore IOException
        }
    }

    private Range getNodeRange(ParserRuleContext ctx) {
        Range range = new Range();
        range.getStart().setCharacter(ctx.start.getCharPositionInLine());
        range.getStart().setLine(ctx.start.getLine());
        range.getEnd().setCharacter(ctx.stop.getCharPositionInLine());
        range.getEnd().setCharacter(ctx.stop.getLine());
        return range;
    }


    private void validateTextDocument(String uri, String text) {
        try {
            BonoboParser parser = Bonobo.parseText(text);
            diagnose(uri, parser.compilationUnit());
        } catch (Exception exc) {
            System.err.println("Validation error: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    private void validateTextDocument(TextDocumentIdentifier documentId) {
        try {
            BonoboParser parser = Bonobo.parseFile(documentId.getUri());
            diagnose(documentId.getUri(), parser.compilationUnit());
        } catch (Exception exc) {
            System.err.println("Validation error: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    private void validateTextDocument(TextDocumentItem document) {
        try {
            BonoboParser parser = Bonobo.parseText(document.getText());
            diagnose(document.getUri(), parser.compilationUnit());
        } catch (Exception exc) {
            System.err.println("Validation error: " + exc.getMessage());
            exc.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<CompletionList> completion(TextDocumentPositionParams textDocumentPositionParams) {
        BonoboLibrary library = analyze(textDocumentPositionParams);
        CompletionList completions = new CompletionList();
        List<CompletionItem> completionItemList = new ArrayList<>();
        System.out.printf("Completing for %d export(s) from %s%n", library.getExports().size(), library.getSource().getText());

        for (String name : library.getExports().keySet()) {
            System.out.printf("Completing %s%n", name);
            BonoboObject value = library.getExports().get(name);
            CompletionItem completionItem = new CompletionItem();
            completionItem.setKind(value instanceof BonoboFunction ? CompletionItemKind.Function : CompletionItemKind.Variable);
            completionItem.setLabel(name);
            completionItemList.add(completionItem);
        }

        completions.setItems(completionItemList);
        return CompletableFuture.completedFuture(completions);
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem completionItem) {
        // TODO: Wtf is this???
        return CompletableFuture.completedFuture(completionItem);
    }

    @Override
    public CompletableFuture<Hover> hover(TextDocumentPositionParams textDocumentPositionParams) {
        return null;
    }

    @Override
    public CompletableFuture<SignatureHelp> signatureHelp(TextDocumentPositionParams textDocumentPositionParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> definition(TextDocumentPositionParams textDocumentPositionParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Location>> references(ReferenceParams referenceParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends DocumentHighlight>> documentHighlight(TextDocumentPositionParams textDocumentPositionParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> documentSymbol(DocumentSymbolParams documentSymbolParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends Command>> codeAction(CodeActionParams codeActionParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends CodeLens>> codeLens(CodeLensParams codeLensParams) {
        return null;
    }

    @Override
    public CompletableFuture<CodeLens> resolveCodeLens(CodeLens codeLens) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> formatting(DocumentFormattingParams documentFormattingParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> rangeFormatting(DocumentRangeFormattingParams documentRangeFormattingParams) {
        return null;
    }

    @Override
    public CompletableFuture<List<? extends TextEdit>> onTypeFormatting(DocumentOnTypeFormattingParams documentOnTypeFormattingParams) {
        return null;
    }

    @Override
    public CompletableFuture<WorkspaceEdit> rename(RenameParams renameParams) {
        return null;
    }

    @Override
    public void didOpen(DidOpenTextDocumentParams didOpenTextDocumentParams) {
        validateTextDocument(didOpenTextDocumentParams.getTextDocument());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
        validateTextDocument(didChangeTextDocumentParams.getTextDocument());
    }

    @Override
    public void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) {
        validateTextDocument(didCloseTextDocumentParams.getTextDocument());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {
        validateTextDocument(didSaveTextDocumentParams.getTextDocument());
    }
}
