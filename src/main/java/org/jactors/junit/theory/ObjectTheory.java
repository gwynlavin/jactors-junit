package org.jactors.junit.theory;

import java.io.IOException;
import java.io.Serializable;

import org.hamcrest.CoreMatchers;
import org.jactors.junit.helper.CloneHelper;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * Object theory implementation with focus on code coverage that is able to check validity and
 * consistency of the well defined constraints for equals and compare ({@link Object#equals(Object)}
 * , and {@link Object#hashCode()}, as well as the special marker interfaces of {@link Serializable}
 * . In addition, it checks the availability and consistency of {@link Object#toString()}, making
 * the assumption that the result should neither be null nor empty.
 * <p>
 * Currently, it is assumed that all object theory build on each other and do not need to adapted,
 * but need to be extended for usage.
 * </p>
 */
@Ignore
@RunWith(Theories.class)
public abstract class ObjectTheory {

    /**
     * Default null and object data point.
     */
    @DataPoints
    public static final Object[] DEFAULTS = {
        null, new Object()
    };

    /**
     * Default number of consistency check rounds.
     */
    protected static final int CONSISTENCY_CHECKS = 5;

    /**
     * Number of rounds for consistency checks.
     */
    protected final int checks;

    /**
     * Create default object theory with default number of rounds for consistency checks (10).
     */
    protected ObjectTheory() {
        this(CONSISTENCY_CHECKS);
    }

    /**
     * Create object theory with given number of rounds for consistency checks.
     *
     * @param checks number of rounds for consistency checks.
     */
    protected ObjectTheory(int checks) {
        this.checks = checks;
    }

    /**
     * Theory initialization test trick to provide test hook that shows exception causes in class
     * initialization and creation of data points.
     */
    @Test
    public final void initDataPoints() {
        Assert.assertTrue(true);
    }

    /**
     * Check object is same condition for {@link Object#equals(Object)}: for any none-null {@code x}
     * test that {@code x.equals(y)} where {@code x} and {@code y} are the same data point instance
     * works. User must provide data points that are similar but not the same. Test to cover initial
     * short cut exit return value in equals code.
     *
     * @param x primary object instance.
     * @param y secondary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void equalsIsSame(Object x, Object y) {
        Assume.assumeTrue(x == y);
        Assert.assertThat(x.equals(y), CoreMatchers.is(true));
    }

    /**
     * Check object is not same but equal condition for {@link Object#equals(Object)}: for any
     * none-null {@code x} test that {@code x.equals(y)} where {@code x} and {@code y} are the same
     * data point instance works. User must provide data points that are similar but not the same.
     * Test to cover final return value in equals code.
     *
     * @param x primary object instance.
     * @param y secondary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void equalsIsNotSameButEqual(Object x, Object y) {
        Assume.assumeTrue(x != y);
        Assume.assumeTrue(x.equals(y));
        Assert.assertThat(x.equals(y), CoreMatchers.is(true));
    }

    /**
     * Check {@link Object#equals(Object)} is reflexive condition: for any none-null reference value
     * {@code x}, {@code x.equals(x)} should return true. Test to cover initial same object equal
     * condition.
     *
     * @param x primary object instance.
     */
    @Theory(nullsAccepted = false)
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SA_LOCAL_SELF_COMPARISON")
    public final void equalsIsReflexive(Object x) {
        Assert.assertThat(x.equals(x), CoreMatchers.is(true));
    }

    /**
     * Check {@link Object#equals(Object)} is symmetric condition: for any none-null reference
     * values {@code x} and {@code y}, {@code x.equals(y)} should return true, if and only if
     * {@code y.equals(x)} returns true. Test to cover symmetry in equals conditions. User must
     * provide data points that are similar but not the same.
     *
     * @param x primary object instance.
     * @param y secondary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void equalsIsSymmetric(Object x, Object y) {
        Assume.assumeTrue(x != y);
        boolean same = x.equals(y);
        Assert.assertThat(y.equals(x), CoreMatchers.is(same));
    }

    /**
     * Check {@link Object#equals(Object)} is transitive condition: for any none-null reference
     * values x, y, and z, if x.equals(y) returns true and y.equals(z) returns true, then
     * x.equals(z) should return true.
     *
     * @param x primary object instance.
     * @param y secondary object instance.
     * @param z tertiary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void equalsIsTransitive(Object x, Object y, Object z) {
        Assume.assumeTrue(x.equals(y) && y.equals(z));
        Assert.assertThat(z.equals(x), CoreMatchers.is(true));
    }

    /**
     * Check {@link Object#equals(Object)} is consistent condition: for any none-null reference
     * values {@code x} and {@code y}, multiple invocations of {@code x.equals(y)} consistently
     * return true or consistently return false, provided no information used in equals comparisons
     * on the objects is modified.
     *
     * @param x primary object instance.
     * @param y secondary object instance.
     */
    @Theory
    public final void equalsIsConsistent(Object x, Object y) {
        Assume.assumeNotNull(x);
        boolean same = x.equals(y);
        for (int count = 0; count < this.checks; count++) {
            Assert.assertThat(x.equals(y), CoreMatchers.is(same));
        }
    }

    /**
     * Check {@link Object#equals(Object)} always returns false on null: for any non-null reference
     * value {@code x}, {@code x.equals(null)} should return false. Test to cover short cut exit for
     * checking equals against null object.
     *
     * @param x primary object instance.
     */
    @Theory(nullsAccepted = false)
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("EC_NULL_ARG")
    public final void equalsReturnFalseOnNull(Object x) {
        Assert.assertThat(x.equals(null), CoreMatchers.is(false));
    }

    /**
     * Check {@link Object#equals(Object)} always returns false on other type condition: for any
     * non-null reference value {@code x}, {@code x.equals(new Object())} should return
     * {@code false}. Test to cover short cut exit for checking equals against other object types
     * (here {@link Object}).
     *
     * @param x primary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void equalsReturnFalseOnOtherType(Object x) {
        Assert.assertThat(x.equals(new Object()), CoreMatchers.is(false));
    }

    /**
     * Check {@link Object#equals(Object)} is always consistent with itself condition: an object
     * without being changed in between always returns the same description about itself.
     *
     * @param x primary object instance.
     * @param y secondary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void equalsStaySame(Object x, Object y) {
        boolean equals = x.equals(y);

        for (int count = 0; count < this.checks; count++) {
            Assert.assertEquals(equals, x.equals(y));
        }
    }

    /**
     * Check {@link Object#hashCode()} is always consistent to itself condition: whenever it is
     * invoked on the same object more than once the {@link Object#hashCode()} method must
     * consistently return the same integer.
     *
     * @param x primary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void hashCodeIsConsistent(Object x) {
        int code = x.hashCode();
        for (int count = 0; count < this.checks; count++) {
            Assert.assertEquals(code, x.hashCode());
        }
    }

    /**
     * Check {@link Object#hashCode()} is always consistent with equals condition: if two objects
     * are equal according to {@link Object#equals(Object)} method, then calling
     * {@link Object#hashCode()} method on each of the two objects must produce the same integer
     * result. User must provide data points that are similar but not the same.
     *
     * @param x primary object instance.
     * @param y secondary object instance.
     */
    @Theory
    public final void hashCodeIsConsistentWithEquals(Object x, Object y) {
        Assume.assumeNotNull(x);
        Assume.assumeTrue(x.equals(y));
        Assert.assertThat(x.hashCode(), CoreMatchers.is(CoreMatchers.equalTo(y.hashCode())));
    }

    /**
     * Check {@link Object#hashCode()} is always consistent with itself condition: an object without
     * being changed in between always returns the same description about itself.
     *
     * @param x primary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void hashCodeStaySame(Object x) {
        int hashCode = x.hashCode();

        for (int count = 0; count < this.checks; count++) {
            Assert.assertEquals(hashCode, x.hashCode());
        }
    }

    /**
     * Check {@link Object#toString()} provides sufficient description condition:
     * {@link Object#toString()} never returns {@code null} or an empty string.
     *
     * @param x primary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void toStringNotNullOrEmpty(Object x) {
        String string = x.toString();
        Assert.assertNotNull(string);
        Assert.assertFalse(string.isEmpty());
    }

    /**
     * Check {@link Object#toString()} is always consistent with itself condition: an object without
     * being changed in between always returns the same description about itself.
     *
     * @param x primary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void toStringStaySame(Object x) {
        String string = x.toString();

        for (int count = 0; count < this.checks; count++) {
            Assert.assertEquals(string, x.toString());
        }
    }

    /**
     * Checks that all objects are survive a full round trip serialization.
     *
     * @param x primary object instance.
     * @throws IOException if any I/O problem occurs on the stream.
     * @throws ClassNotFoundException if class cannot be found.
     */
    @Theory(nullsAccepted = false)
    public final void isSerializable(Object x) throws IOException, ClassNotFoundException {
        if (x instanceof Serializable) {
            Object copy = CloneHelper.clone(x);
            Assert.assertThat(x.equals(copy), CoreMatchers.is(true));
        }
    }
}
