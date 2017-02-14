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

    private BonoboLibrary analyze(TextDocumentPositionParams textDocumentPositionParams) {
        try {
            BonoboParser parser = Bonobo.parseFile(textDocumentPositionParams.getTextDocument().getUri());
            StaticAnalyzer analyzer = new StaticAnalyzer(debug);

            try {
                return analyzer.analyzeCompilationUnit(parser.compilationUnit());
            } catch (BonoboException exc) {
                return new BonoboLibrary(parser.compilationUnit());
            }
        } catch (IOException exc) {
            return new BonoboLibrary(null);
        }
    }

    private void diagnose(String uri, BonoboParser.CompilationUnitContext ast) {
        System.out.printf("Diagnosing %s...%n", uri);
        Diagnostic diagnostic = new Diagnostic();
        diagnostic.setMessage("WTF LOL");
        diagnostic.setRange(getNodeRange(ast));
        diagnostic.setSeverity(DiagnosticSeverity.Error);
        diagnostic.setSource(ast.getText());
        serverContext.getLanguageClient().publishDiagnostics(new PublishDiagnosticsParams(uri, Collections.singletonList(diagnostic)));
    }

    private Range getNodeRange(ParserRuleContext ctx) {
        Range range = new Range();
        range.getStart().setCharacter(ctx.start.getCharPositionInLine());
        range.getStart().setLine(ctx.start.getLine());
        range.getEnd().setCharacter(ctx.stop.getCharPositionInLine());
        range.getEnd().setCharacter(ctx.stop.getLine());
        return range;
    }

    private void validateTextDocument(TextDocumentIdentifier documentId) {
        try {
            BonoboParser parser = Bonobo.parseFile(documentId.getUri());
            diagnose(documentId.getUri(), parser.compilationUnit());
        } catch (Exception exc) {
            //
        }
    }

    private void validateTextDocument(TextDocumentItem document) {
        try {
            BonoboParser parser = Bonobo.parseText(document.getText());
            diagnose(document.getUri(), parser.compilationUnit());
        } catch (Exception exc) {
            //
        }
    }

    @Override
    public CompletableFuture<CompletionList> completion(TextDocumentPositionParams textDocumentPositionParams) {
        BonoboLibrary library = analyze(textDocumentPositionParams);
        CompletionList completions = new CompletionList();
        completions.setItems(new ArrayList<>());

        for (String name : library.getExports().keySet()) {
            System.out.printf("Completing %s%n", name);
            BonoboObject value = library.getExports().get(name);
            CompletionItem completionItem = new CompletionItem();
            completionItem.setKind(value instanceof BonoboFunction ? CompletionItemKind.Function : CompletionItemKind.Variable);
            completionItem.setLabel(name);
            completions.getItems().add(completionItem);
        }

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
        // validateTextDocument(didCloseTextDocumentParams.getTextDocument());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {
        // validateTextDocument(didSaveTextDocumentParams.getTextDocument());
    }
}
