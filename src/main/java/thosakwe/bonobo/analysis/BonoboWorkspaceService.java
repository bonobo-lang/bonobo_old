package thosakwe.bonobo.analysis;

import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.services.WorkspaceService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created on 2/14/2017.
 */
public class BonoboWorkspaceService implements WorkspaceService {
    private final boolean debug;
    private final BonoboLanguageServerContext serverContext;

    public BonoboWorkspaceService(boolean debug, BonoboLanguageServerContext context) {
        this.debug = debug;
        this.serverContext = context;
    }

    @Override
    public CompletableFuture<List<? extends SymbolInformation>> symbol(WorkspaceSymbolParams workspaceSymbolParams) {
        return null;
    }

    @Override
    public void didChangeConfiguration(DidChangeConfigurationParams didChangeConfigurationParams) {

    }

    @Override
    public void didChangeWatchedFiles(DidChangeWatchedFilesParams didChangeWatchedFilesParams) {

    }
}
