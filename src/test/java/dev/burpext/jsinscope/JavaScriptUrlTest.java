package dev.burpext.jsinscope;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaScriptUrlTest {
    @Test
    void acceptsExtensionsIgnoringCaseAndQuery() {
        assertTrue(JavaScriptUrl.isJavaScriptFile("https://example.test/app.js?v=1"));
        assertTrue(JavaScriptUrl.isJavaScriptFile("https://example.test/module.MJS"));
        assertTrue(JavaScriptUrl.isJavaScriptFile("https://example.test/server.cjs#fragment"));
    }

    @Test
    void rejectsNonJavaScriptAndMisleadingQueryValues() {
        assertFalse(JavaScriptUrl.isJavaScriptFile("https://example.test/index.html?file=app.js"));
        assertFalse(JavaScriptUrl.isJavaScriptFile("not a valid URL"));
    }

    @Test
    void identityDropsFragmentButPreservesQuery() {
        assertEquals("https://example.test/app.js?v=2",
                JavaScriptUrl.identity("https://example.test/app.js?v=2#source"));
    }
}
