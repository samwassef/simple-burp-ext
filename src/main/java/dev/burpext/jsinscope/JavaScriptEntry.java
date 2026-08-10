package dev.burpext.jsinscope;

import java.time.Instant;

/**
 * Immutable snapshot displayed by the Swing table.
 *
 * @param url resource URL used to derive the table identity
 * @param method HTTP method of the initiating request
 * @param statusCode received HTTP response status
 * @param mimeType MIME type classified by Burp
 * @param source Burp tool or site-map scan that observed the resource
 * @param lastSeen time of the most recent observation
 */
record JavaScriptEntry(
        String url,
        String method,
        int statusCode,
        String mimeType,
        String source,
        Instant lastSeen) {
}
