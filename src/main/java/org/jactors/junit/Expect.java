package org.jactors.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Factory;
import org.hamcrest.StringDescription;
import org.jactors.junit.rule.BaseRule;
import org.junit.Assert;
import org.junit.Test;
import org.junit.matchers.JUnitMatchers;

/**
 * Expected exception failure annotation used by rules and theories to express custom exception
 * failure expectations.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.FIELD })
public @interface Expect {

    /**
     * Default null message result.
     */
    public static final String NULL = "<%null%>";

    /**
     * Default empty message result.
     */
    public static final String UNAVAILABLE = "<%message not available%>";

    /**
     * Failure message matcher type.
     */
    public static enum Matcher {

        /**
         * Matcher operation type for equals.
         */
        EQUALS,

        /**
         * Matcher operation type for contains.
         */
        CONTAINS,

        /**
         * Matcher operation type for starts with.
         */
        STARTS_WITH,

        /**
         * Matcher operation type for ends with.
         */
        ENDS_WITH,

        /**
         * MAtcher operation type for regular expression patterns.
         */
        PATTERN;

        /**
         * Check whether given compare expression pattern matches using given actual message string.
         *
         * @param   pattern  compare expression pattern.
         * @param   message  actual message string.
         *
         * @return  whether given compare expression pattern matches given actual message string.
         */
        public boolean match(String pattern, String message) {
            return Matcher.match(this, pattern, message);
        }

        /**
         * Check whether given matcher operation type with given compare expression pattern matches
         * given actual message string.
         *
         * @param   matcher  matcher operation type.
         * @param   pattern  compare expression pattern.
         * @param   message  actual message string.
         *
         * @return  whether given compare expression pattern matches given actual message string.
         */
        private static boolean match(Matcher matcher, String pattern, String message) {
            if (Expect.NULL.equals(pattern)) {
                pattern = null;
            }
            if ((message == null) && (pattern == null)) {
                return true;
            } else if (message == null) {
                return false;
            } else if (pattern == null) {
                return false;
            }
            switch (matcher) {
                case EQUALS:
                    return message.equals(pattern);

                case CONTAINS:
                    return message.contains(pattern);

                case STARTS_WITH:
                    return message.startsWith(pattern);

                case ENDS_WITH:
                    return message.endsWith(pattern);

                case PATTERN:
                    return message.matches(pattern);

                default:
                    throw new IllegalArgumentException("matcher not supported [" + matcher + "]");
            }
        }
    }

    /**
     * Failure exception class type.
     */
    public Class<? extends Throwable> type() default Test.None.class;

    /**
     * Failure message.
     */
    public String message() default UNAVAILABLE;

    /**
     * Failure message matcher type.
     */
    public Matcher matcher() default Matcher.EQUALS;

    /**
     * Failure message.
     */
    public Cause[] cause() default {};

    /**
     * Abstract expected exception rule.
     */
    public static class Rule extends BaseRule {

        /**
         * Message constant to introduce expectation.
         */
        private static final String EXPECTED_MESSAGE = "expected test to throw ";

        /**
         * List of expectations matchers.
         */
        private final List<org.hamcrest.Matcher<?>> matchers =
            new ArrayList<org.hamcrest.Matcher<?>>();

        /**
         * Insert given expectation matchers defined by {@link Expect} and return expected exception
         * rule for further setup. Use {@link Expect.Builder} to create new expected exception
         * failure.
         *
         * @param   expect  expected exception failure.
         *
         * @return  expected exception rule for further setup.
         */
        public final Rule expect(Expect expect) {
            if ((expect != null) && Helper.matches(expect)) {
                return this.expect(Helper.expect(expect));
            }
            return this;
        }

        /**
         * Insert expectation matchers defined by given expected exception failure ({@link Expect})
         * as well as JUnit test annotation ({@link Test}) and return expected exception rule for
         * further setup.
         *
         * @param   test    JUnit test annotation.
         * @param   expect  expected exception failure.
         *
         * @return  expected exception rule for further setup.
         */
        public final Rule expect(Test test, Expect expect) {
            if ((test != null) && (test.expected() != Test.None.class)) {
                if (expect == null) {
                    return this.expect(CoreMatchers.instanceOf(test.expected()));
                } else if (expect.type() == Test.None.class) {
                    return this.expect(Builder.join(test, expect));
                }
            }
            return this.expect(expect);
        }

        /**
         * Insert given expectation matcher to list of expectations and return expected exception
         * rule for further setup.
         *
         * @param   matcher  expectation matcher.
         *
         * @return  expected exception rule for further setup.
         */
        private final Rule expect(org.hamcrest.Matcher<?> matcher) {
            this.matchers.add(matcher);
            return this;
        }

        /**
         * Handle expectation in response to given exception after execution, and return whether
         * original exception should be re-thrown.
         *
         * @param   <Type>  exception type.
         * @param   except  exception instance.
         *
         * @throws  Type  assertion error or original exception (if re-thrown).
         */
        protected <Type extends Throwable> void failure(Type except) throws Type {
            if (this.matchers.isEmpty()) {
                throw except;
            }
            Assert.assertThat(except, this.throwing());
        }

        /**
         * Handle expectation in response to successful execution. If an exception was expected, an
         * assertion error is thrown.
         */
        protected void success() {
            if (!this.matchers.isEmpty()) {
                throw new AssertionError(EXPECTED_MESSAGE + StringDescription.toString(this.throwing()));
            }
        }

        /**
         * Return list of expectation matchers.
         *
         * @return  list of expectation matchers.
         */
        public List<org.hamcrest.Matcher<?>> matchers() {
            return this.matchers;
        }

        /**
         * Return expected exception defined by match builder.
         *
         * @return  expected exception defined by match builder.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        protected org.hamcrest.Matcher<Throwable> throwing() {
            if (this.matchers.size() == 1) {
                return JUnitMatchers.isThrowable((org.hamcrest.Matcher<Throwable>) this.matchers.get(0));
            }
            return JUnitMatchers.isThrowable(CoreMatchers.allOf(
                        new ArrayList<org.hamcrest.Matcher<? super Throwable>>((List) this.matchers)));
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "Expect.Rule[message=" + StringDescription.toString(this.throwing()) + "]";
        }
    }

    /**
     * Expectation helper.
     */
    public static final class Helper {

        /**
         * Create failure message matcher with given matcher operation type and compare expression
         * pattern.
         *
         * @param   pattern  compare expression pattern.
         * @param   matcher  matcher operation type.
         *
         * @return  string matcher.
         */
        protected static org.hamcrest.Matcher<String> message(String pattern, Matcher matcher) {
            return ToStringMatcher.create(pattern, matcher);
        }

        /**
         * Create general expectation based exception matcher with given expected exception failure.
         *
         * @param   <Type>  exception type.
         * @param   expect  expected exception failure.
         *
         * @return  general expectation based exception matcher.
         */
        protected static <Type extends Throwable> org.hamcrest.Matcher<Type> expect(Expect expect) {
            return ExpectMatcher.create(expect);
        }

        /**
         * Check whether given expected exception failure matches any exception.
         *
         * @param   expect  expected exception failure.
         *
         * @return  whether given expected exception failure matches any exception.
         */
        protected static boolean matches(Expect expect) {
            if (Helper.matches(expect.type(), expect.message())) {
                return true;
            }
            for (Cause cause : expect.cause()) {
                if (Helper.matches(cause.type(), cause.message())) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Check whether given expected failure exception class type and given expected failure
         * message matches any distinct exception.
         *
         * @param   type     expected failure exception class type.
         * @param   message  expected failure message.
         *
         * @return  whether any distinct exception is matched.
         */
        protected static boolean matches(Class<? extends Throwable> type, String message) {
            return (type != Test.None.class) || !UNAVAILABLE.equals(message);
        }

        /**
         * Check whether given expected failure exception class type and given expected failure
         * message using given failure message matcher type matches the given distinct exception.
         *
         * @param   except   distinct exception.
         * @param   type     expected failure exception class type.
         * @param   message  expected failure message.
         * @param   matcher  failure message matcher type.
         *
         * @return  whether given distinct exception is matched.
         */
        protected static boolean matches(Throwable except, Class<? extends Throwable> type, String message,
                Matcher matcher) {
            if ((type == null) || ((type != Test.None.class) && !type.isAssignableFrom(except.getClass()))) {
                return false;
            } else if (!Expect.UNAVAILABLE.equals(message)
                    && !matcher.match((!Expect.NULL.equals(message)) ? message : null, except.getMessage())) {
                return false;
            }
            return true;
        }

        /**
         * Append given expected failure message pattern using given failure message matcher type to
         * given matcher description.
         *
         * @param  description  matcher description.
         * @param  pattern      expected failure message pattern.
         * @param  matcher      failure message matcher type.
         */
        protected static void describe(org.hamcrest.Description description, String pattern, Expect.Matcher matcher) {
            if ((pattern == null) || Expect.NULL.equals(pattern)) {
                description.appendText("null");
                return;
            }
            switch (matcher) {
                case EQUALS:
                    description.appendValue(pattern);
                    break;

                case CONTAINS:
                    description.appendText("<^.*").appendText(pattern).appendText(".*$>");
                    break;

                case STARTS_WITH:
                    description.appendText("<^").appendText(pattern).appendText(".*>");
                    break;

                case ENDS_WITH:
                    description.appendText("<.*").appendText(pattern).appendText("$>");
                    break;

                case PATTERN:
                    description.appendText("<").appendText(pattern).appendText(">");
                    break;

                default:
                    throw new IllegalArgumentException("matcher not supported [" + matcher + "]");
            }
        }

        /**
         * String matcher that allows to define use different dynamic matching strategies including
         * equals, contains, starts with, ends with, and regular expressions.
         */
        private static final class ToStringMatcher extends BaseMatcher<String> {

            /**
             * String matcher type.
             */
            private final Expect.Matcher matcher;

            /**
             * Compare string pattern.
             */
            private final String pattern;

            /**
             * Create string matcher with given matcher operation type and compare expression
             * pattern.
             *
             * @param  matcher  matcher operation type.
             * @param  pattern  compare expression pattern.
             */
            private ToStringMatcher(Expect.Matcher matcher, String pattern) {
                this.matcher = matcher;
                this.pattern = pattern;
            }

            /**
             * Create string matcher with given matcher operation type and compare expression
             * pattern.
             *
             * @param   matcher  matcher operation type.
             * @param   pattern  compare expression pattern.
             *
             * @return  string matcher.
             */
            @Factory
            public static ToStringMatcher create(String pattern, Expect.Matcher matcher) {
                return new ToStringMatcher(matcher, pattern);
            }

            /**
             * {@inheritDoc}
             */
            public boolean matches(Object item) {
                return this.matcher.match(this.pattern, (item == null) ? null : item.toString());
            }

            /**
             * {@inheritDoc}
             */
            public void describeTo(org.hamcrest.Description description) {
                Helper.describe(description, this.pattern, this.matcher);
            }

            /**
             * {@inheritDoc}
             */
            public String toString() {
                return "StringMatcher[matcher=" + this.matcher + ", pattern=" + this.pattern + "]";
            }
        }

        /**
         * General expect based exception matcher supporting detailed exception root causes.
         *
         * @param  <Type>  exception type.
         */
        private static final class ExpectMatcher<Type extends Throwable> extends BaseMatcher<Type> {

            /**
             * Expected exception failure.
             */
            private final Expect expect;

            /**
             * Actual mismatch position.
             */
            private Throwable except;

            /**
             * Create general expectation based exception matcher with given expected exception
             * failure.
             *
             * @param  expect  expected exception failure.
             */
            private ExpectMatcher(Expect expect) {
                this.expect = expect;
            }

            /**
             * Create general expectation based exception matcher with given expected exception
             * failure.
             *
             * @param   <Type>  type exception to throw.
             * @param   expect  expected exception failure.
             *
             * @return  general expectation based exception matcher.
             */
            @Factory
            public static <Type extends Throwable> org.hamcrest.Matcher<Type> create(Expect expect) {
                return new ExpectMatcher<Type>(expect);
            }

            /**
             * {@inheritDoc}
             */
            public boolean matches(Object item) {
                if (!(item instanceof Throwable)) {
                    return false;
                }
                Throwable except = (Throwable) item;
                if (!Helper.matches(except, this.expect.type(), this.expect.message(), this.expect.matcher())) {
                    this.except = except;
                    return false;
                }
                for (Cause cause : this.expect.cause()) {
                    except = except.getCause();
                    if (!Helper.matches(except, cause.type(), cause.message(), cause.matcher())) {
                        this.except = except;
                        return false;
                    }
                }
                return true;
            }

            /**
             * {@inheritDoc}
             */
            public void describeMismatch(Object item, org.hamcrest.Description description) {
                if (item == null) {
                    description.appendText("was null");
                    return;
                } else if (!(item instanceof Throwable)) {
                    description.appendText("was instance-of <").appendText(item.getClass().getName()).appendText("> ")
                        .appendValue(item);
                    return;
                }
                Throwable except = (Throwable) item;
                description.appendText("was exception <").appendText(item.getClass().getName()).appendText(
                    "> with message ").appendValue(except.getMessage());
                for (Throwable cause = except.getCause(); cause != null; cause = cause.getCause()) {
                    description.appendText("\n    caused by <").appendText(cause.getClass().getName()).appendText(
                        "> with message ").appendValue(cause.getMessage());
                }
            }

            /**
             * Append given expected failure message pattern using given failure message matcher
             * type to given matcher description.
             *
             * @param  description  matcher description.
             * @param  pattern      expected failure message pattern.
             * @param  matcher      failure message matcher type.
             */
            private void describeTo(org.hamcrest.Description description, String pattern, Expect.Matcher matcher) {
                if (Expect.UNAVAILABLE.equals(pattern)) {
                    return;
                }
                description.appendText(" with message ");
                Helper.describe(description, pattern, matcher);
            }

            /**
             * {@inheritDoc}
             */
            public void describeTo(org.hamcrest.Description description) {
                if (this.expect.type() != Test.None.class) {
                    description.appendText("exception <").appendText(this.expect.type().getName()).appendText(">");
                } else {
                    description.appendText("any exception");
                }
                this.describeTo(description, this.expect.message(), this.expect.matcher());
                for (Cause cause : this.expect.cause()) {
                    description.appendText("\n    caused by <").appendText(cause.type().getName()).appendText(">");
                    this.describeTo(description, cause.message(), cause.matcher());
                }
                if (this.except != null) {
                    description.appendText("\n    mismatch in (").appendValue(this.except).appendText(")");
                }
            }

            /**
             * {@inheritDoc}
             */
            public String toString() {
                return "ExpectMatcher[expect=" + this.expect + "]";
            }
        }
    }

    /**
     * Expected exception configuration builder.
     */
    public static final class Builder {

        /**
         * Expected exception.
         */
        private Expect expect =
            new Expected(Test.None.class, UNAVAILABLE, Matcher.EQUALS, new Cause[] {});

        /**
         * List of expected exception cause.
         */
        private final List<Cause> causes = new ArrayList<Cause>();

        /**
         * Join JUnit test annotation with given expected exception annotation into new expected
         * exception annotation value.
         *
         * @param   test    JUnit test annotation value.
         * @param   expect  expected exception annotation value.
         *
         * @return  joined expected exception annotation value.
         */
        protected static Expect join(Test test, Expect expect) {
            return new Expected(test.expected(), expect.message(), expect.matcher(), expect.cause());
        }

        /**
         * Create expected exception builder with default settings.
         *
         * @return  expected exception builder.
         */
        public static Builder create() {
            return new Builder();
        }

        /**
         * Create expected exception builder with given expected failure exception class type
         * without checking for failure message pattern. Note: missing expectations will set to
         * defaults.
         *
         * @param   type  expected failure exception class type.
         *
         * @return  expected exception builder.
         */
        public static Builder create(Class<? extends Throwable> type) {
            return new Builder().expect(type);
        }

        /**
         * Create expected exception builder with given expected failure exception class type and
         * expected failure message pattern. Note: missing expectations will set to defaults.
         *
         * @param   type     expected failure exception class type.
         * @param   message  expected failure message pattern.
         *
         * @return  expected exception builder.
         */
        public static Builder create(Class<? extends Throwable> type, String message) {
            return new Builder().expect(type, message);
        }

        /**
         * Create expected exception builder with given expected failure exception class type and
         * expected failure message pattern using failure message matcher type. Note: missing
         * expectations will set to defaults.
         *
         * @param   type     expected failure exception class type.
         * @param   message  expected failure message pattern.
         * @param   matcher  failure message matcher type.
         *
         * @return  expected exception builder.
         */
        public static Builder create(Class<? extends Throwable> type, String message, Matcher matcher) {
            return new Builder().expect(type, message, matcher);
        }

        /**
         * Create expected exception builder with given expected failure message pattern. Note:
         * missing expectations will set to defaults.
         *
         * @param   message  expected failure message pattern.
         *
         * @return  expected exception builder.
         */
        public static Builder create(String message) {
            return new Builder().expect(message);
        }

        /**
         * Create expected exception builder with given expected failure message pattern using
         * failure message matcher type. Note: missing expectations will set to defaults.
         *
         * @param   message  expected failure message pattern.
         * @param   matcher  failure message matcher type.
         *
         * @return  expected exception builder.
         */
        public static Builder create(String message, Matcher matcher) {
            return new Builder().expect(message, matcher);
        }

        /**
         * Expect exception with given failure exception class type without checking for failure
         * message. Note: missing expectations stay unchanged.
         *
         * @param   type  expected failure exception class type.
         *
         * @return  same expected exception builder for further setup.
         */
        public Builder expect(Class<? extends Throwable> type) {
            this.expect = new Expected(type, this.expect.message(), this.expect.matcher(), this.expect.cause());
            return this;
        }

        /**
         * Expect exception with given failure exception class type and failure message pattern.
         * Note: missing expectations stay unchanged.
         *
         * @param   type     expected failure exception class type.
         * @param   message  expected failure message pattern.
         *
         * @return  same expected exception builder for further setup.
         */
        public Builder expect(Class<? extends Throwable> type, String message) {
            this.expect = new Expected(type, message, this.expect.matcher(), this.expect.cause());
            return this;
        }

        /**
         * Expect exception with given failure exception class type and failure message pattern
         * using given failure message matcher type. Note: missing expectations stay unchanged.
         *
         * @param   type     expected failure exception class type.
         * @param   message  expected failure message pattern.
         * @param   matcher  failure message matcher type.
         *
         * @return  same expected exception builder for further setup.
         */
        public Builder expect(Class<? extends Throwable> type, String message, Matcher matcher) {
            this.expect = new Expected(type, message, matcher, this.expect.cause());
            return this;
        }

        /**
         * Expect exception with given failure message pattern. Note: missing expectations stay
         * unchanged.
         *
         * @param   message  expected failure message pattern.
         *
         * @return  same expected exception builder for further setup.
         */
        public Builder expect(String message) {
            this.expect = new Expected(this.expect.type(), message, this.expect.matcher(), this.expect.cause());
            return this;
        }

        /**
         * Expect exception with given failure message pattern using given failure message matcher
         * type. Note: missing expectations stay unchanged.
         *
         * @param   message  expected failure message pattern.
         * @param   matcher  failure message matcher type.
         *
         * @return  same expected exception builder for further setup.
         */
        public Builder expect(String message, Matcher matcher) {
            this.expect = new Expected(this.expect.type(), message, matcher, this.expect.cause());
            return this;
        }

        /**
         * Expect exception cause with given failure exception class type without checking for
         * failure message. Note: declaring a cause this way always appends a new cause to the list
         * of causes.
         *
         * @param   type  expected failure exception class type.
         *
         * @return  same expected exception builder for further setup.
         */
        public Builder cause(Class<? extends Throwable> type) {
            this.causes.add(new Caused(type, UNAVAILABLE, Matcher.EQUALS));
            return this;
        }

        /**
         * Expect exception cause with given failure exception class type and failure message
         * pattern. Note: declaring a cause this way always appends a new cause to the list of
         * causes.
         *
         * @param   type     expected failure exception class type.
         * @param   message  expected failure message pattern.
         *
         * @return  same expected exception builder for further setup.
         */
        public Builder cause(Class<? extends Throwable> type, String message) {
            this.causes.add(new Caused(type, message, Matcher.EQUALS));
            return this;
        }

        /**
         * Expect exception cause with given failure exception class type and failure message
         * pattern using given failure message matcher type. Note: declaring a cause this way always
         * appends a new cause to the list of causes.
         *
         * @param   type     expected failure exception class type.
         * @param   message  expected failure message pattern.
         * @param   matcher  failure message matcher type.
         *
         * @return  same expected exception builder for further setup.
         */
        public Builder cause(Class<? extends Throwable> type, String message, Matcher matcher) {
            this.causes.add(new Caused(type, message, matcher));
            return this;
        }

        /**
         * Expect exception cause with given failure message pattern. Note: declaring a cause this
         * way always appends a new cause to the list of causes.
         *
         * @param   message  expected failure message pattern.
         *
         * @return  same expected exception builder for further setup.
         */
        public Builder cause(String message) {
            this.causes.add(new Caused(Test.None.class, message, Matcher.EQUALS));
            return this;
        }

        /**
         * Expect exception cause with given failure message pattern using given failure message
         * matcher type. Note: declaring a cause this way always appends a new cause to the list of
         * causes.
         *
         * @param   message  expected failure message pattern.
         * @param   matcher  failure message matcher type.
         *
         * @return  same expected exception builder for further setup.
         */
        public Builder cause(String message, Matcher matcher) {
            this.causes.add(new Caused(Test.None.class, message, matcher));
            return this;
        }

        /**
         * Clear expected exception builder. Removes all expectations, i.e. list of expected
         * exception causes as well as expected exception itself.
         *
         * @return  same expected exception builder for further setup.
         */
        public Builder clear() {
            this.expect = new Expected(Test.None.class, UNAVAILABLE, Matcher.EQUALS, new Cause[] {});
            this.causes.clear();
            return this;
        }

        /**
         * Return expected exception configuration.
         *
         * @return  expected exception configuration.
         */
        public Expect build() {
            Cause[] causes = this.causes.toArray(new Cause[this.causes.size()]);
            return new Expected(this.expect, causes);
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "Builder[expect=" + this.expect + ", causes=" + this.causes + "]";
        }

        /**
         * Base expected exception information.
         */
        private abstract static class Config implements Cause {

            /**
             * Expected failure exception class type.
             */
            private final Class<? extends Throwable> type;

            /**
             * Expected failure message pattern.
             */
            private final String message;

            /**
             * Failure message matcher type.
             */
            private final Matcher matcher;

            /**
             * Create base expected exception information with given expected exception class type,
             * expected failure message, and failure message matcher type.
             *
             * @param  type     expected exception class type.
             * @param  message  expected failure message pattern.
             * @param  matcher  failure message matcher type.
             */
            public Config(Class<? extends Throwable> type, String message, Matcher matcher) {
                this.type = (type == null) ? Test.None.class : type;
                this.message = (Expect.NULL.equals(message)) ? null : message;
                this.matcher = ((matcher == null) || (this.message == null)) ? Matcher.EQUALS : matcher;
            }

            /**
             * {@inheritDoc}
             */
            public Class<? extends Throwable> type() {
                return this.type;
            }

            /**
             * {@inheritDoc}
             */
            public String message() {
                return this.message;
            }

            /**
             * {@inheritDoc}
             */
            public Expect.Matcher matcher() {
                return this.matcher;
            }

            /**
             * {@inheritDoc}
             */
            public String toString() {
                return "type=" + this.type.getName() + ", message=" + this.message + ", matcher=" + this.matcher;
            }
        }

        /**
         * Expected exception information.
         */
        private static final class Expected extends Config implements Expect {

            /**
             * List of expected exception causes.
             */
            private final Cause[] causes;

            /**
             * Create expected exception information based on given expected exception information
             * and given list of expected exception causes.
             *
             * @param  expect  expected exception class type.
             * @param  causes  list of expected exception causes.
             */
            public Expected(Expect expect, Cause[] causes) {
                super(expect.type(), expect.message(), expect.matcher());
                this.causes = causes;
            }

            /**
             * Create expected exception information with given expected exception class type,
             * expected failure message pattern, and failure message matcher type, and list of
             * expected exception causes.
             *
             * @param  type     expected exception class type.
             * @param  message  expected failure message pattern.
             * @param  matcher  failure message matcher type.
             * @param  causes   list of expected exception causes.
             */
            public Expected(Class<? extends Throwable> type, String message, Matcher matcher, Cause[] causes) {
                super(type, message, matcher);
                this.causes = causes;
            }

            /**
             * {@inheritDoc}
             */
            public Class<Expect> annotationType() {
                return Expect.class;
            }

            /**
             * {@inheritDoc}
             */
            public Cause[] cause() {
                return this.causes;
            }

            /**
             * {@inheritDoc}
             */
            public String toString() {
                return "Expect[" + super.toString() + ", causes=" + Arrays.toString(this.causes) + "]";
            }
        }

        /**
         * Expected exception cause information.
         */
        private static final class Caused extends Config implements Cause {

            /**
             * Create expected exception cause information with given expected exception class type,
             * expected failure message pattern, and failure message matcher type.
             *
             * @param  type     expected exception class type.
             * @param  message  expected failure message pattern.
             * @param  matcher  failure message matcher type.
             */
            public Caused(Class<? extends Throwable> type, String message, Matcher matcher) {
                super(type, message, matcher);
            }

            /**
             * {@inheritDoc}
             */
            public Class<Cause> annotationType() {
                return Cause.class;
            }

            /**
             * {@inheritDoc}
             */
            public String toString() {
                return "Cause[" + super.toString() + "]";
            }
        }
    }

    /**
     * Expected exception cause annotation.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD, ElementType.FIELD })
    public @interface Cause {

        /**
         * Failure exception class type.
         */
        public Class<? extends Throwable> type() default Test.None.class;

        /**
         * Failure message.
         */
        public String message() default UNAVAILABLE;

        /**
         * Failure message matcher type.
         */
        public Matcher matcher() default Matcher.EQUALS;
    }
}
