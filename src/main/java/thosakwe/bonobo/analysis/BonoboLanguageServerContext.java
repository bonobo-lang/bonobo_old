package thosakwe.bonobo.analysis;

import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class BonoboLanguageServerContext {
    private final Map<String, String> documentContents = new HashMap<>();
    private LanguageClient languageClient = null;

    public LanguageClient getLanguageClient() {
        return languageClient;
    }

    public void setLanguageClient(LanguageClient languageClient) {
        this.languageClient = languageClient;
    }

    public String normalizeUri(String uri) {
        return URI.create(uri).getPath();
    }

    public void updateUri(String uri, String text) {
        documentContents.put(normalizeUri(uri), text);
    }

    public void releaseUri(String uri) {
        String key = normalizeUri(uri);
        if (documentContents.containsKey(key))
            documentContents.remove(key);
    }

    public void updateUriFromFilesystem(String uri) throws IOException {
        File file = new File(normalizeUri(uri));
        String text = FileUtils.readFileToString(file);
        updateUri(uri, text);
    }

    public String getUriContents(String uri) throws IOException {
        String key = normalizeUri(uri);

        if (documentContents.containsKey(key))
            return documentContents.get(key);
        else {
            updateUriFromFilesystem(uri);
            return getUriContents(uri);
        }
    }
}
