package dev.burpext.jsinscope;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;

import javax.swing.SwingUtilities;
import java.time.Instant;

/** Burp entry point. The public no-argument constructor is supplied implicitly. */
public final class Extension implements BurpExtension {
    // Burp services and the suite-tab view are retained for callbacks after initialization.
    private MontoyaApi api;
    private JavaScriptPanel panel;

    @Override
    public void initialize(MontoyaApi api) {
        // Phase 1: publish extension identity before registering any callbacks or UI.
        this.api = api;
        api.extension().setName("JS in Scope");

        // Burp may call initialize off the Swing Event Dispatch Thread. invokeAndWait ensures the
        // component is completely constructed before Burp registers and displays it.
        try {
            if (SwingUtilities.isEventDispatchThread()) {
                panel = new JavaScriptPanel(this::refreshFromSiteMap);
            } else {
                SwingUtilities.invokeAndWait(() -> panel = new JavaScriptPanel(this::refreshFromSiteMap));
            }
        } catch (Exception exception) {
            api.logging().logToError("Unable to create JS in Scope user interface", exception);
            throw new IllegalStateException("Unable to initialize extension UI", exception);
        }

        // Phase 2: integrate the completed view and passive response observer with Burp.
        api.userInterface().applyThemeToComponent(panel);
        api.userInterface().registerSuiteTab("JS in Scope", panel);
        api.http().registerHttpHandler(new JavaScriptHttpHandler(api, panel));
        // Phase 3: seed the table with traffic Burp captured before this extension loaded.
        refreshFromSiteMap();
        api.logging().logToOutput("JS in Scope loaded: monitoring in-scope .js, .mjs and .cjs files.");
    }

    private void refreshFromSiteMap() {
        // Site maps can be large, so scan away from Swing's UI thread. The panel safely hands each
        // result back to Swing's Event Dispatch Thread.
        Thread worker = new Thread(() -> {
            try {
                // Apply cheap validity and scope checks before constructing display records.
                api.siteMap().requestResponses().stream()
                        .filter(item -> item.request() != null && item.response() != null)
                        .filter(item -> api.scope().isInScope(item.request().url()))
                        .filter(item -> JavaScriptUrl.isJavaScriptFile(item.request().url()))
                        .forEach(item -> panel.addOrUpdate(new JavaScriptEntry(
                                item.request().url(), item.request().method(),
                                item.response().statusCode(), item.response().mimeType().toString(),
                                "SITE_MAP", Instant.now())));
            } catch (RuntimeException exception) {
                api.logging().logToError("Could not refresh JavaScript files from the site map", exception);
            }
        }, "js-in-scope-site-map-refresh");
        worker.setDaemon(true);
        worker.start();
    }
}
