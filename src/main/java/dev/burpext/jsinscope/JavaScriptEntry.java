package dev.burpext.jsinscope;

import java.time.Instant;

/** Immutable snapshot displayed by the Swing table. */
record JavaScriptEntry(
        String url,
        String method,
        int statusCode,
        String mimeType,
        String source,
        Instant lastSeen) {
}
