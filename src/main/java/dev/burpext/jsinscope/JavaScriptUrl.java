package dev.burpext.jsinscope;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** URL helpers kept separate from Burp types so their edge cases can be unit-tested. */
final class JavaScriptUrl {
    private JavaScriptUrl() {
    }

    /**
     * Matches normal JavaScript resources. Query strings and fragments do not affect the
     * filename check; for example, /app.js?v=42 is still a JavaScript file.
     */
    static boolean isJavaScriptFile(String url) {
        try {
            String path = new URI(url).getPath();
            if (path == null) {
                return false;
            }
            String lowerPath = path.toLowerCase(Locale.ROOT);
            return lowerPath.endsWith(".js")
                    || lowerPath.endsWith(".mjs")
                    || lowerPath.endsWith(".cjs");
        } catch (URISyntaxException | IllegalArgumentException exception) {
            // Burp can expose malformed traffic. Failing closed keeps the handler alive and
            // avoids filling the table with an entry whose identity cannot be trusted.
            return false;
        }
    }

    /**
     * Removes URL fragments because fragments never reach an HTTP server. The query is kept:
     * /app.js?v=1 and /app.js?v=2 may be different application builds and should remain visible.
     */
    static String identity(String url) {
        try {
            URI uri = new URI(url);
            return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uri.getQuery(), null)
                    .toASCIIString();
        } catch (URISyntaxException | IllegalArgumentException exception) {
            return url;
        }
    }
}
