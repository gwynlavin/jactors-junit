package org.jactors.junit.rule;

import org.jactors.junit.EnumTest;
import org.jactors.junit.Expect;
import org.jactors.junit.test.SimpleTest;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.MethodSorters;

/**
 * Base rule test.
 */
@FixMethodOrder(MethodSorters.JVM)
public class BaseRuleTest extends SimpleTest {
    /**
     * Ensure enums are extended.
     */
    @BeforeClass
    public static void before() {
       EnumTest.ensure();
    }

    /**
     * Check base rule for duplicate description setup.
     */
    @Test(expected = RuntimeException.class)
    @Expect(message = "rule description must not be initialized twice")
    public void init() {
        BaseRule rule = new BaseRule();
        rule.init(Description.createSuiteDescription("test"));
        rule.init(Description.createSuiteDescription("test"));
    }

    /**
     * Check base rule uninitialized description query.
     */
    @Test(expected = RuntimeException.class)
    @Expect(message = "rule description not initialized")
    public void describe() {
        new BaseRule().describe();
    }
}
