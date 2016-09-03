package org.jactors.junit;

import java.lang.annotation.Annotation;
import java.util.EnumSet;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.jactors.junit.Expect.Helper;
import org.jactors.junit.rule.ExpectRule;
import org.jactors.junit.test.ParameterTest;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;

/**
 * Expect test.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
    {
        ExpectTest.MatcherBehavior.class,
        ExpectTest.StringMatcherBehavior.class,
        ExpectTest.ExpectMatcherBehavior.class,
        ExpectTest.BuilderBehavior.class,
        ExpectTest.RuleBehavior.class
    }
)
public class ExpectTest implements EnumTest.Unknown {

    /**
     * JUnit test annotation mock.
     */
    private static final class JUnitTest implements Test {

        /**
         * Expected exception type.
         */
        private final Class<? extends Throwable> expected;

        /**
         * Create JUnit test annotation mock with given expected exception type.
         *
         * @param  expected  expected exception type.
         */
        public JUnitTest(Class<? extends Throwable> expected) {
            this.expected = expected;
        }

        /**
         * {@inheritDoc}
         */
        public Class<? extends Annotation> annotationType() {
            return Test.class;
        }

        /**
         * {@inheritDoc}
         */
        public Class<? extends Throwable> expected() {
            return this.expected;
        }

        /**
         * {@inheritDoc}
         */
        public long timeout() {
            return 0;
        }
    }

    /**
     * Check rule behavior.
     */
    @FixMethodOrder(MethodSorters.JVM)
    public static final class RuleBehavior {

        /**
         * Activate expectation rule.
         */
        @Rule
        public final ExpectRule expect = new ExpectRule(true);

        /**
         * Test rule behavior for no failure.
         */
        @Test
        public void expectNoFailure() {
            Expect.Rule rule = new Expect.Rule();
            rule.expect(new JUnitTest(Test.None.class), Expect.Builder.create().build());
            rule.success();
        }

        /**
         * Test rule behavior for no failure.
         */
        @Test
        public void expectNoFailureWithMultipleCauses() {
            Expect.Rule rule = new Expect.Rule();
            rule.expect(new JUnitTest(Test.None.class),
                Expect.Builder.create().cause((Class<Throwable>) null).cause((Class<Throwable>) null).build());
            rule.success();
        }

        /**
         * Test rule behavior for failure only defined in test.
         *
         * @throws Throwable  if test fails.
         */
        @Test
        public void expectOnlyTestFailure() throws Throwable {
            Expect.Rule rule = new Expect.Rule();
            rule.expect(new JUnitTest(RuntimeException.class), null);
            rule.failure(new IllegalArgumentException());
        }

        /**
         * Test rule behavior for contradicting failures defined in test and expect.
         *
         * @throws Throwable  if test fails.
         */
        @Test
        public void expectTypePrecedenceFailure() throws Throwable {
            Expect.Rule rule = new Expect.Rule();
            rule.expect(new JUnitTest(IllegalStateException.class),
                Expect.Builder.create(IllegalArgumentException.class).build());
            rule.failure(new IllegalArgumentException());
        }

        /**
         * Test rule behavior for handling success (no exception).
         */
        @Test
        public void handleSuccess() {
            new Expect.Rule().expect((Test) null, null).success();
        }

        /**
         * Test rule behavior for handling of unexpected exception.
         *
         * @throws Throwable  if test fails.
         */
        @Test(expected = IllegalArgumentException.class)
        @Expect(message = Expect.NULL)
        public void handleFailureNotExpected() throws Throwable {
            new Expect.Rule().failure(new IllegalArgumentException());
        }

        /**
         * test rule behavior for string information.
         */
        @Test
        public void checkToString() {
            Assert.assertThat(new Expect.Rule().toString(), CoreMatchers.equalTo("Expect.Rule[message=()]"));
        }
    }

    /**
     * Check expectation builder behavior.
     */
    @FixMethodOrder(MethodSorters.JVM)
    public static final class BuilderBehavior {

        /**
         * Activate expectation rule.
         */
        @Rule
        public final ExpectRule expect = new ExpectRule(true);

        /**
         * Test builder behavior join test and expect.
         */
        @Test
        public void joinTestAndExpect() {
            Class<? extends Throwable> type = IllegalAccessError.class;
            String message = "failure-";
            Expect.Matcher matcher = Expect.Matcher.STARTS_WITH;
            Expect expect = Expect.Builder.join(new JUnitTest(type), Expect.Builder.create(message, matcher).build());
            Assert.assertThat(expect.type(), CoreMatchers.<Object>equalTo(type));
            Assert.assertThat(expect.message(), CoreMatchers.equalTo(message));
            Assert.assertThat(expect.matcher(), CoreMatchers.equalTo(matcher));
        }

        /**
         * Test builder behavior create and expect with exception type.
         */
        @Test
        public void expectDefaultWithType() {
            Class<? extends Throwable> type = RuntimeException.class;
            Expect expect = Expect.Builder.create(type).build();
            Assert.assertThat(expect.type(), CoreMatchers.<Object>equalTo(type));
            Assert.assertThat(expect.message(), CoreMatchers.equalTo(Expect.UNAVAILABLE));
            Assert.assertThat(expect.matcher(), CoreMatchers.equalTo(Expect.Matcher.EQUALS));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException();
        }

        /**
         * Test builder behavior create and expect with exception type and failure message.
         */
        @Test
        public void expectDefaultWithTypeMessage() {
            String message = "failure";
            Class<? extends Throwable> type = RuntimeException.class;
            Expect expect = Expect.Builder.create(type, message).build();
            Assert.assertThat(expect.type(), CoreMatchers.<Object>equalTo(type));
            Assert.assertThat(expect.message(), CoreMatchers.equalTo(message));
            Assert.assertThat(expect.matcher(), CoreMatchers.equalTo(Expect.Matcher.EQUALS));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException(message);
        }

        /**
         * Test builder behavior create and expect with exception type, failure message, and message
         * matcher.
         */
        @Test
        public void expectDefaultWithTypeMessageMatcher() {
            Class<? extends Throwable> type = RuntimeException.class;
            String message = "failure-.*";
            Expect.Matcher matcher = Expect.Matcher.PATTERN;
            Expect expect = Expect.Builder.create(type, message, matcher).build();
            Assert.assertThat(expect.type(), CoreMatchers.<Object>equalTo(type));
            Assert.assertThat(expect.message(), CoreMatchers.equalTo(message));
            Assert.assertThat(expect.matcher(), CoreMatchers.equalTo(matcher));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException("failure-abc");
        }

        /**
         * Test builder behavior create and expect with failure message.
         */
        @Test
        public void expectDefaultWithMessage() {
            String message = "failure";
            Expect expect = Expect.Builder.create(message).build();
            Assert.assertThat(expect.type(), CoreMatchers.<Object>equalTo(Test.None.class));
            Assert.assertThat(expect.message(), CoreMatchers.equalTo(message));
            Assert.assertThat(expect.matcher(), CoreMatchers.equalTo(Expect.Matcher.EQUALS));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException("failure");
        }

        /**
         * Test builder behavior create and expect with failure message and message matcher.
         */
        @Test
        public void expectDefaultWithMessageMatcher() {
            String message = "failure-";
            Expect.Matcher matcher = Expect.Matcher.STARTS_WITH;
            Expect expect = Expect.Builder.create(message, matcher).build();
            Assert.assertThat(expect.toString(), CoreMatchers.notNullValue());
            Assert.assertThat(expect.type(), CoreMatchers.<Object>equalTo(Test.None.class));
            Assert.assertThat(expect.message(), CoreMatchers.equalTo(message));
            Assert.assertThat(expect.matcher(), CoreMatchers.equalTo(matcher));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException("failure-abc");
        }

        /**
         * Test builder behavior create and expect with null exception type.
         */
        @Test
        public void expectChangedWithNullExceptionIsNoException() {
            Expect expect =
                Expect.Builder.create(IllegalArgumentException.class) //
                .expect((Class<Throwable>) null).build();
            Assert.assertThat(expect.type(), CoreMatchers.<Object>equalTo(Test.None.class));
            Assert.assertThat(expect.message(), CoreMatchers.equalTo(Expect.UNAVAILABLE));
            Assert.assertThat(expect.matcher(), CoreMatchers.equalTo(Expect.Matcher.EQUALS));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(0));
        }

        /**
         * Test builder behavior create and expect with unavailable failure message.
         */
        @Test
        public void expectChangedWithUnavailableMessageIsNoFailure() {
            Expect expect =
                Expect.Builder.create(null, "message", Expect.Matcher.CONTAINS) //
                .expect(Expect.UNAVAILABLE).build();
            Assert.assertThat(expect.type(), CoreMatchers.<Object>equalTo(Test.None.class));
            Assert.assertThat(expect.message(), CoreMatchers.equalTo(Expect.UNAVAILABLE));
            Assert.assertThat(expect.matcher(), CoreMatchers.equalTo(Expect.Matcher.CONTAINS));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(0));
        }

        /**
         * Test builder behavior create and expect with null failure message is failure with null
         * message.
         */
        @Test
        public void expectChangedWithNullStringMessageIsFailureWithNullMessage() {
            Expect expect =
                Expect.Builder.create(null, "message", Expect.Matcher.CONTAINS) //
                .expect(Expect.NULL).build();
            Assert.assertThat(expect.type(), CoreMatchers.<Object>equalTo(Test.None.class));
            Assert.assertThat(expect.message(), CoreMatchers.nullValue());
            Assert.assertThat(expect.matcher(), CoreMatchers.equalTo(Expect.Matcher.EQUALS));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException();
        }

        /**
         * Test builder behavior create and expect with null failure message is failure with equals
         * matcher.
         */
        @Test
        public void expectChangedWithNullStringMessageIsFailureWithEquals() {
            Expect expect =
                Expect.Builder.create(null, "message", Expect.Matcher.CONTAINS) //
                .expect((String) null, null).build();
            Assert.assertThat(expect.type(), CoreMatchers.<Object>equalTo(Test.None.class));
            Assert.assertThat(expect.message(), CoreMatchers.nullValue());
            Assert.assertThat(expect.matcher(), CoreMatchers.equalTo(Expect.Matcher.EQUALS));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new IllegalArgumentException();
        }

        /**
         * Test builder behavior cause with exception type.
         */
        @Test
        public void causeDefaultWithTypeIsFailure() {
            Class<? extends Throwable> type = IllegalArgumentException.class;
            Expect expect = Expect.Builder.create().cause(type).build();
            Assert.assertThat(expect.cause()[0].type(), CoreMatchers.<Object>equalTo(type));
            Assert.assertThat(expect.cause()[0].message(), CoreMatchers.equalTo(Expect.UNAVAILABLE));
            Assert.assertThat(expect.cause()[0].matcher(), CoreMatchers.equalTo(Expect.Matcher.EQUALS));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException(new IllegalArgumentException());
        }

        /**
         * Test builder behavior cause with exception type and failure message.
         */
        @Test
        public void causeDefaultWithTypeMessageIsFailure() {
            Class<? extends Throwable> type = IllegalArgumentException.class;
            String message = "failure";
            Expect expect = Expect.Builder.create().cause(type, message).build();
            Assert.assertThat(expect.cause()[0].type(), CoreMatchers.<Object>equalTo(type));
            Assert.assertThat(expect.cause()[0].message(), CoreMatchers.equalTo(message));
            Assert.assertThat(expect.cause()[0].matcher(), CoreMatchers.equalTo(Expect.Matcher.EQUALS));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException(new IllegalArgumentException("failure"));
        }

        /**
         * Test builder behavior cause with exception type, failure message, and message matcher.
         */
        @Test
        public void causeDefaultWithTypeMessageMatcherIsFailure() {
            Class<? extends Throwable> type = IllegalArgumentException.class;
            String message = "-xyz";
            Expect.Matcher matcher = Expect.Matcher.ENDS_WITH;
            Expect expect = Expect.Builder.create().cause(type, message, matcher).build();
            Assert.assertThat(expect.cause()[0].type(), CoreMatchers.<Object>equalTo(type));
            Assert.assertThat(expect.cause()[0].message(), CoreMatchers.equalTo(message));
            Assert.assertThat(expect.cause()[0].matcher(), CoreMatchers.equalTo(matcher));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException(new IllegalArgumentException("failure-xyz"));
        }

        /**
         * Test builder behavior cause with failure message.
         */
        @Test
        public void causeDefaultWithMessageIsFailure() {
            String message = "failure";
            Expect expect = Expect.Builder.create().cause(message).build();
            Assert.assertThat(expect.cause()[0].type(), CoreMatchers.<Object>equalTo(Test.None.class));
            Assert.assertThat(expect.cause()[0].message(), CoreMatchers.equalTo(message));
            Assert.assertThat(expect.cause()[0].matcher(), CoreMatchers.equalTo(Expect.Matcher.EQUALS));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException(new IllegalArgumentException("failure"));
        }

        /**
         * Test builder behavior cause with failure message and message matcher.
         */
        @Test
        public void causeDefaultWithMessageMatcherIsFailure() {
            String message = "failure-.*";
            Expect.Matcher matcher = Expect.Matcher.PATTERN;
            Expect expect = Expect.Builder.create().cause(message, matcher).build();
            Assert.assertThat(expect.cause()[0].type(), CoreMatchers.<Object>equalTo(Test.None.class));
            Assert.assertThat(expect.cause()[0].message(), CoreMatchers.equalTo(message));
            Assert.assertThat(expect.cause()[0].matcher(), CoreMatchers.equalTo(matcher));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException(new IllegalArgumentException("failure-xyz"));
        }

        /**
         * Test builder behavior cause with failure message and message matcher.
         */
        @Test
        public void causeDefaultWithMultipleCauses() {
            Class<? extends Throwable> type0 = IllegalStateException.class;
            Class<? extends Throwable> type1 = IllegalArgumentException.class;
            String message0 = "failure0-.*", message1 = "failure1-";
            Expect.Matcher matcher0 = Expect.Matcher.PATTERN, matcher1 = Expect.Matcher.STARTS_WITH;
            Expect expect = //
                Expect.Builder.create().cause(type0, message0, matcher0).cause(type1, message1, matcher1).build();
            Assert.assertThat(expect.cause()[0].type(), CoreMatchers.<Object>equalTo(type0));
            Assert.assertThat(expect.cause()[0].message(), CoreMatchers.equalTo(message0));
            Assert.assertThat(expect.cause()[0].matcher(), CoreMatchers.equalTo(matcher0));
            Assert.assertThat(expect.cause()[1].type(), CoreMatchers.<Object>equalTo(type1));
            Assert.assertThat(expect.cause()[1].message(), CoreMatchers.equalTo(message1));
            Assert.assertThat(expect.cause()[1].matcher(), CoreMatchers.equalTo(matcher1));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(1));
            throw new RuntimeException(new IllegalStateException("failure0-xyz",
                    new IllegalArgumentException("failure1-xyz")));
        }

        /**
         * Test builder behavior is cleared.
         */
        @Test
        public void clearToNothing() {
            Class<? extends Throwable> type = RuntimeException.class;
            String message = "failure-.*";
            Expect.Matcher matcher = Expect.Matcher.PATTERN;
            Expect expect = Expect.Builder.create(type, message, matcher).cause(type, message, matcher).clear().build();
            Assert.assertThat(expect.type(), CoreMatchers.<Object>equalTo(Test.None.class));
            Assert.assertThat(expect.message(), CoreMatchers.equalTo(Expect.UNAVAILABLE));
            Assert.assertThat(expect.matcher(), CoreMatchers.equalTo(Expect.Matcher.EQUALS));
            this.expect.expect(expect);
            Assert.assertThat(this.expect.matchers().size(), CoreMatchers.is(0));
        }

        /**
         * Test builder behavior provides to string.
         */
        @Test
        public void testToString() {
            Class<? extends Throwable> type = RuntimeException.class;
            String message = "failure-.*";
            Expect.Matcher matcher = Expect.Matcher.PATTERN;
            Expect.Builder builder = Expect.Builder.create(type, message, matcher).cause(type, message, matcher);
            Assert.assertThat(builder.toString(),
                CoreMatchers.equalTo(
                    "Builder[expect=Expect[type=java.lang.RuntimeException, message=failure-.*, matcher=PATTERN, causes=[]]"
                    + ", causes=[Cause[type=java.lang.RuntimeException, message=failure-.*, matcher=PATTERN]]]"));
        }
    }

    /**
     * Check expectation matcher behavior.
     */
    @FixMethodOrder(MethodSorters.JVM)
    public static final class MatcherBehavior extends ParameterTest {

        /**
         * Actual message string.
         */
        private final String message;

        /**
         * Failure message matcher type.
         */
        private final Expect.Matcher matcher;

        /**
         * Compare expression pattern.
         */
        private final String pattern;

        /**
         * Create test with given actual message string, failure message matcher type, compare
         * expression pattern, expected test result/failure.
         *
         * @param  message  actual message string.
         * @param  matcher  failure message matcher type.
         * @param  pattern  compare expression pattern.
         * @param  result   expected test result/failure.
         */
        public MatcherBehavior(String message, Expect.Matcher matcher, String pattern, Object result) {
            super(result);
            this.matcher = matcher;
            this.message = message;
            this.pattern = pattern;
        }

        /**
         * Create test data.
         *
         * @return  test data.
         */
        @Parameterized.Parameters(name = "{index}: message={0}, matcher={1}, pattern={2}, result=...")
        public static Iterable<Object[]> data() {
            Builder builder = ParameterTest.builder();
            for (Expect.Matcher matcher : EnumSet.complementOf(EnumSet.of(MATCHER_UNKNOWN))) {
                builder.add(null, matcher, null, true);
                builder.add(null, matcher, Expect.NULL, true);
                builder.add(null, matcher, "x", false);
                builder.add("y", matcher, null, false);
                builder.add("y", matcher, Expect.NULL, false);
                builder.add("y", matcher, "x", false);
                builder.add("", matcher, "x", false);
            }
            builder.add("x", MATCHER_UNKNOWN, "x",
                Expect.Builder.create(IllegalArgumentException.class,
                    "matcher not supported [" + MATCHER_UNKNOWN + "]"));
            builder.add("x", Expect.Matcher.EQUALS, "x", true);
            builder.add("axb", Expect.Matcher.CONTAINS, "", true);
            builder.add("axb", Expect.Matcher.CONTAINS, "x", true);
            builder.add("axb", Expect.Matcher.STARTS_WITH, "", true);
            builder.add("axb", Expect.Matcher.STARTS_WITH, "x", false);
            builder.add("xb", Expect.Matcher.STARTS_WITH, "x", true);
            builder.add("axb", Expect.Matcher.ENDS_WITH, "", true);
            builder.add("axb", Expect.Matcher.ENDS_WITH, "x", false);
            builder.add("ax", Expect.Matcher.ENDS_WITH, "x", true);
            builder.add("xz", Expect.Matcher.PATTERN, "x.*z", true);
            builder.add("xaz", Expect.Matcher.PATTERN, "x.*z", true);
            builder.add("xabz", Expect.Matcher.PATTERN, "x.*z", true);
            builder.add("axz", Expect.Matcher.PATTERN, "x.*z", false);
            builder.add("xzb", Expect.Matcher.PATTERN, "x.*z", false);
            return builder;
        }

        /**
         * Test expectation matcher behavior.
         */
        @Test
        public void test() {
            this.rule.actual(this.matcher.match(this.pattern, this.message));
        }
    }

    /**
     * Check expectation helper match method behavior.
     */
    public static final class StringMatcherBehavior extends ParameterTest {

        /**
         * Actual message string.
         */
        private final String message;

        /**
         * Failure message matcher type.
         */
        private final Expect.Matcher matcher;

        /**
         * Compare expression pattern.
         */
        private final String pattern;

        /**
         * Create test with given actual message string, failure message matcher type, compare
         * expression pattern, and expected test result/failure.
         *
         * @param  message  actual message string.
         * @param  matcher  failure message matcher type.
         * @param  pattern  compare expression pattern.
         * @param  result   expected test result/failure.
         */
        public StringMatcherBehavior(String message, Expect.Matcher matcher, String pattern, Object result) {
            super(result);
            this.message = message;
            this.matcher = matcher;
            this.pattern = pattern;
        }

        /**
         * Create test data.
         *
         * @return  test data.
         */
        @Parameterized.Parameters(name = "{index}: message={0}, matcher={1}, pattern={2}, result=...")
        public static Iterable<Object[]> data() {
            Builder builder = ParameterTest.builder();
            builder.add("x", MATCHER_UNKNOWN, "x",
                Expect.Builder.create(IllegalArgumentException.class,
                    "matcher not supported [" + MATCHER_UNKNOWN + "]"));

            for (Expect.Matcher matcher : EnumSet.complementOf(EnumSet.of(MATCHER_UNKNOWN))) {
                StringDescription expect = new StringDescription();
                Helper.describe(expect, "x", matcher);
                String format = "%s\nbut: was %s";
                builder.add(null, matcher, null, "null");
                builder.add(null, matcher, Expect.NULL, "null");
                builder.add(null, matcher, "x",
                    Expect.Builder.create(AssertionError.class, String.format(format, expect, "null")));
                builder.add("y", matcher, null,
                    Expect.Builder.create(AssertionError.class, String.format(format, "null", "\"y\"")));
                builder.add("y", matcher, Expect.NULL,
                    Expect.Builder.create(AssertionError.class, String.format(format, "null", "\"y\"")));
                builder.add("y", matcher, "x",
                    Expect.Builder.create(AssertionError.class, String.format(format, expect, "\"y\"")));
                builder.add("", matcher, "x",
                    Expect.Builder.create(AssertionError.class, String.format(format, expect, "\"\"")));
            }

            String format = "%s\nbut: was %s";
            builder.add("x", Expect.Matcher.EQUALS, "x", "\"x\"");
            builder.add("axb", Expect.Matcher.CONTAINS, "", "<^.*.*$>");
            builder.add("axb", Expect.Matcher.CONTAINS, "x", "<^.*x.*$>");
            builder.add("axb", Expect.Matcher.STARTS_WITH, "", "<^.*>");
            builder.add("axb", Expect.Matcher.STARTS_WITH, "x",
                Expect.Builder.create(AssertionError.class, String.format(format, "<^x.*>", "\"axb\"")));
            builder.add("xb", Expect.Matcher.STARTS_WITH, "x", "<^x.*>");
            builder.add("axb", Expect.Matcher.ENDS_WITH, "", "<.*$>");
            builder.add("axb", Expect.Matcher.ENDS_WITH, "x",
                Expect.Builder.create(AssertionError.class, String.format(format, "<.*x$>", "\"axb\"")));
            builder.add("ax", Expect.Matcher.ENDS_WITH, "x", "<.*x$>");
            builder.add("xz", Expect.Matcher.PATTERN, "x.*z", "<x.*z>");
            builder.add("xaz", Expect.Matcher.PATTERN, "x.*z", "<x.*z>");
            builder.add("xabz", Expect.Matcher.PATTERN, "x.*z", "<x.*z>");
            builder.add("axz", Expect.Matcher.PATTERN, "x.*z",
                Expect.Builder.create(AssertionError.class, String.format(format, "<x.*z>", "\"axz\"")));
            builder.add("xzb", Expect.Matcher.PATTERN, "x.*z",
                Expect.Builder.create(AssertionError.class, String.format(format, "<x.*z>", "\"xzb\"")));
            return builder;
        }

        /**
         * Test string matcher behavior.
         */
        @Test
        public void test() {
            Matcher<String> matcher = Expect.Helper.message(this.pattern, this.matcher);
            Assert.assertThat(matcher.toString(), CoreMatchers.notNullValue());
            StringDescription description = new StringDescription();
            if (!matcher.matches(this.message)) {
                description.appendDescriptionOf(matcher).appendText("\nbut: ");
                matcher.describeMismatch(this.message, description);
                throw new AssertionError(description.toString());
            }
            matcher.describeTo(description);
            this.rule.actual(description.toString());
        }
    }

    /**
     * Check expectation helper match method behavior.
     */
    public static final class ExpectMatcherBehavior extends ParameterTest {

        /**
         * Expectation to define expect matcher.
         */
        private final Expect expect;

        /**
         * Object item value.
         */
        private final Object item;

        /**
         * Create expect matcher behavior test with given expectation to define expect matcher, and
         * given object item value, and expected test result/failure.
         *
         * @param  expect  expectation to define expect matcher.
         * @param  item    object item value.
         * @param  result  expected test result/failure.
         */
        public ExpectMatcherBehavior(Expect expect, Object item, Object result) {
            super(result);
            this.expect = expect;
            this.item = item;
        }

        /**
         * Create test data.
         *
         * @return  test data.
         */
        @Parameterized.Parameters(name = "{index}: builder={0}, except={1}, result=...")
        public static Iterable<Object[]> data() {
            Builder builder = ParameterTest.builder();
            builder.add(Expect.Builder.create().build(), null,
                Expect.Builder.create(AssertionError.class, "(?s).*any exception\n" + ".*but: was null",
                    Expect.Matcher.PATTERN));
            builder.add(Expect.Builder.create().build(), new Long(0),
                Expect.Builder.create(AssertionError.class,
                    "(?s).*any exception\n" + ".*but: was instance-of <java.lang.Long> <0L>", Expect.Matcher.PATTERN));
            builder.add(Expect.Builder.create().expect(RuntimeException.class, "runtime").build(), new Throwable(),
                Expect.Builder.create(AssertionError.class,
                    "(?s).*exception <java.lang.RuntimeException> with message \"runtime\"\n"
                    + ".*but: was exception <java.lang.Throwable> with message null", Expect.Matcher.PATTERN));
            builder.add(Expect.Builder.create().expect(RuntimeException.class, "runtime").build(),
                new RuntimeException(),
                Expect.Builder.create(AssertionError.class,
                    "(?s).*exception <java.lang.RuntimeException> with message \"runtime\"\n"
                    + ".*but: was exception <java.lang.RuntimeException> with message null", Expect.Matcher.PATTERN));
            builder.add(Expect.Builder.create().expect(RuntimeException.class, "runtime") //
                .cause(IllegalArgumentException.class, "argument").cause(IllegalStateException.class).build(),
                new RuntimeException("runtime", new IllegalStateException("state")),
                Expect.Builder.create(AssertionError.class,
                    "(?s).*exception <java.lang.RuntimeException> with message \"runtime\"\n"
                    + ".*caused by <java.lang.IllegalArgumentException> with message \"argument\"\n"
                    + ".*caused by <java.lang.IllegalStateException>\n"
                    + ".*but: was exception <java.lang.RuntimeException> with message \"runtime\"\n"
                    + ".*caused by <java.lang.IllegalStateException> with message \"state\"", Expect.Matcher.PATTERN));
            builder.add(Expect.Builder.create().expect(RuntimeException.class, "runtime") //
                .cause(IllegalArgumentException.class, "argument").cause(IllegalStateException.class, "state").build(),
                new RuntimeException("runtime",
                    new IllegalArgumentException("argument", new IllegalStateException("state"))),
                Expect.Helper.message(
                    "(?s).*exception <java.lang.RuntimeException> with message \"runtime\"\n"
                    + ".*caused by <java.lang.IllegalArgumentException> with message \"argument\"\n"
                    + ".*caused by <java.lang.IllegalStateException> with message \"state\"", Expect.Matcher.PATTERN));
            return builder;
        }

        /**
         * Test expect matcher behavior.
         */
        @Test
        public void test() {
            Matcher<Throwable> matcher = Expect.Helper.expect(this.expect);
            Assert.assertThat(matcher.toString(), CoreMatchers.notNullValue());
            StringDescription description = new StringDescription();
            if (!matcher.matches(this.item)) {
                description.appendDescriptionOf(matcher).appendText("\nbut: ");
                matcher.describeMismatch(this.item, description);
                throw new AssertionError(description.toString());
            }
            matcher.describeTo(description);
            super.rule.actual(description.toString());
        }
    }
}
