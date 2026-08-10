package dev.burpext.jsinscope;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.handler.HttpHandler;
import burp.api.montoya.http.handler.HttpRequestToBeSent;
import burp.api.montoya.http.handler.HttpResponseReceived;
import burp.api.montoya.http.handler.RequestToBeSentAction;
import burp.api.montoya.http.handler.ResponseReceivedAction;

import java.time.Instant;

/** Observes Burp traffic without modifying requests, responses, or annotations. */
final class JavaScriptHttpHandler implements HttpHandler {
    // The handler reads Burp scope configuration and forwards matching rows to the view.
    private final MontoyaApi api;
    private final JavaScriptPanel panel;

    JavaScriptHttpHandler(MontoyaApi api, JavaScriptPanel panel) {
        this.api = api;
        this.panel = panel;
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent request) {
        // This extension is passive. Returning continueWith preserves the original request.
        return RequestToBeSentAction.continueWith(request);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived response) {
        // Response processing is observational: extract metadata, conditionally publish a row,
        // and always return the original response to Burp.
        try {
            var request = response.initiatingRequest();
            String url = request.url();

            // Re-check scope for every response because users can change target scope at runtime.
            if (api.scope().isInScope(url) && JavaScriptUrl.isJavaScriptFile(url)) {
                // Capture a point-in-time value so the Swing layer has no dependency on Burp messages.
                panel.addOrUpdate(new JavaScriptEntry(
                        url,
                        request.method(),
                        response.statusCode(),
                        response.mimeType().toString(),
                        response.toolSource().toolType().toString(),
                        Instant.now()));
            }
        } catch (RuntimeException exception) {
            // Malformed messages must not break Burp's HTTP processing chain. The stack trace is
            // intentionally sent to Extension output to make field troubleshooting possible.
            api.logging().logToError("Could not inspect an HTTP response", exception);
        }
        return ResponseReceivedAction.continueWith(response);
    }
}
