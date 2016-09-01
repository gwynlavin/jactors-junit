package org.jactors.junit.rule;

import org.hamcrest.CoreMatchers;
import org.jactors.junit.Expect;
import org.jactors.junit.rule.ExpectRule;
import org.jactors.junit.test.SimpleTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Suite;


/**
 * Expect rulte test.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ ExpectRuleTest.RuleBehavior.class })
public class ExpectRuleTest { // NOPMD: test suite!

    /**
     * Check rule behavior.
     */
    @FixMethodOrder(MethodSorters.JVM)
    public static final class RuleBehavior extends SimpleTest {

        /**
         * Test expectation with null value.
         */
        @Test
        public void expectUnset() {
            ExpectRule rule = new ExpectRule();
            Assert.assertThat(rule.expect(), CoreMatchers.equalTo(ExpectRule.UNSET));
            Assert.assertThat(rule.actual(), CoreMatchers.equalTo(ExpectRule.UNSET));
            Assert.assertThat(rule.toString(), CoreMatchers.notNullValue());
            rule.success();
        }

        /**
         * Test expectation with null value.
         */
        @Test
        public void expectNull() {
            ExpectRule rule = new ExpectRule();
            rule.expect((Object) null);
            rule.actual((Object) null);
            Assert.assertThat(rule.expect(), CoreMatchers.equalTo(null));
            Assert.assertThat(rule.actual(), CoreMatchers.equalTo(null));
            Assert.assertThat(rule.toString(), CoreMatchers.notNullValue());
            rule.success();
        }

        /**
         * Test expectation with same values.
         */
        @Test
        public void expectSameValue() {
            String value = "test";
            ExpectRule rule = new ExpectRule();
            rule.expect((Object) value);
            rule.actual((Object) value);
            Assert.assertThat(rule.<String>expect(), CoreMatchers.equalTo(value));
            Assert.assertThat(rule.<String>actual(), CoreMatchers.equalTo(value));
            Assert.assertThat(rule.toString(), CoreMatchers.notNullValue());
            rule.success();
        }

        /**
         * Test expectation with actual value unset.
         */
        @Test
        public void expectActualUnset() {
            ExpectRule rule = new ExpectRule();
            rule.expect((Object) null);
            Assert.assertThat(rule.expect(), CoreMatchers.equalTo(null));
            Assert.assertThat(rule.actual(), CoreMatchers.equalTo(ExpectRule.UNSET));
            Assert.assertThat(rule.toString(), CoreMatchers.notNullValue());
            rule.success();
        }

        /**
         * Test expectation with expected value unset.
         */
        @Test(expected = AssertionError.class)
        @Expect(message = "missing value [expect]")
        public void expectExpectUnset() {
            ExpectRule rule = new ExpectRule();
            rule.actual((Object) null);
            Assert.assertThat(rule.expect(), CoreMatchers.equalTo(ExpectRule.UNSET));
            Assert.assertThat(rule.actual(), CoreMatchers.equalTo(null));
            Assert.assertThat(rule.toString(), CoreMatchers.notNullValue());
            rule.success();
        }

        /**
         * Test expectation with same expect value.
         */
        @Test
        public void expectSameExpectValue() {
            Object value = Expect.Builder.create().build();
            ExpectRule rule = new ExpectRule();
            rule.expect(value);
            rule.actual(value);
            Assert.assertThat(rule.expect(), CoreMatchers.equalTo(value));
            Assert.assertThat(rule.actual(), CoreMatchers.equalTo(value));
            Assert.assertThat(rule.toString(), CoreMatchers.notNullValue());
            rule.success();
        }

        /**
         * Test expectation with actual value unset.
         */
        @Test
        public void expectStringMatcher() {
            Object value = CoreMatchers.startsWith("str");
            ExpectRule rule = new ExpectRule();
            rule.expect(value);
            rule.actual("string");
            Assert.assertThat(rule.expect(), CoreMatchers.equalTo(value));
            Assert.assertThat(rule.<String>actual(), CoreMatchers.equalTo("string"));
            Assert.assertThat(rule.toString(), CoreMatchers.notNullValue());
            rule.success();
        }

        /**
         * Test multiple exceptions with failure.
         */
        @Test
        public void expectMultipleWithFailures() {
            ExpectRule rule = new ExpectRule();
            rule.expect(Expect.Builder.create(":x:", Expect.Matcher.CONTAINS).build());
            rule.expect((Object) Expect.Builder.create(":y:", Expect.Matcher.CONTAINS));
            rule.expect((Object) Expect.Builder.create(":z:", Expect.Matcher.CONTAINS).build());
            rule.expect((Object) null);
            rule.failure(new IllegalArgumentException(":x:a:y:b:z:"));
            Assert.assertThat(rule.expect(), CoreMatchers.equalTo(null));
            Assert.assertThat(rule.toString(), CoreMatchers.notNullValue());
        }

        /**
         * Test multiple exceptions with success.
         */
        @Test(expected = AssertionError.class)
        @Expect(
            message = "expected test to throw (any exception with message <^.*:x:.*$> and "
                + "any exception with message <^.*:y:.*$> and any exception with message <^.*:z:.*$>)"
        )
        public void expectMultipleWithSuccess() {
            ExpectRule rule = new ExpectRule();
            rule.expect(Expect.Builder.create(":x:", Expect.Matcher.CONTAINS).build());
            rule.expect((Object) Expect.Builder.create(":y:", Expect.Matcher.CONTAINS));
            rule.expect((Object) Expect.Builder.create(":z:", Expect.Matcher.CONTAINS).build());
            rule.expect((Object) null);
            Assert.assertThat(rule.expect(), CoreMatchers.equalTo(null));
            rule.success();
        }

        /**
         * Test expect failure with failure.
         */
        @Test(expected = AssertionError.class)
        public void expectFailure() {
            ExpectRule rule = new ExpectRule();
            rule.expect(Expect.Builder.create(IllegalStateException.class).build());
            rule.failure(new IllegalArgumentException());
        }
    }
}
