package thosakwe.bonobo.analysis;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.concurrent.CompletableFuture;

/**
 * Created on 2/14/2017.
 */
public class BonoboLanguageServer implements LanguageServer {
    private final TextDocumentService textDocumentService = new BonoboTextDocumentService();
    private final WorkspaceService workspaceService = new BonoboWorkspaceService();

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
        return null;
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return null;
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
