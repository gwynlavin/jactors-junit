package org.jactors.junit.test;

import org.jactors.junit.rule.ExpectRule;
import org.jactors.junit.rule.SpringRule;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

/**
 * Abstract simple spring capable test.
 */
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.JVM)
public abstract class SimpleTest {

    /**
     * Spring class rule for dependency injection.
     */
    @ClassRule
    public static final SpringRule.ClassRule INJECTION = new SpringRule.ClassRule();

    /**
     * Spring method rule for transaction behavior.
     */
    @Rule
    public final SpringRule.MethodRule transaction = new SpringRule.MethodRule();

    /**
     * Activate expectation rule on any request. This is required for spring tests to enable correct
     * handling of exceptions in case of transactions. Otherwise, exceptions would be caught by the
     * declared test expectation before being handled by the spring transaction rule.
     */
    @Rule
    public final ExpectRule rule = new ExpectRule(true);
}
