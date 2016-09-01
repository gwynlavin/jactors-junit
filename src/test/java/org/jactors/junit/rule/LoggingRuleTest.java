package org.jactors.junit.rule;

import org.jactors.junit.Expect;
import org.jactors.junit.rule.LoggingRule;
import org.jactors.junit.test.SimpleTest;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.MethodSorters;


/**
 * Logging rule test.
 */
@FixMethodOrder(MethodSorters.JVM)
public class LoggingRuleTest extends SimpleTest { // NOPMD: simple test

    /**
     * Static logging rule to improve audit output.
     */
    @ClassRule
    public static final LoggingRule LOG = new LoggingRule();

    /**
     * Logging rule to improved audit output.
     */
    @Rule
    public final LoggingRule log = new LoggingRule();

    /**
     * Check logging function on successful test.
     */
    @Test
    public void success() {
        // nothing to do!
    }

    /**
     * Check logging function on failing test.
     */
    @Test(expected = IllegalArgumentException.class)
    @Expect(message = "failure")
    public void failure() {
        throw new IllegalArgumentException("failure");
    }
}
