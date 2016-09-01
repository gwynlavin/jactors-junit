package org.jactors.junit.theory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * Object theory implementation with focus on code coverage that is able to check validity and
 * consistency of the well defined constraints for equals and compare ({@link Object#equals(Object)},
 * and {@link Object#hashCode()}, as well as the special marker interfaces of {@link Serializable}.
 * In addition, it checks the availability and consistency of {@link Object#toString()}, making the
 * assumption that the result should neither be null nor empty.
 *
 * <p>Currently, it is assumed that all object theory build on each other and do not need to
 * adapted, but need to be extended for usage.</p>
 */
@Ignore
@RunWith(Theories.class)
public abstract class ObjectTheory { // NOPMD: only extendible.

    /**
     * Default null data point.
     */
    @DataPoint
    public static final Object NULL = null;

    /**
     * Default foreign object data point.
     */
    @DataPoint
    public static final Object OBJECT = new Object();

    /**
     * Default number of consistency check rounds.
     */
    protected static final int CONSISTENCY_CHECKS = 10;

    /**
     * Whether object theory for equals is enabled.
     */
    protected final boolean equals;

    /**
     * Whether object theory for string is enabled.
     */
    protected final boolean string;

    /**
     * Number of rounds for consistency checks.
     */
    protected final int checks;

    /**
     * Create default object theory with all checks enabled and default number of rounds for
     * consistency checks (10).
     */
    protected ObjectTheory() {
        this(true, true, CONSISTENCY_CHECKS);
    }

    /**
     * Create object theory with given flags whether object theory for equals, hash code, and string
     * are enabled, as well as given number of rounds for consistency checks.
     *
     * @param  equals  flag whether object theory for equals is enabled.
     * @param  string  flag whether object theory for string is enabled.
     * @param  checks  number of rounds for consistency checks.
     */
    protected ObjectTheory(boolean equals, boolean string, int checks) {
        this.equals = equals;
        this.string = string;
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
     * @param  x  primary object instance.
     * @param  y  secondary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void equalsIsSame(Object x, Object y) {
        if (this.equals) {
            Assume.assumeNotNull(x);
            Assume.assumeTrue(x == y); // NOPMD: needed!
            Assert.assertThat(x.equals(y), CoreMatchers.is(true));
        }
    }

    /**
     * Check object is not same but equal condition for {@link Object#equals(Object)}: for any
     * none-null {@code x} test that {@code x.equals(y)} where {@code x} and {@code y} are the same
     * data point instance works. User must provide data points that are similar but not the same.
     * Test to cover final return value in equals code.
     *
     * @param  x  primary object instance.
     * @param  y  secondary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void equalsIsNotSameButEqual(Object x, Object y) {
        if (this.equals) {
            Assume.assumeNotNull(x);
            Assume.assumeNotNull(y);
            Assume.assumeTrue(x != y); // NOPMD: needed!
            Assume.assumeTrue(x.equals(y));
            Assert.assertThat(x.equals(y), CoreMatchers.is(true));
        }
    }

    /**
     * Check {@link Object#equals(Object)} is reflexive condition: for any none-null reference value
     * {@code x}, {@code x.equals(x)} should return true. Test to cover initial same object equal
     * condition.
     *
     * @param  x  primary object instance.
     */
    @Theory(nullsAccepted = false)
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SA_LOCAL_SELF_COMPARISON")
    public final void equalsIsReflexive(Object x) {
        if (this.equals) {
            Assume.assumeNotNull(x);
            Assert.assertThat(x.equals(x), CoreMatchers.is(true));
        }
    }

    /**
     * Check {@link Object#equals(Object)} is symmetric condition: for any none-null reference
     * values {@code x} and {@code y}, {@code x.equals(y)} should return true, if and only if
     * {@code y.equals(x)} returns true. Test to cover symmetry in equals conditions. User must
     * provide data points that are similar but not the same.
     *
     * @param  x  primary object instance.
     * @param  y  secondary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void equalsIsSymmetric(Object x, Object y) {
        if (this.equals) {
            Assume.assumeNotNull(x);
            Assume.assumeNotNull(y);
            Assume.assumeTrue(x != y); // NOPMD: needed!
            boolean same = x.equals(y);
            Assert.assertThat(y.equals(x), CoreMatchers.is(same));
        }
    }

    /**
     * Check {@link Object#equals(Object)} is transitive condition: for any none-null reference
     * values x, y, and z, if x.equals(y) returns true and y.equals(z) returns true, then
     * x.equals(z) should return true.
     *
     * @param  x  primary object instance.
     * @param  y  secondary object instance.
     * @param  z  tertiary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void equalsIsTransitive(Object x, Object y, Object z) {
        if (this.equals) {
            Assume.assumeNotNull(x);
            Assume.assumeNotNull(y);
            Assume.assumeNotNull(z);
            Assume.assumeTrue(x.equals(y) && y.equals(z));
            Assert.assertThat(z.equals(x), CoreMatchers.is(true));
        }
    }

    /**
     * Check {@link Object#equals(Object)} is consistent condition: for any none-null reference
     * values {@code x} and {@code y}, multiple invocations of {@code x.equals(y)} consistently
     * return true or consistently return false, provided no information used in equals comparisons
     * on the objects is modified.
     *
     * @param  x  primary object instance.
     * @param  y  secondary object instance.
     */
    @Theory
    public final void equalsIsConsistent(Object x, Object y) {
        if (this.equals) {
            Assume.assumeNotNull(x);
            boolean same = x.equals(y);
            for (int count = 0; count < this.checks; count++) {
                Assert.assertThat(x.equals(y), CoreMatchers.is(same));
            }
        }
    }

    /**
     * Check {@link Object#equals(Object)} always returns false on null: for any non-null reference
     * value {@code x}, {@code x.equals(null)} should return false. Test to cover short cut exit for
     * checking equals against null object.
     *
     * @param  x  primary object instance.
     */
    @Theory(nullsAccepted = false)
    @edu.umd.cs.findbugs.annotations.SuppressWarnings("EC_NULL_ARG")
    public final void equalsReturnFalseOnNull(Object x) {
        if (this.equals) {
            Assume.assumeNotNull(x);
            Assert.assertThat(x.equals(null), CoreMatchers.is(false)); // NOPMD: not possible!
        }
    }

    /**
     * Check {@link Object#equals(Object)} always returns false on other type condition: for any
     * non-null reference value {@code x}, {@code x.equals(new Object())} should return
     * {@code false}. Test to cover short cut exit for checking equals against other object types
     * (here {@link Object}).
     *
     * @param  x  primary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void equalsReturnFalseOnOtherType(Object x) {
        if (this.equals) {
            Assume.assumeNotNull(x);
            Assert.assertThat(x.equals(new Object()), CoreMatchers.is(false)); // NOPMD: not possible!
        }
    }

    /**
     * Check {@link Object#hashCode()} is always consistent to itself condition: whenever it is
     * invoked on the same object more than once the {@link Object#hashCode()} method must
     * consistently return the same integer.
     *
     * @param  x  primary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void hashCodeIsConsistent(Object x) {
        if (this.equals) {
            Assume.assumeNotNull(x);
            int code = x.hashCode();
            for (int count = 0; count < this.checks; count++) {
                Assert.assertEquals(code, x.hashCode());
            }
        }
    }

    /**
     * Check {@link Object#hashCode()} is always consistent with equals condition: if two objects
     * are equal according to {@link Object#equals(Object)} method, then calling
     * {@link Object#hashCode()} method on each of the two objects must produce the same integer
     * result. User must provide data points that are similar but not the same.
     *
     * @param  x  primary object instance.
     * @param  y  secondary object instance.
     */
    @Theory
    public final void hashCodeIsConsistentWithEquals(Object x, Object y) {
        if (this.equals) {
            Assume.assumeNotNull(x);
            Assume.assumeTrue(x.equals(y));
            Assert.assertThat(x.hashCode(), CoreMatchers.is(CoreMatchers.equalTo(y.hashCode())));
        }
    }

    /**
     * Check {@link Object#toString()} provides sufficient description condition:
     * {@link Object#toString()} never returns {@code null} or an empty string.
     *
     * @param  x  primary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void toStringNotNullOrEmpty(Object x) {
        if (this.string) {
            Assume.assumeNotNull(x);
            String string = x.toString();
            Assert.assertNotNull(string);
            Assert.assertFalse(string.isEmpty());
        }
    }

    /**
     * Check {@link Object#toString()} is always consistent with itself condition: an object without
     * being changed in between always returns the same description about itself.
     *
     * @param  x  primary object instance.
     */
    @Theory(nullsAccepted = false)
    public final void toStringConsistent(Object x) {
        if (this.string) {
            Assume.assumeNotNull(x);
            String string = x.toString();
            for (int count = 0; count < this.checks; count++) {
                Assert.assertEquals(string, x.toString());
            }
        }
    }

    /**
     * Checks that all objects are survive a full round trip serialization.
     *
     * @param   x  primary object instance.
     *
     * @throws  IOException             if any I/O problem occurs on the stream.
     * @throws  ClassNotFoundException  if class cannot be found.
     */
    @Theory(nullsAccepted = false)
    public final void isSerializable(Object x) throws IOException, ClassNotFoundException {
        Assume.assumeNotNull(x);
        if (x instanceof Serializable) {
            Object copy = Helper.clone(x);
            if (this.equals) {
                Assert.assertThat(x.equals(copy), CoreMatchers.is(true));
            }
        }
    }

    /**
     * Helper class to clone object via serialization.
     */
    private static final class Helper {

        /**
         * Create cloned object for given source object via simple serialization.
         *
         * @param   object  source object.
         *
         * @return  cloned object.
         *
         * @throws  IOException             if any I/O problem occurs on the stream.
         * @throws  ClassNotFoundException  if class cannot be found.
         */
        @SuppressWarnings("unchecked")
        public static <Type> Type clone(Type object) throws IOException, ClassNotFoundException {
            Output cout = null;
            Input cin = null;
            Queue<Class<?>> queue = new LinkedList<Class<?>>();
            try {
                ByteArrayOutputStream sout = new ByteArrayOutputStream();
                cout = new Output(sout, queue);
                cout.writeObject(object);
                ByteArrayInputStream sin = new ByteArrayInputStream(sout.toByteArray());
                cin = new Input(sin, queue);
                return (Type) cin.readObject();
            } finally {
                if (cout != null) {
                    try {
                        cout.close();
                    } catch (Exception except) {
                        // nothing to do!
                    }
                }
                if (cin != null) {
                    try {
                        cin.close();
                    } catch (Exception except) {
                        // nothing to do!
                    }
                }
            }
        }

        /**
         * Clone object output stream.
         */
        private static final class Output extends ObjectOutputStream {

            /**
             * Serialized class list queue.
             */
            private final Queue<Class<?>> queue;

            /**
             * Create clone object output stream with given delegating output stream and given
             * serialized class list queue.
             *
             * @param   out    delegating output stream.
             * @param   queue  serialized class list queue.
             *
             * @throws  IOException  if creation of object output stream failed.
             */
            protected Output(OutputStream out, Queue<Class<?>> queue) throws IOException {
                super(out);
                this.queue = queue;
            }

            /**
             * {@inheritDoc}
             */
            protected void annotateClass(Class<?> c) {
                this.queue.add(c);
            }

            /**
             * {@inheritDoc}
             */
            protected void annotateProxyClass(Class<?> c) {
                this.queue.add(c);
            }
        }

        /**
         * Clone input output stream.
         */
        private static final class Input extends ObjectInputStream {

            /**
             * Serialized class list queue.
             */
            private final Queue<Class<?>> queue;

            /**
             * Create clone object input stream with given delegating input stream and given
             * serialized class list queue.
             *
             * @param   in     delegating input stream.
             * @param   queue  serialized class list queue.
             *
             * @throws  IOException  if creation of object output stream failed.
             */
            protected Input(InputStream in, Queue<Class<?>> queue) throws IOException {
                super(in);
                this.queue = queue;
            }

            /**
             * {@inheritDoc}
             */
            protected Class<?> resolveClass(ObjectStreamClass oclazz) throws IOException, ClassNotFoundException {
                Class<?> clazz = this.queue.poll();
                String actual = (clazz == null) ? null : clazz.getName();
                if (!oclazz.getName().equals(actual)) {
                    throw new InvalidClassException("invalid class [expected=" + oclazz.getName() //
                        + ", actual=" + actual + "]");
                }
                return clazz;
            }

            /**
             * {@inheritDoc}
             */
            protected Class<?> resolveProxyClass(String[] ifaces) throws IOException, ClassNotFoundException {
                return this.queue.poll();
            }
        }
    }
}
