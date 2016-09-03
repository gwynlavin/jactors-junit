package org.jactors.junit.rule;

import java.lang.reflect.Field;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.jactors.junit.Expect;
import org.jactors.junit.helper.AccessHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Expectation rule is an improvement of {@link org.junit.rules.ExpectedException} that allows to
 * define failure results by annotation. Besides the native annotation {@link Expect}, it evaluates
 * {@link org.junit.Test#expected()} (if possible) to setup exception expectations. The following
 * examples shows how the {@link ExpectRule} is used.
 */
public final class ExpectRule extends Expect.Rule implements TestRule {

    /**
     * Unset expectation value marker object.
     */
    public static final Object UNSET = new Object();

    /**
     * Flag whether rule should be activated always.
     */
    private final boolean always;

    /**
     * Expected value object (may be also {@link Expect} or {@link Matcher}).
     */
    protected Object expect = UNSET;

    /**
     * Actual value object.
     */
    protected Object actual = UNSET;

    /**
     * Create expect rule with on-demand activation. The rule is only activated, if a test is
     * annotated by {@link Expect}.
     */
    public ExpectRule() {
        this(false);
    }

    /**
     * Create expect rule with flag whether rule should be activated always. Otherwise, it is only
     * activated, if a test is annotated by {@link Expect}.
     *
     * @param  always  flag whether rule should be activated always.
     */
    public ExpectRule(boolean always) {
        this.always = always;
    }

    /**
     * Setup expected value object for testing. May be any value, but {@link Expect},
     * {@link Expect.Builder}, and {@link Matcher}) are handled different.
     *
     * @param   <Type>  actual value type.
     * @param   expect  expected value object.
     *
     * @return  expected value object.
     */
    @SuppressWarnings("unchecked")
    public <Type> Type expect(Type expect) {
        if (expect instanceof Expect.Builder) {
            expect = (Type) ((Expect.Builder) expect).build();
            this.expect((Expect) expect);
        } else if (expect instanceof Expect) {
            this.expect((Expect) expect);
        }
        return (Type) (this.expect = expect);
    }

    /**
     * Return expected value object.
     *
     * @param   <Type>  actual value type.
     *
     * @return  expected value object.
     */
    @SuppressWarnings("unchecked")
    public <Type> Type expect() {
        return (Type) this.expect;
    }

    /**
     * Setup actual value object for testing.
     *
     * @param   <Type>  actual value type.
     * @param   actual  actual value object.
     *
     * @return  actual value object.
     */
    @SuppressWarnings("unchecked")
    public <Type> Type actual(Type actual) {
        return (Type) (this.actual = actual);
    }

    /**
     * Return actual value object.
     *
     * @param   <Type>  actual value type.
     *
     * @return  actual value object.
     */
    @SuppressWarnings("unchecked")
    public <Type> Type actual() {
        return (Type) this.actual;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    protected void success() {
        super.success();
        if ((this.actual != UNSET) && (this.expect != UNSET)) {
            if ((this.expect != null) && (this.expect instanceof Matcher<?>)) {
                Assert.assertThat(this.actual, (Matcher<Object>) this.expect);
            } else if ((this.expect == null) || !(this.expect instanceof Expect)) {
                Assert.assertThat(this.actual, CoreMatchers.equalTo(this.expect));
            }
        } else if (this.actual != UNSET) {
            throw new AssertionError("missing value [expect]");
        }
    }

    /**
     * {@inheritDoc}
     */
    protected <Type extends Throwable> void failure(Type except) throws Type {
        super.failure(except);
    }

    /**
     * {@inheritDoc}
     */
    public Statement apply(Statement next, Description descr) {
        if (this.always || (descr.getAnnotation(Expect.class) != null)) {
            return new ExpectStatement(this, next, descr);
        }
        return next;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "ExpectRule[expect=" + (UNSET.equals(this.expect) ? "UNSET" : this.expect) //
            + ", actual=" + (UNSET.equals(this.actual) ? "UNSET" : this.actual) //
            + ", message=" + StringDescription.toString(this.throwing()) + "]";
    }

    /**
     * Helper class for cleaning up statement chain from exception trapping.
     */
    private abstract static class Helper {

        /**
         * Remove all statements extending given statement class type from statement chain with
         * given head statement.
         *
         * @param  chain  head of statement chain.
         * @param  type   class type of statement to remove.
         */
        protected static void remove(Statement chain, Class<? extends Statement> type) {
            Statement before = chain, next = AccessHelper.Fields.get(chain, Helper.field(chain));
            while (!(next instanceof InvokeMethod)) {
                if (type.isAssignableFrom(next.getClass())) {
                    next = AccessHelper.Fields.get(next, Helper.field(next));
                    AccessHelper.Fields.set(before, Helper.field(before), next);
                } else {
                    before = next;
                    next = AccessHelper.Fields.get(next, Helper.field(next));
                }
            }
        }

        /**
         * Resolve field containing next statement in statement chain.
         *
         * @param   head  head statement in statement chain.
         *
         * @return  field containing next statement.
         */
        private static Field field(Statement head) {
            List<Field> fields = AccessHelper.Fields.resolve(head.getClass(), Statement.class,
                    AccessHelper.Resolve.Type.EXACT);
            if (fields.size() != 1) { // should never happen!
                throw new IllegalStateException("could not resolve nested rule statement [type="
                    + head.getClass().getName() + ", value=" + head + "] (ambiguous)");
            }
            return fields.get(0);
        }
    }

    /**
     * Rule specific statement.
     */
    private static final class ExpectStatement extends Statement {

        /**
         * Expectation rule instance.
         */
        private final ExpectRule rule;

        /**
         * Next statement for delegation.
         */
        private final Statement next;

        /**
         * Create rule statement with given expectation rule instance, next statement for
         * delegation, and test case description.
         *
         * @param  rule   expectation rule instance.
         * @param  next   next statement for delegation.
         * @param  descr  test case description.
         */
        public ExpectStatement(ExpectRule rule, Statement next, Description descr) {
            this.rule = rule;
            this.next = next;
            rule.init(descr);
        }

        /**
         * {@inheritDoc}
         */
        public void evaluate() throws Throwable {
            Test test = this.rule.describe().getAnnotation(Test.class);
            Expect expect = this.rule.describe().getAnnotation(Expect.class);
            this.rule.expect(test, expect);
            if (test.expected() != Test.None.class) {
                Helper.remove(this, ExpectException.class);
            }

            try {
                this.next.evaluate();
                this.rule.success();
            } catch (Throwable except) {
                this.rule.failure(except);
            } finally {
                this.rule.matchers().clear();
                this.rule.expect = UNSET;
                this.rule.actual = UNSET;
            }
        }
    }
}
