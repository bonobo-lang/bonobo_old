package thosakwe.bonobo.analysis;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Created on 2/14/2017.
 */
public class BonoboLanguageServer implements LanguageServer, LanguageClientAware {
    private final BonoboLanguageServerContext context = new BonoboLanguageServerContext();
    private final TextDocumentService textDocumentService;
    private final WorkspaceService workspaceService;

    public BonoboLanguageServer(boolean debug) {
        this.textDocumentService = new BonoboTextDocumentService(debug, context);
        this.workspaceService = new BonoboWorkspaceService(debug, context);
    }

    @Override
    public void connect(LanguageClient languageClient) {
        context.setLanguageClient(languageClient);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
        InitializeResult result = new InitializeResult();
        result.setCapabilities(new ServerCapabilities());
        result.getCapabilities().setCompletionProvider(new CompletionOptions(true, new ArrayList<String>()));
        result.getCapabilities().setTextDocumentSync(TextDocumentSyncKind.Full);
        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }
}
