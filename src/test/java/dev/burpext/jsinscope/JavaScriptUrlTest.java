package dev.burpext.jsinscope;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaScriptUrlTest {
    // Detection accepts the supported extensions independently of case and URL suffixes.
    @Test
    void acceptsExtensionsIgnoringCaseAndQuery() {
        assertTrue(JavaScriptUrl.isJavaScriptFile("https://example.test/app.js?v=1"));
        assertTrue(JavaScriptUrl.isJavaScriptFile("https://example.test/module.MJS"));
        assertTrue(JavaScriptUrl.isJavaScriptFile("https://example.test/server.cjs#fragment"));
    }

    @Test
    void rejectsNonJavaScriptAndMisleadingQueryValues() {
        // Only the parsed path is eligible; malformed input fails closed.
        assertFalse(JavaScriptUrl.isJavaScriptFile("https://example.test/index.html?file=app.js"));
        assertFalse(JavaScriptUrl.isJavaScriptFile("not a valid URL"));
    }

    @Test
    void identityDropsFragmentButPreservesQuery() {
        // Identity mirrors HTTP semantics: fragments are client-side, while queries reach the server.
        assertEquals("https://example.test/app.js?v=2",
                JavaScriptUrl.identity("https://example.test/app.js?v=2#source"));
    }
}
