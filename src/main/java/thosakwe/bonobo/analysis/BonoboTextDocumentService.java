package thosakwe.bonobo.analysis;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.TextDocumentService;
import thosakwe.bonobo.Bonobo;
import thosakwe.bonobo.grammar.BonoboParser;
import thosakwe.bonobo.language.BonoboException;
import thosakwe.bonobo.language.BonoboLibrary;
import thosakwe.bonobo.language.BonoboObject;
import thosakwe.bonobo.language.objects.BonoboFunction;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created on 2/14/2017.
 */
public class BonoboTextDocumentService implements TextDocumentService {
    private final boolean debug;

    public BonoboTextDocumentService(boolean debug) {
        this.debug = debug;
    }

    @Override
    public CompletableFuture<CompletionList> completion(TextDocumentPositionParams textDocumentPositionParams) {
        BonoboLibrary library = analyze(textDocumentPositionParams);
        CompletionList completions = new CompletionList();

        for (String name : library.getExports().keySet()) {
            BonoboObject value = library.getExports().get(name);
            CompletionItem completionItem = new CompletionItem();
            completionItem.setKind(value instanceof BonoboFunction ? CompletionItemKind.Function : CompletionItemKind.Variable);
            completionItem.setLabel(name);
            completions.getItems().add(completionItem);
        }

        return CompletableFuture.completedFuture(completions);
    }

    private BonoboLibrary analyze(TextDocumentPositionParams textDocumentPositionParams) {
        try {
            BonoboParser parser = Bonobo.parseFile(textDocumentPositionParams.getTextDocument().getUri());
            StaticAnalyzer analyzer = new StaticAnalyzer(debug);

            try {
                return analyzer.analyzeCompilationUnit(parser.compilationUnit());
            } catch(BonoboException exc) {
                return new BonoboLibrary(parser.compilationUnit());
            }
        } catch (IOException exc) {
            return new BonoboLibrary(null);
        }
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

    }

    @Override
    public void didChange(DidChangeTextDocumentParams didChangeTextDocumentParams) {
    }

    @Override
    public void didClose(DidCloseTextDocumentParams didCloseTextDocumentParams) {

    }

    @Override
    public void didSave(DidSaveTextDocumentParams didSaveTextDocumentParams) {

    }
}
