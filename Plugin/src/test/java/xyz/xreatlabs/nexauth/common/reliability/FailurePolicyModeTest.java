package xyz.xreatlabs.nexauth.common.reliability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FailurePolicyModeTest {

    @Test
    void parseNullFallsBackToHardFail() {
        assertEquals(FailurePolicyMode.HARD_FAIL, FailurePolicyMode.parse(null));
    }

    @Test
    void parseInvalidFallsBackToHardFail() {
        assertEquals(FailurePolicyMode.HARD_FAIL, FailurePolicyMode.parse("invalid"));
    }

    @Test
    void parseKnownValueWorksCaseInsensitive() {
        assertEquals(FailurePolicyMode.RETRY_THEN_DISABLE, FailurePolicyMode.parse("retry_then_disable"));
    }
}
