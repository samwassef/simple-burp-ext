# JS in Scope

Burp Suite Montoya extension that lists in-scope `.js`, `.mjs`, and `.cjs` resources in a dedicated tab.

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
