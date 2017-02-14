package thosakwe.bonobo.analysis;

import org.eclipse.lsp4j.services.LanguageClient;

public class BonoboLanguageServerContext {
    private LanguageClient languageClient = null;

    public LanguageClient getLanguageClient() {
        return languageClient;
    }

    public void setLanguageClient(LanguageClient languageClient) {
        this.languageClient = languageClient;
    }
}
