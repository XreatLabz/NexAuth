package xyz.xreatlabs.nexauth.api.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SemanticVersionTest {

    @Test
    void parseValidBetaVersion() {
        var version = SemanticVersion.parse("0.0.1-beta2");

        assertNotNull(version);
        assertEquals(0, version.major());
        assertEquals(0, version.minor());
        assertEquals(1, version.patch());
        assertTrue(version.dev());
    }

    @Test
    void parseInvalidVersionReturnsNull() {
        assertNull(SemanticVersion.parse("not-a-version"));
        assertNull(SemanticVersion.parse("1.2"));
        assertNull(SemanticVersion.parse(null));
    }

    @Test
    void compareTreatsNullAsUnknownNewerVersion() {
        var current = SemanticVersion.parse("0.0.1-beta2");

        assertNotNull(current);
        assertEquals(1, current.compare(null));
    }
}
