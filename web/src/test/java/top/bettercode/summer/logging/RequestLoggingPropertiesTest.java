package top.bettercode.summer.logging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Peter Wu
 */
class RequestLoggingPropertiesTest {

    @Test
    void matchIgnored() {
        boolean match = new RequestLoggingProperties().matchIgnored("/3.7bfc95301c8035c803d4.js");
        System.err.println(match);
        Assertions.assertTrue(match);
        match = new RequestLoggingProperties().matchIgnored("/static/js/3.7bfc95301c8035c803d4.js");
        System.err.println(match);
        Assertions.assertTrue(match);
    }
}