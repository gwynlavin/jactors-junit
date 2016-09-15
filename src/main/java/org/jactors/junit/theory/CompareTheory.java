package org.jactors.junit.theory;

import java.io.Serializable;
import java.util.Comparator;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * Compare theory implementation that is able to check validity and consistency of compare
 * constraint for objects {@link Comparable#compareTo(Object)} or respectively for comparators
 * {@link java.util.Comparator#compare(Object, Object)}. If no compare operator instance is provided
 * to the compare theory, it is assumed that object instances implement {@link Comparable}. In
 * addition, it allows to check validity and consistency of the well defined object theory
 * constraints for equals and compare ({@link Object#equals(Object)}, and {@link Object#hashCode()},
 * as well as the special marker interfaces of {@link Serializable}. In addition, the object theory
 * checks the availability and consistency of {@link Object#toString()}, making the assumption that
 * the result should neither be null nor empty.
 *
 * @param <Type> object type.
 */
@Ignore
@RunWith(Theories.class)
public abstract class CompareTheory<Type> extends ObjectTheory {

    /**
     * Operator instance for comparing (comparator).
     */
    protected final Comparator<Type> operator;

    /**
     * Create default compare theory with all object theories enabled and default number of rounds
     * for consistency checks (10).
     */
    protected CompareTheory() {
        this(null);
    }

    /**
     * Create default compare theory for given operator instance for comparing with default number
     * of rounds for consistency checks (10). If no compare operator instance is provided, it is
     * assumed that object instances implement {@link Comparable}.
     *
     * @param operator operator instance for comparing (comparator - may be null).
     */
    protected CompareTheory(Comparator<Type> operator) {
        this(operator, CONSISTENCY_CHECKS);
    }

    /**
     * Create compare theory for given operator instance for comparing with given number of rounds
     * for consistency checks. If no compare operator instance is provided, it is assumed that
     * object instances implement {@link Comparable}.
     *
     * @param operator operator instance for comparing (comparator).
     * @param checks number of rounds for consistency checks.
     */
    protected CompareTheory(Comparator<Type> operator, int checks) {
        super(checks);
        this.operator = operator;
    }

    /**
     * Check whether compare operation is symmetric condition: for all {@code x} and {@code y},
     * {@code sgn(x.compareTo(y))} must be equal to {@code -sgn(y.compareTo(x))}. This implies that
     * {@code x.compareTo(y)} must throw an exception iff {@code y.compareTo(x)} throws an
     * exception.
     *
     * @param x primary object instance.
     * @param y secondary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void compareToIsSymmetric(Type x, Type y) {
        CompareTheory.Atom<Type> xToY = new CompareTheory.Atom<Type>("X-Y", this.operator, x, y);
        CompareTheory.Atom<Type> yToX = new CompareTheory.Atom<Type>("Y-X", this.operator, y, x);

        if (Helper.okay(xToY, yToX)) {
            Assert.assertThat(xToY.value, CoreMatchers.is(CoreMatchers.equalTo(-yToX.value)));
        } else {
            Assert.assertThat("only X threw an exception.", yToX.except,
                    CoreMatchers.is(CoreMatchers.not(CoreMatchers.equalTo(null))));
            Assert.assertThat("only Y threw an exception.", xToY.except,
                    CoreMatchers.is(CoreMatchers.not(CoreMatchers.equalTo(null))));
        }
    }

    /**
     * Check whether compare operation is transitive condition: for all {@code x}, {@code y}, and
     * {@code z}, {@code (x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0} implies
     * {@code x.compareTo(z)&gt;0}. Finally, {@code x.compareTo(y)==0} implies that <tt>
     * {@code sgn(x.compareTo(z)) == sgn(y.compareTo(z))}, for all {@code z}.</tt>
     *
     * @param x primary object instance.
     * @param y secondary object instance.
     * @param z tertiary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void compareToIsTransitive(Type x, Type y, Type z) {
        CompareTheory.Atom<Type> xToY = new CompareTheory.Atom<Type>("X-Y", this.operator, x, y);
        CompareTheory.Atom<Type> yToZ = new CompareTheory.Atom<Type>("Y-Z", this.operator, y, z);
        CompareTheory.Atom<Type> xToZ = new CompareTheory.Atom<Type>("Z-X", this.operator, x, z);

        if (Helper.okay(xToY, yToZ, xToZ)) {
            Assume.assumeTrue((xToY.value > 0) && (yToZ.value > 0));
            Assert.assertThat(xToZ.value, CoreMatchers.is(Matchers.greaterThan(0)));
        } else {
            Assert.assertThat(Helper.message("X", yToZ, xToZ), xToY,
                    CoreMatchers.is(CoreMatchers.not(CoreMatchers.equalTo(null))));
            Assert.assertThat(Helper.message("Y", xToY, xToZ), yToZ,
                    CoreMatchers.is(CoreMatchers.not(CoreMatchers.equalTo(null))));
            Assert.assertThat(Helper.message("Z", xToY, yToZ), xToZ,
                    CoreMatchers.is(CoreMatchers.not(CoreMatchers.equalTo(null))));
        }
    }

    /**
     * Check optional condition that compare operation is consistent to equals operation: It is
     * strongly recommended, but <i>not</i> strictly required that for all {@code x} and {@code y},
     * {@code (x.compareTo(y)==0) == (x.equals(y))}.
     *
     * @param x primary object instance.
     * @param y secondary object instance.
     */
    @Theory
    public final void compareToIsConsistentToEquals(Type x, Type y) {
        Assume.assumeNotNull(x);

        CompareTheory.Atom<Type> xToY = new CompareTheory.Atom<Type>("X", this.operator, x, y);

        Assume.assumeThat(xToY.except != null, CoreMatchers.is(CoreMatchers.equalTo(false)));
        Assume.assumeThat(xToY.value, CoreMatchers.is(CoreMatchers.equalTo(0)));

        Assert.assertThat(x.equals(y), CoreMatchers.is(CoreMatchers.equalTo(true)));
    }

    /**
     * Test atom that executes compare and stores the result or exception.
     *
     * @param <Type> test atom type.
     */
    private static final class Atom<Type> {

        /**
         * Label for reporting failures.
         */
        protected final String label;

        /**
         * Result comparing the .
         */
        protected int value;

        /**
         * The exception raised (if any) during the comparing of the UUTs.
         */
        protected Exception except;

        /**
         * Create test atom with given atom label, given comparator instance, given primary object
         * instance, and given secondary object instance. If no comparator instance is provided, it
         * is assumed that object instances implement {@link Comparable}.
         *
         * @param label atom label.
         * @param c compare operator instance (may be null).
         * @param x primary object instance.
         * @param y secondary object instance.
         */
        @SuppressWarnings("unchecked")
        protected Atom(String label, Comparator<Type> c, Type x, Type y) {
            this.label = label;
            try {
                if (c != null) {
                    this.value = c.compare(x, y);
                } else {
                    this.value = ((Comparable<Type>) x).compareTo(y);
                }
            } catch (Exception except) {
                this.except = except;
            }
        }
    }

    /**
     * Compare theory helper.
     */
    private static final class Helper {

        /**
         * Create error message with given error message prefix, primary test atom, and secondary
         * test atom.
         *
         * @param <Type> type of atom.
         * @param prefix error message prefix.
         * @param a primary test atom.
         * @param b secondary test atom.
         * @return error message.
         */
        protected static <Type> String message(String prefix, CompareTheory.Atom<Type> a,
                CompareTheory.Atom<Type> b) {
            return new StringBuilder(64).append(prefix)
                    .append(" did not throw an exception, while ").append(a.label).append(" did ")
                    .append((a.except == null) ? "not " : "").append(" and").append(b.label)
                    .append(" did ").append((b.except == null) ? "not " : "").toString();
        }

        /**
         * Check whether all given test atoms are free of exceptions.
         *
         * @param atoms list of test atoms.
         * @return whether test atoms are free of exceptions.
         */
        protected static boolean okay(final CompareTheory.Atom<?>... atoms) {
            for (CompareTheory.Atom<?> atom : atoms) {
                if (atom.except != null) {
                    return false;
                }
            }
            return true;
        }
    }
}
