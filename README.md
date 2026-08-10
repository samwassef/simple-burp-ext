# JS in Scope

Burp Suite Montoya extension that lists in-scope `.js`, `.mjs`, and `.cjs` resources in a dedicated tab.

## How it works

At startup, the extension creates its Swing tab, registers a passive HTTP handler, and scans existing
site-map entries. Later HTTP responses are checked against the current Burp target scope and their URL
path. Matching resources are normalized, de-duplicated by URL, and displayed in insertion order.

The implementation is split into the Burp lifecycle entry point, passive traffic handler, URL helper,
immutable row data, Swing table model, and Swing panel. See
[Architecture and flow](docs/architecture.md) for class, component, and processing diagrams.

## Build and load

Requires JDK 17+, Maven 3.9+, and a Burp release compatible with Montoya API 2026.7.

```shell
mvn clean test package
```

In Burp, choose **Extensions > Installed > Add**, select **Java**, and load
`target/js-in-scope-1.0.0.jar`. Set **Target > Scope**, browse the application, then open
**JS in Scope**. Logs appear in the extension Output and Errors panes.

Existing site-map entries are loaded at startup and live responses are observed afterward. Detection
is case-insensitive and checks the URL path: `/app.js?v=1` matches, while `/page?file=app.js` does not.
The extension is passive and never modifies HTTP traffic.
