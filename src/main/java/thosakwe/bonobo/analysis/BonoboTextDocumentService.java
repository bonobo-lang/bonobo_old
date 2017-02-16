package thosakwe.bonobo.analysis;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.CompletableFutures;
import org.eclipse.lsp4j.services.TextDocumentService;
import thosakwe.bonobo.Bonobo;
import thosakwe.bonobo.ErrorAwareBonoboParser;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.objects.BonoboFunction;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
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

    private void ifDebug(Runnable runnable) {
        if (debug)
            runnable.run();
    }

    void printDebug(String message) {
        ifDebug(() -> System.out.println(message));
    }

    private BonoboLibrary analyze(String uri) {
        try {
            BonoboParser parser = Bonobo.parseText(serverContext.getUriContents(uri));
            return analyze(parser);
        } catch (IOException exc) {
            return new BonoboLibrary(null);
        }
    }

    private BonoboLibrary analyze(TextDocumentPositionParams textDocumentPositionParams) {
        return analyze(textDocumentPositionParams.getTextDocument().getUri());
    }

    private BonoboLibrary analyze(BonoboParser parser) {
        try {
            BonoboParser.CompilationUnitContext ast = parser.compilationUnit();
            StaticAnalyzer analyzer = new StaticAnalyzer(debug, ast);
            return analyzer.analyzeCompilationUnit(ast);
        } catch (BonoboException exc) {
            return new BonoboLibrary(parser.compilationUnit());
        }
    }

    private void diagnose(String uri, BonoboParser.CompilationUnitContext ast, ErrorAwareBonoboParser parser) {
        List<Diagnostic> diagnosticsList = new ArrayList<>();
        StaticAnalyzer analyzer = new StaticAnalyzer(debug, ast);
        ErrorChecker errorChecker = new ErrorChecker(analyzer);
        List<BonoboException> errors = new ArrayList<>();
        errors.addAll(parser.getErrors());

        try {
            BonoboLibrary library = analyzer.analyzeCompilationUnit(ast);
            errors.addAll(errorChecker.visitLibrary(library));
        } catch (BonoboException exc) {
            errors.add(exc);
        }

        for (BonoboException err : errors) {
            Diagnostic diagnostic = new Diagnostic();
            diagnostic.setMessage(err.getMessage());
            diagnostic.setRange(getNodeRange(err.getMessage(), err.getSource()));
            diagnostic.setSeverity(DiagnosticSeverity.Error);
            // diagnostic.setSource(err.getSource().getText());
            diagnosticsList.add(diagnostic);
        }

        PublishDiagnosticsParams diagnosticsParams = new PublishDiagnosticsParams();
        diagnosticsParams.setUri(uri);
        diagnosticsParams.setDiagnostics(diagnosticsList);
        printDebug(String.format("Publishing %d error(s) from %s%n", diagnosticsList.size(), uri));
        serverContext.getLanguageClient().publishDiagnostics(diagnosticsParams);
    }

    private Position getTokenPosition(Token token) {
        int line = token.getLine(), charPositionInLine = token.getCharPositionInLine();
        return new Position(line > 0 ? line - 1 : line, charPositionInLine);
    }

    private Range getNodeRange(String message, ParserRuleContext ctx) {
        Range range = new Range();
        range.setStart(getTokenPosition(ctx.getStart()));
        range.setEnd(getTokenPosition(ctx.getStop()));
        printDebug(String.format(
                "ERR %s: (%d:%d) -> (%d:%d)%n",
                message,
                range.getStart().getLine(),
                range.getStart().getCharacter(),
                range.getEnd().getLine(),
                range.getEnd().getCharacter()));
        return range;
    }


    private void validateTextDocument(String uri, String text) {
        ErrorAwareBonoboParser parser = Bonobo.parseText(text);
        diagnose(uri, parser.compilationUnit(), parser);
    }

    private void validateTextDocument(TextDocumentIdentifier documentId) {
        try {
            String uri = URI.create(documentId.getUri()).getPath();
            ErrorAwareBonoboParser parser = Bonobo.parseText(serverContext.getUriContents(uri));
            diagnose(documentId.getUri(), parser.compilationUnit(), parser);
        } catch (Exception exc) {
            ifDebug(() -> {
                System.err.println("Validation error: " + exc.getMessage());
                exc.printStackTrace();
            });
        }
    }

    private void validateTextDocument(TextDocumentItem document) {
        ErrorAwareBonoboParser parser = Bonobo.parseText(document.getText());
        diagnose(document.getUri(), parser.compilationUnit(), parser);
    }

    @Override
    public CompletableFuture<CompletionList> completion(TextDocumentPositionParams params) {
        return CompletableFutures.computeAsync(cancelToken -> {
            // cancelToken.checkCanceled();
            try {
                String uri = URI.create(params.getTextDocument().getUri()).getPath();
                printDebug(String.format("Auto-complete %s%n", uri));
                BonoboParser parser = Bonobo.parseText(serverContext.getUriContents(uri));
                BonoboParser.CompilationUnitContext ast = parser.compilationUnit();
                StaticAnalyzer analyzer = new StaticAnalyzer(debug, ast);
                CodeCompleter codeCompleter = new CodeCompleter(this, parser, analyzer, params.getPosition());
                return codeCompleter.complete(ast);
            } catch (IOException exc) {
                CompletionList completions = new CompletionList();
                completions.setIsIncomplete(false);
                completions.setItems(new ArrayList<>());
                return completions;
            }
        });
    }

    @Override
    public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem completionItem) {
        // TODO: Resolve completions?
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
    public void didOpen(DidOpenTextDocumentParams params) {
        serverContext.updateUri(params.getTextDocument().getUri(), params.getTextDocument().getText());
        validateTextDocument(params.getTextDocument());
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String text = params.getContentChanges().get(0).getText();
        serverContext.updateUri(params.getTextDocument().getUri(), text);
        validateTextDocument(params.getTextDocument().getUri(), text);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        serverContext.releaseUri(params.getTextDocument().getUri());
        validateTextDocument(params.getTextDocument());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {
        try {
            serverContext.updateUriFromFilesystem(params.getTextDocument().getUri());
            validateTextDocument(params.getTextDocument());
        } catch(Exception exc) {
            ifDebug(() -> {
                System.err.println("Error on save: " + exc.getMessage());
                exc.printStackTrace();
            });
        }
    }
}
