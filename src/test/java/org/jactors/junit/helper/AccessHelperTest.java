package org.jactors.junit.helper; // NOPMD: test suite

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.hamcrest.CoreMatchers;
import org.jactors.junit.Expect;
import org.jactors.junit.Property;
import org.jactors.junit.rule.ExpectRule;
import org.jactors.junit.test.ParameterTest;
import org.jactors.junit.theory.BeanTheory;
import org.jactors.junit.theory.ObjectTheory;
import org.junit.After;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;


/**
 * Access helper test suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
    {
        AccessHelperTest.FailureBehavior.class,
        AccessHelperTest.FieldsBehavior.class,
        AccessHelperTest.MethodsBehavior.class,
        AccessHelperTest.ClassesBehavior.class,
        AccessHelperTest.ObjectsBehavior.class,
        AccessHelperTest.EnumsBehavior.class,
        AccessHelperTest.BeansBehavior.class,
    }
)
public class AccessHelperTest { // NOPMD: test suite

    /**
     * Base class name.
     */
    private static final String NAME_BASE = //
        "org.jactors.junit.helper.AccessHelperTest$Base";

    /**
     * Helper class name.
     */
    private static final String NAME_HELPER = //
        "org.jactors.junit.helper.AccessHelper";

    /**
     * Name for value.
     */
    protected static final String NAME_VALUE = "value";

    /**
     * Field for value.
     */
    protected static final Field FIELD_VALUE = //
        AccessHelper.Fields.resolve(Base.class, NAME_VALUE);

    /**
     * Getter for value.
     */
    protected static final Method GETTER_VALUE = //
        AccessHelper.Methods.resolve(Base.class, NAME_VALUE, new Class[] {});

    /**
     * Setter for value.
     */
    protected static final Method SETTER_VALUE = //
        AccessHelper.Methods.resolve(Base.class, NAME_VALUE, new Class[] { long.class });

    /**
     * Unknown enum types for testing.
     */
    protected static interface Unknown {

        /**
         * Unknown enumeration element name.
         */
        public static final String NAME_UNKNOWN = "UNKNOWN";

        /**
         * Unknown failure mode.
         */
        public static final AccessHelper.Failure.Mode MODE_FAILURE_UNKNOWN = //
            AccessHelper.Enums.insert(AccessHelper.Failure.Mode.class, NAME_UNKNOWN, -1, null);

        /**
         * Unknown bean property resolution mode.
         */
        public static final AccessHelper.Beans.Mode MODE_BEAN_UNKNOWN = //
            AccessHelper.Enums.insert(AccessHelper.Beans.Mode.class, NAME_UNKNOWN, -1, null);

        /**
         * Unknown resolution type.
         */
        public static final AccessHelper.Resolve.Type TYPE_RESOLVE_UNKNOWN = //
            AccessHelper.Enums.insert(AccessHelper.Resolve.Type.class, NAME_UNKNOWN, -1, null);
    }

    /**
     * Base bean getter/setter test class.
     */
    @Anno(1L)
    protected static class Base implements Serializable, Comparable<Base> {

        /**
         * Serial version unique identifier.
         */
        private static final long serialVersionUID = 8529691362050845398L;

        /**
         * Primitive long value.
         */
        private long value; // NOPMD: personal style.

        /**
         * Primitive integer value.
         */
        private int integer;

        /**
         * Create base value from given base value.
         *
         * @param  base  vase value..
         */
        public Base(Base base) {
            this(base.value, base.integer);
        }

        /**
         * Create initialized base value from given value string.
         *
         * @param  value  value string.
         */
        public Base(String value) {
            String[] values = (value != null) ? value.split(",") : new String[] {};
            this.value = (values.length > 0) ? Long.parseLong(values[0]) : 0;
            this.integer = (values.length > 1) ? Integer.parseInt(values[1]) : 0;
        }

        /**
         * Create initialized base value with given long value and integer value.
         *
         * @param  value    long value.
         * @param  integer  integer value.
         */
        public Base(long value, int integer) {
            if (value == Long.MIN_VALUE) {
                throw new IllegalArgumentException("not allowed [" + value + "]");
            }
            this.value = value;
            this.integer = integer;
        }

        /**
         * None-conforming setter for long value.
         *
         * @param  value  long value.
         */
        public void value(long value) {
            if (value == Long.MIN_VALUE) {
                throw new IllegalArgumentException("not allowed [" + value + "]");
            }
            this.value = value;
        }

        /**
         * None-conforming getter for long value.
         *
         * @return  long value.
         */
        public long value() {
            if (this.value == Long.MIN_VALUE) {
                throw new IllegalArgumentException("not allowed [" + this.value + "]");
            }
            return this.value;
        }

        /**
         * Getter for long value.
         *
         * @return  long value.
         */
        protected long getValues() {
            return this.value;
        }

        /**
         * None-conforming setter for long value with return value.
         *
         * @param   value  long value.
         *
         * @return  same long value.
         */
        protected long setValues(long value) {
            return this.value = value;
        }

        /**
         * Getter for integer value.
         *
         * @return  integer value
         */
        protected int getInteger() {
            return this.integer;
        }

        /**
         * Setter for integer value.
         *
         * @param  integer  integer value.
         */
        protected void setInteger(int integer) {
            this.integer = integer;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + this.integer;
            result = (prime * result) + (int) (this.value ^ (this.value >>> 32));
            return result;
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (this.getClass() != obj.getClass()) {
                return false;
            }
            Base other = (Base) obj;
            if (this.integer != other.integer) {
                return false;
            } else if (this.value != other.value) {
                return false;
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(Base obj) {
            if (this.value == obj.value) {
                if (this.integer == obj.integer) {
                    return 0;
                } else if (this.integer > obj.integer) {
                    return 1;
                }
                return -1;
            } else if (this.value > obj.value) {
                return 1;
            }
            return -1;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "Base[value=" + this.value + ", integer=" + this.integer + "]";
        }
    }

    /**
     * Iterable bean getter/setter test class.
     */
    @Anno(-1L)
    protected static class Child extends Base implements Iterable<Base> {

        /**
         * Serial version unique identifier.
         */
        private static final long serialVersionUID = 8631033346992669281L;

        /**
         * Primitive long value.
         */
        protected Long value; // NOPMD: personal style.

        /**
         * Other long value.
         */
        private final long other;

        /**
         * Original base value.
         */
        private final Base base;

        /**
         * Base value list.
         */
        private List<Base> list;

        /**
         * Base value array.
         */
        private Base[] array; // NOPMD: personal style!

        /**
         * Create child from base value.
         *
         * @param  base  base value.
         */
        public Child(Base base) {
            super(base);
            if (base instanceof Child) {
                Child child = (Child) base;
                this.value = child.value;
                this.other = child.other;
                this.base = child.base;
                this.list = new ArrayList<Base>(child.list);
                this.array = child.array;
            } else {
                this.value = base.getValues();
                this.other = base.getInteger();
                this.base = base;
                this.list = new ArrayList<Base>();
                this.list.add(base);
                this.array = new Base[] { base };
            }
        }

        /**
         * Create child value with given long value, other long value, integer value, and base value
         * array.
         *
         * @param  value    long value.
         * @param  other    other long value.
         * @param  integer  integer value.
         * @param  base     base value array.
         */
        public Child(long value, long other, int integer, Base... base) {
            super(value, integer);
            this.value = (value > 0) ? value : null;
            this.other = other;
            this.base = new Base(value, integer);
            this.list = (base != null) ? new ArrayList<Base>(Arrays.asList(base)) : null;
        }

        /**
         * {@inheritDoc}
         */
        public void value(long value) {
            this.value = (value > 0) ? value : null;
        }

        /**
         * {@inheritDoc}
         */
        public long value() {
            return this.value;
        }

        /**
         * Getter for long value.
         *
         * @return  long value.
         */
        public Long get() {
            return this.value;
        }

        /**
         * Setter for long value.
         *
         * @param  value  long value.
         */
        public void set(Long value) {
            this.value = value;
        }

        /**
         * Getter for long value.
         *
         * @return  long value.
         */
        public long getValues() {
            return this.value;
        }

        /**
         * Getter for other value.
         *
         * @return  other value.
         */
        public long getOther() {
            return this.other;
        }

        /**
         * Getter for array value.
         *
         * @return  array value.
         */
        public boolean hasArray() {
            return this.array != null;
        }

        /**
         * Setter for array value.
         *
         * @param  array  array value.
         */
        public void setHasArray(boolean array) {
            if (array) {
                if (this.array == null) {
                    this.array = new Base[] { this.base };
                }
            } else {
                this.array = null;
            }
        }

        /**
         * Getter for empty value.
         *
         * @return  empty value.
         */
        public boolean isEmpty() {
            return (this.list == null) || this.list.isEmpty();
        }

        /**
         * Setter for empty value.
         *
         * @param  empty  empty value.
         */
        public void setIsEmpty(boolean empty) {
            if (empty) {
                this.list.clear();
            } else {
                this.list.add(this.base);
            }
        }

        /**
         * Getter for base value list.
         *
         * @return  base value list.
         */
        public List<Base> getList() {
            return this.list;
        }

        /**
         * Unconventional setter for base value list.
         *
         * @param   base  base value list.
         *
         * @return  previous base value list.
         */
        public List<Base> setList(List<Base> base) {
            List<Base> list = this.list;
            this.list = base;
            return list;
        }

        /**
         * Unconventional setter for base value array.
         *
         * @param   base  base value array.
         *
         * @return  previous base value array.
         */
        public Base[] array(Base... base) {
            Base[] array = this.array;
            this.array = base;
            return array;
        }

        /**
         * Getter for base value array.
         *
         * @return  base value array.
         */
        public Base[] array() {
            return this.array;
        }

        /**
         * {@inheritDoc}
         */
        public Iterator<Base> iterator() {
            return this.list.iterator();
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "Child[value=" + this.value + "/" + super.getValues() + ", integer=" + this.getInteger() //
                + ", other=" + this.other + ", base=" + this.base + ", list=" + this.list + "]";
        }
    }

    /**
     * Check fields access helper behavior.
     */
    @RunWith(Suite.class)
    @Suite.SuiteClasses(
        {
            TestBehavior.BaseBeanTheory.class,
            TestBehavior.BaseObjectTheory.class,
            TestBehavior.BaseCompareTheory.class,
            TestBehavior.ChildBeanTheory.class
        }
    )
    public static final class TestBehavior {

        /**
         * Check base object bean theory.
         */
        @RunWith(Theories.class)
        public static final class BaseBeanTheory extends BeanTheory {

            /**
             * Base object for bean theory testing.
             */
            @DataPoint
            public static final Base BASE = new Base(0, 0);
        }

        /**
         * Check base object theory.
         */
        @RunWith(Theories.class)
        public static final class BaseObjectTheory extends ObjectTheory {

            /**
             * Default base object for object theory testing.
             */
            @DataPoint
            public static final Base DEFAULT = new Base(0, 0);

            /**
             * Base object for object theory testing.
             */
            @DataPoints
            public static final Base[] BASE = new Base[] { new Base(1, 0), new Base(1, 1) };
        }

        /**
         * Check base object theory.
         */
        @RunWith(Theories.class)
        public static final class BaseCompareTheory
            extends org.jactors.junit.theory.CompareTheory<Base> {

            /**
             * Default base object for object theory testing.
             */
            @DataPoint
            public static final Base DEFAULT = new Base(0, 0);

            /**
             * Base object for object theory testing.
             */
            @DataPoints
            public static final Base[] BASE = new Base[] { new Base(1, 0), new Base(1, 1) };
        }

        /**
         * Check child object bean theory.
         */
        @RunWith(Theories.class)
        public static final class ChildBeanTheory extends BeanTheory {

            /**
             * Child object for bean theory testing.
             */
            @DataPoint
            public static final Child CHILD = new Child(1, 2, 3, new Base(0, 0));

            /**
             * Alternative base property.
             */
            @Property
            public static final Base base = new Base(1, 1);
        }
    }

    /**
     * Check failure building.
     */
    @FixMethodOrder(MethodSorters.JVM)
    @RunWith(BlockJUnit4ClassRunner.class)
    public static final class FailureBehavior implements Unknown {

        /**
         * Activate expectation rule.
         */
        @Rule
        public final ExpectRule expect = new ExpectRule();

        /**
         * Test failure with null type.
         */
        @Test(expected = IllegalArgumentException.class)
        @Expect(message = "type must not be null")
        public void createWithNullType() {
            AccessHelper.Failure.create(null, "not allowed");
        }

        /**
         * Test failure with null message.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "invocation target failure")
        public void createWithNullMessage() {
            throw AccessHelper.Failure.create(AccessHelper.Failure.Type.TARGET, null);
        }

        /**
         * Test failure output with security exception.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "security failure",
            cause = @Expect.Cause(type = SecurityException.class)
        )
        public void createWithSecurityException() {
            throw AccessHelper.Failure.create((String) null, new SecurityException());
        }

        /**
         * Test failure output with index out of bounds exception.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "index out of bounds failure",
            cause = @Expect.Cause(type = ArrayIndexOutOfBoundsException.class)
        )
        public void createWithIndexOutOfBoundException() {
            throw AccessHelper.Failure.create((String) null, new ArrayIndexOutOfBoundsException(5));
        }

        /**
         * Test failure output with unsupported operation exception.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "support failure",
            cause = @Expect.Cause(type = UnsupportedOperationException.class)
        )
        public void createWithNotSupportedException() {
            throw AccessHelper.Failure.create((String) null, new UnsupportedOperationException());
        }

        /**
         * Test failure output with unknown exception cause.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "unknown failure",
            cause = @Expect.Cause(type = Throwable.class)
        )
        public void createWithUnknownCause() {
            throw AccessHelper.Failure.create((String) null, new Throwable());
        }

        /**
         * Test failure output with unknown exception cause.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "index out of bounds failure",
            cause = @Expect.Cause(type = ArrayIndexOutOfBoundsException.class)
        )
        public void wrapFailureUnwrapped() {
            throw AccessHelper.Failure.create(AccessHelper.Failure.Style.UNWRAPPED, "unwrapped message",
                AccessHelper.Failure.create((String) null, new ArrayIndexOutOfBoundsException(5)));
        }

        /**
         * Test failure output with wrapped failure using merged.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "index out of bounds failure [merged message]",
            cause = @Expect.Cause(type = ArrayIndexOutOfBoundsException.class)
        )
        public void wrapFailureMerged() {
            throw AccessHelper.Failure.create(AccessHelper.Failure.Style.MERGED, "[merged message]",
                AccessHelper.Failure.create((String) null, new ArrayIndexOutOfBoundsException(5)));
        }

        /**
         * Test failure target resolution without cause.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "failure wrapper",
            cause = @Expect.Cause
        )
        public void extractTargetForNullCause() {
            throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS, "failure wrapper") //
            .getTarget(RuntimeException.class);
        }

        /**
         * Test failure target resolution without invocation target exception cause.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "failure wrapper",
            cause = @Expect.Cause(type = IllegalArgumentException.class)
        )
        public void extractTargetForNoneTargetCause() {
            throw AccessHelper.Failure.create("failure wrapper", new IllegalArgumentException()) //
            .getTarget(RuntimeException.class);
        }

        /**
         * Test failure target resolution with invocation target exception cause.
         */
        @Test(expected = IllegalArgumentException.class)
        @Expect(message = "failed argument")
        public void extractTargetForTargetCause() {
            throw AccessHelper.Failure.create("does not matter",
                new InvocationTargetException(new IllegalArgumentException("failed argument"))) //
            .getTarget(RuntimeException.class);
        }

        /**
         * Test failure target resolution with wrapped invocation target exception cause.
         */
        @Test(expected = IllegalArgumentException.class)
        @Expect(message = "failed argument")
        public void extractTargetForWrappedTargetCause() {
            throw AccessHelper.Failure.create("does not matter",
                AccessHelper.Failure.create("does not matter",
                    new InvocationTargetException(new IllegalArgumentException("failed argument")))) //
            .getTarget(RuntimeException.class);
        }

        /**
         * Test failure root cause resolution without cause.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "failure wrapper",
            cause = @Expect.Cause
        )
        public void extractRootWithNullCause() {
            throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS, "failure wrapper") //
            .getRoot(RuntimeException.class);
        }

        /**
         * Test failure root cause resolution with root cause.
         */
        @Test(expected = IllegalArgumentException.class)
        @Expect(message = "failed argument")
        public void extractRootWithCause() {
            throw AccessHelper.Failure.create("does not matter", new IllegalArgumentException("failed argument")) //
            .getRoot(RuntimeException.class);
        }

        /**
         * Test failure root cause resolution with wrapped root cause.
         */
        @Test(expected = IllegalArgumentException.class)
        @Expect(message = "failed argument")
        public void extractRootWithWrappedCause() {
            throw AccessHelper.Failure.create("does not matter",
                AccessHelper.Failure.create("does not matter", new IllegalArgumentException("failed argument"))) //
            .getRoot(RuntimeException.class);
        }
    }

    /**
     * Check fields access helper behavior.
     */
    @FixMethodOrder(MethodSorters.JVM)
    @RunWith(BlockJUnit4ClassRunner.class)
    public static final class FieldsBehavior implements Unknown {

        /**
         */
        private static final String NAME_CHANGING = "MODE_CHANGING";

        /**
         * Unknown failure mode for testing.
         */
        private static AccessHelper.Failure.Mode MODE_CHANGING = // NOPMD: for testing!
            AccessHelper.Enums.create(AccessHelper.Failure.Mode.class, //
                NAME_CHANGING, AccessHelper.Failure.Mode.values().length, null);

        /**
         * Activate expectation rule.
         */
        @Rule
        public final ExpectRule expect = new ExpectRule();

        /**
         * Test field resolution for first field with given name.
         */
        @Test
        public void resolveByName() {
            Field field = AccessHelper.Fields.resolve(Base.class, NAME_VALUE);
            Assert.assertThat(field, CoreMatchers.notNullValue());
            Assert.assertThat(field, CoreMatchers.is(FIELD_VALUE));
        }

        /**
         * Test field resolution for none existing field with default failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "invalid field failure [type=" + NAME_BASE + ", name=x]")
        public void resolveFailureDefaultMode() {
            AccessHelper.Fields.resolve(Base.class, "x");
        }

        /**
         * Test field resolution for none existing field with throw-exception failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "invalid field failure [type=" + NAME_BASE + ", name=x]")
        public void resolveFailureThrowExceptionMode() {
            AccessHelper.Fields.resolve(Base.class, "x", AccessHelper.Failure.Mode.THROW_EXCEPTION);
        }

        /**
         * Test field resolution for none existing field with return-null failure mode.
         */
        @Test
        public void resolveFailureReturnNullMode() {
            Field field = AccessHelper.Fields.resolve(Base.class, "x", AccessHelper.Failure.Mode.RETURN_NULL);
            Assert.assertThat(field, CoreMatchers.nullValue());
        }

        /**
         * Test field resolution for none existing field with unknown failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "mode not supported [" + NAME_UNKNOWN + "]")
        public void resolveFailureUnkownMode() {
            AccessHelper.Fields.resolve(Base.class, "x", MODE_FAILURE_UNKNOWN);
        }

        /**
         * Test field resolution for none existing field with unknown failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "resolve type not supported [" + NAME_UNKNOWN + "]")
        public void resolveFailureUnkownType() {
            AccessHelper.Fields.resolve(Base.class, long.class, TYPE_RESOLVE_UNKNOWN);
        }

        /**
         * Test fields resolution for all with given name.
         */
        @Test
        public void resolveAllByName() {
            List<Field> fields = //
                AccessHelper.Fields.resolve(Child.class, NAME_VALUE, (Class<?>) null);
            Assert.assertThat(fields, CoreMatchers.hasItem(FIELD_VALUE));
            Assert.assertThat(fields.size(), CoreMatchers.is(2));
        }

        /**
         * Test field resolution for all with given class type using super mode.
         */
        @Test
        public void resolveAllByTypeSuper() {
            List<Field> fields = //
                AccessHelper.Fields.resolve(Child.class, long.class, AccessHelper.Resolve.Type.SUPER);
            Assert.assertThat(fields, CoreMatchers.hasItem(FIELD_VALUE));
            Assert.assertThat(fields.size(), CoreMatchers.is(5));
        }

        /**
         * Test field resolution for all with given class type using child mode.
         */
        @Test
        public void resolveAllByTypeBoxedChild() {
            List<Field> fields = //
                AccessHelper.Fields.resolve(Child.class, Long.class, AccessHelper.Resolve.Type.CHILD);
            Assert.assertThat(fields, CoreMatchers.hasItem(FIELD_VALUE));
            Assert.assertThat(fields.size(), CoreMatchers.is(5));
        }

        /**
         * Test field resolution for all with given name and given class type using super mode.
         */
        @Test
        public void resolveAllByNameTypeSuper() {
            List<Field> fields = //
                AccessHelper.Fields.resolve(Child.class, NAME_VALUE, long.class, AccessHelper.Resolve.Type.SUPER);
            Assert.assertThat(fields, CoreMatchers.hasItem(FIELD_VALUE));
            Assert.assertThat(fields.size(), CoreMatchers.is(2));
        }

        /**
         * Test field resolution for all with given class type using exact mode.
         */
        @Test
        public void resolveAllByTypeBaseExact() {
            List<Field> fields = //
                AccessHelper.Fields.resolve(Child.class, Base.class, AccessHelper.Resolve.Type.EXACT);
            Assert.assertThat(fields.size(), CoreMatchers.is(1));
        }

        /**
         * Test field resolution for all with given class type using child mode.
         */
        @Test
        public void resolveAllByTypeNumberChild() {
            List<Field> fields = //
                AccessHelper.Fields.resolve(Child.class, Number.class, AccessHelper.Resolve.Type.CHILD);
            Assert.assertThat(fields.size(), CoreMatchers.is(1));
        }

        /**
         * Test field resolution for all with given class type using super mode.
         */
        @Test
        public void resolveAllByTypeChildSuper() {
            List<Field> fields = //
                AccessHelper.Fields.resolve(Child.class, Base.class, AccessHelper.Resolve.Type.SUPER);
            Assert.assertThat(fields.size(), CoreMatchers.is(1));
        }

        /**
         * Test getting field value with invalid field.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "invalid field failure [target=Base[value=0, integer=0], name=x]")
        public void getFailureInvalidField() {
            AccessHelper.Fields.get(new Base(0, 0), "x");
        }

        /**
         * Test getting static field value with invalid field.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "invalid field failure [type=java.lang.Object, name=x]")
        public void getFailureStaticInvalidField() {
            AccessHelper.Fields.get(Object.class, "x");
        }

        /**
         * Test getting field value with illegal access.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "illegal access failure [target=Base[value=0, integer=0], field=value]")
        public void getFailureIllegalAccess() {
            Field field = AccessHelper.Fields.resolve(Base.class, NAME_VALUE);
            field.setAccessible(false);
            AccessHelper.Fields.get(new Base(0, 0), field);
        }

        /**
         * Test getting field value with illegal argument.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "illegal argument failure [target=DEFAULT, field=value]")
        public void getFailureIllegalArgument() {
            AccessHelper.Fields.get(AccessHelper.Failure.Mode.DEFAULT, FIELD_VALUE);
        }

        /**
         * Test getting field value by declared field name on {@code null} target object.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "illegal argument failure [target=null, name=value]")
        public void getFailureNullTargetName() {
            AccessHelper.Fields.get((Object) null, NAME_VALUE);
        }

        /**
         * Test getting field value by declared field on {@code null} target object.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "illegal access failure [target=null, field=value]")
        public void getFailureNullTargetNoneStatic() {
            AccessHelper.Fields.get((Object) null, FIELD_VALUE);
        }

        /**
         * Test getting field value by name successful.
         */
        @Test
        public void getValueByName() {
            Base base = new Base(10, 0);
            Long actual = AccessHelper.Fields.get(base, NAME_VALUE);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.equalTo(base.value()));
        }

        /**
         * Test getting annotation field value by name successful.
         */
        @Test
        public void getAnnoValueByName() {
            Anno anno = Base.class.getAnnotation(Anno.class);
            Long actual = AccessHelper.Fields.get(anno, NAME_VALUE);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.equalTo(anno.value()));
        }

        /**
         * Test getting annotation implentation field value by name successful.
         */
        @Test
        public void getAnnoImplValueByName() {
            Anno anno = new AnnoImpl(2);
            Long actual = AccessHelper.Fields.get(anno, NAME_VALUE);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.equalTo(anno.value()));
        }

        /**
         * Test getting field value by declared field successful.
         */
        @Test
        public void getValueByField() {
            Base base = new Base(11, 0);
            Long actual = AccessHelper.Fields.get(base, FIELD_VALUE);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.equalTo(base.value()));
        }

        /**
         * Test getting static field value by name successful.
         */
        @Test
        public void getStaticByBName() {
            AccessHelper.Failure.Mode actual = AccessHelper.Fields.get(FieldsBehavior.class, NAME_CHANGING);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.equalTo(MODE_CHANGING));
        }

        /**
         * Test getting static field value by declared field successful.
         */
        @Test
        public void getStaticByField() {
            Field field = AccessHelper.Fields.resolve(FieldsBehavior.class, NAME_CHANGING);
            AccessHelper.Failure.Mode actual = AccessHelper.Fields.get((Object) null, field);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.equalTo(MODE_CHANGING));
        }

        /**
         * Test setting field value with invalid field.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "invalid field failure [target=Base[value=0, integer=0], name=x, value=null]")
        public void setFailureInvalidField() {
            AccessHelper.Fields.set(new Base(0, 0), "x", null);
        }

        /**
         * Test setting field value with illegal access.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "illegal access failure [target=java.lang.Object, field=ENUM, value=null]")
        public void setFailureIllegalAccess() {
            Field field = AccessHelper.Fields.resolve(Class.class, "ENUM");
            AccessHelper.Fields.set(Object.class, field, null);
        }

        /**
         * Test setting field value with illegal argument.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "illegal argument failure [target=Base[value=0, integer=0], field=value, value=null]")
        public void setFailureIllegalArgument() {
            Field field = AccessHelper.Fields.resolve(Child.class, NAME_VALUE);
            AccessHelper.Fields.set(new Base(0, 0), field, null);
        }

        /**
         * Test setting field value by declared field name on {@code null} target object.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "illegal argument failure [target=null, name=value, value=0]")
        public void setFailureNullTargetName() {
            AccessHelper.Fields.set(null, NAME_VALUE, 0L);
        }

        /**
         * Test setting field value by declared field on {@code null} target object.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "illegal access failure [target=null, field=value, value=0]")
        public void setFailureNullTargetNoneStatic() {
            AccessHelper.Fields.set(null, FIELD_VALUE, 0L);
        }

        /**
         * Test setting null field value on annotation target object.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "illegal argument failure [target=@" + NAME_HELPER
                + "Test$Anno(value=1), name=value, value=null]"
        )
        public void setFailureIllegalArgumentNullAnnoValue() {
            Anno anno = Base.class.getAnnotation(Anno.class);
            AccessHelper.Fields.set(anno, NAME_VALUE, null);
        }

        /**
         * Test setting invalid field value on annotation target object.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "illegal argument failure [target=@" + NAME_HELPER
                + "Test$Anno(value=1), name=value, value=test]"
        )
        public void setFailureIllegalArgumentAnnoValue() {
            Anno anno = Base.class.getAnnotation(Anno.class);
            AccessHelper.Fields.set(anno, NAME_VALUE, "test");
        }

        /**
         * Test setting field value by name successful.
         */
        @Test
        public void setValueByName() {
            Base base = new Base(12, 0);
            Long before = base.value();
            Long actual = AccessHelper.Fields.set(base, NAME_VALUE, 13L);
            Assert.assertThat(actual, CoreMatchers.equalTo(before));
            Assert.assertThat(base.value(), CoreMatchers.equalTo(13L));
        }

        /**
         * Test setting field value by name successful.
         */
        @Test
        public void setAnnoValueByName() {
            Anno anno = Base.class.getAnnotation(Anno.class);
            Long before = anno.value();
            Long actual = AccessHelper.Fields.set(anno, NAME_VALUE, 14L);
            Assert.assertThat(actual, CoreMatchers.equalTo(before));
            Assert.assertThat(anno.value(), CoreMatchers.equalTo(14L));
            AccessHelper.Fields.set(anno, NAME_VALUE, before);
        }

        /**
         * Test setting field value by declared field successful.
         */
        @Test
        public void setValueByField() {
            Base base = new Base(12, 0);
            Long before = base.value();
            Long actual = AccessHelper.Fields.set(base, FIELD_VALUE, 15L);
            Assert.assertThat(actual, CoreMatchers.equalTo(before));
            Assert.assertThat(base.value(), CoreMatchers.equalTo(15L));
        }

        /**
         * Test setting static field value by field successful.
         */
        @Test
        public void setStaticValueByField() {
            AccessHelper.Failure.Mode mode = MODE_CHANGING;
            Assert.assertThat(mode, CoreMatchers.notNullValue());
            Field field = AccessHelper.Fields.resolve(FieldsBehavior.class, NAME_CHANGING);
            AccessHelper.Failure.Mode actual = AccessHelper.Fields.set(null, field, null);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.equalTo(mode));
            Assert.assertThat(MODE_CHANGING, CoreMatchers.nullValue());
            actual = AccessHelper.Fields.set(null, field, mode);
            Assert.assertThat(actual, CoreMatchers.nullValue());
            Assert.assertThat(MODE_CHANGING, CoreMatchers.notNullValue());
            Assert.assertThat(MODE_CHANGING, CoreMatchers.equalTo(mode));
        }

        /**
         * Test modified field declaration.
         */
        @Test
        public void modififyField() {
            Field field = AccessHelper.Fields.resolve(FieldsBehavior.class, NAME_CHANGING);
            int before = field.getModifiers();
            Assert.assertThat(AccessHelper.Fields.modify(field,
                    (before & ~Modifier.PRIVATE) | Modifier.FINAL | Modifier.PUBLIC), CoreMatchers.equalTo(before));
            int actual = field.getModifiers();
            Assert.assertThat(actual & Modifier.PRIVATE, CoreMatchers.equalTo(0));
            Assert.assertThat(actual & Modifier.FINAL, CoreMatchers.equalTo(Modifier.FINAL));
            Assert.assertThat(actual & Modifier.PUBLIC, CoreMatchers.equalTo(Modifier.PUBLIC));
        }
    }

    /**
     * Check fields access helper behavior.
     */
    @FixMethodOrder(MethodSorters.JVM)
    @RunWith(BlockJUnit4ClassRunner.class)
    public static final class MethodsBehavior implements Unknown {

        /**
         * Activate expectation rule.
         */
        @Rule
        public final ExpectRule expect = new ExpectRule();

        /**
         * Test method resolution for first method with given name.
         */
        @Test
        public void resolveByName() {
            Method method = AccessHelper.Methods.resolve(Base.class, NAME_VALUE);
            Assert.assertThat(method, CoreMatchers.notNullValue());
            Assert.assertThat(method, CoreMatchers.is(GETTER_VALUE));
        }

        /**
         * Test method resolution for none existing method with default failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "invalid method failure [type=" + NAME_BASE + ", name=x, types=[]]")
        public void resolveFailureDefaultMode() {
            AccessHelper.Methods.resolve(Base.class, "x");
        }

        /**
         * Test method resolution for none existing method with throw-exception failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "invalid method failure [type=" + NAME_BASE + ", name=x, types=[]]")
        public void resolveFailureThrowExceptionMode() {
            AccessHelper.Methods.resolve(Base.class, "x", AccessHelper.Failure.Mode.THROW_EXCEPTION);
        }

        /**
         * Test method resolution for none existing method with return-null failure mode.
         */
        @Test
        public void resolveFailureReturnNullMode() {
            Method method = AccessHelper.Methods.resolve(Base.class, "x", AccessHelper.Failure.Mode.RETURN_NULL);
            Assert.assertThat(method, CoreMatchers.nullValue());
        }

        /**
         * Test method resolution for none existing method with unknown failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "mode not supported [" + NAME_UNKNOWN + "]")
        public void resolveFailureUnkownMode() {
            AccessHelper.Methods.resolve(Base.class, "x", MODE_FAILURE_UNKNOWN);
        }

        /**
         * Test method resolution for all methods with given method name.
         */
        @Test
        public void resolveAllByName() {
            List<Method> methods = //
                AccessHelper.Methods.resolve(Child.class, NAME_VALUE, (Class<?>[]) null, null);
            Assert.assertThat(methods, CoreMatchers.hasItem(GETTER_VALUE));
            Assert.assertThat(methods, CoreMatchers.hasItem(SETTER_VALUE));
            Assert.assertThat(methods.size(), CoreMatchers.is(4));
        }

        /**
         * Test method resolution for all methods with given argument class types.
         */
        @Test
        public void resolveAllByArgTypes() {
            List<Method> methods = //
                AccessHelper.Methods.resolve(Child.class, null, new Class<?>[] { long.class }, null);
            Assert.assertThat(methods, CoreMatchers.hasItem(SETTER_VALUE));
            Assert.assertThat(methods.size(), CoreMatchers.is(5));
        }

        /**
         * Test method resolution for all methods with given return class type.
         */
        @Test
        public void resolveAllByReturnType() {
            List<Method> methods = //
                AccessHelper.Methods.resolve(Child.class, null, null, long.class);
            Assert.assertThat(methods, CoreMatchers.hasItem(GETTER_VALUE));
            Assert.assertThat(methods.size(), CoreMatchers.is(7));
        }

        /**
         * Test method resolution for all methods with given method name and given return class
         * type.
         */
        @Test
        public void resolveAllByNameArgTypes() {
            List<Method> methods = //
                AccessHelper.Methods.resolve(Child.class, NAME_VALUE, new Class<?>[] { long.class }, null);
            Assert.assertThat(methods, CoreMatchers.hasItem(SETTER_VALUE));
            Assert.assertThat(methods.size(), CoreMatchers.is(2));
        }

        /**
         * Test method resolution for all methods with given method name and given return class
         * type.
         */
        @Test
        public void resolveAllByNameReturnType() {
            List<Method> methods = //
                AccessHelper.Methods.resolve(Child.class, NAME_VALUE, null, long.class);
            Assert.assertThat(methods, CoreMatchers.hasItem(GETTER_VALUE));
            Assert.assertThat(methods.size(), CoreMatchers.is(2));
        }

        /**
         * Test method resolution for all methods with given method name and given return class
         * type.
         */
        @Test
        public void resolveAllByAll() {
            List<Method> methods = //
                AccessHelper.Methods.resolve(Child.class, NAME_VALUE, new Class<?>[] { long.class }, void.class);
            Assert.assertThat(methods, CoreMatchers.hasItem(SETTER_VALUE));
            Assert.assertThat(methods.size(), CoreMatchers.is(2));
        }

        /**
         * Test successful method invocation by method name.
         */
        @Test
        public void invokeByName() {
            Base base = new Base(14, 1);
            Long actual = AccessHelper.Methods.invoke(base, NAME_VALUE, new Class<?>[] { long.class },
                    new Object[] { 15L });
            Assert.assertThat(actual, CoreMatchers.nullValue());
            Assert.assertThat(base.value(), CoreMatchers.is(15L));
        }

        /**
         * Test successful method invocation by class type and method name.
         */
        @Test
        public void invokeByTypeAndName() {
            Base base = new Base(15, 2);
            Long actual = AccessHelper.Methods.invoke(base, Base.class, NAME_VALUE, new Class<?>[] { long.class },
                    new Object[] { 15L });
            Assert.assertThat(actual, CoreMatchers.nullValue());
            Assert.assertThat(base.value(), CoreMatchers.is(15L));
        }

        /**
         * Test successful method invocation by declared method.
         */
        @Test
        public void invokeByMethod() {
            Base base = new Base(16, 3);
            Long actual = AccessHelper.Methods.invoke(base, SETTER_VALUE, new Object[] { 15L });
            Assert.assertThat(actual, CoreMatchers.nullValue());
            Assert.assertThat(base.value(), CoreMatchers.is(15L));
        }

        /**
         * Test method invocation with illegal arguments and default failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "illegal argument failure [target=Base[value=17, integer=4], method=value, args=[]]")
        public void invokeFailureDefaultMode() {
            Base base = new Base(17, 4);
            AccessHelper.Methods.invoke(base, SETTER_VALUE);
        }

        /**
         * Test method invocation with illegal arguments and throw-exception failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "illegal argument failure [target=Base[value=17, integer=4], method=value, args=[]]")
        public void invokeFailureThrowExceptionMode() {
            Base base = new Base(17, 4);
            AccessHelper.Methods.invoke(base, SETTER_VALUE, AccessHelper.Failure.Mode.THROW_EXCEPTION);
        }

        /**
         * Test method invocation with illegal arguments and return-null failure mode.
         */
        @Test
        public void invokeFailureReturnNullMode() {
            Base base = new Base(18, 5);
            AccessHelper.Methods.invoke(base, SETTER_VALUE, AccessHelper.Failure.Mode.RETURN_NULL);
        }

        /**
         * Test method invocation with illegal arguments and unknown failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "mode not supported [" + NAME_UNKNOWN + "]")
        public void invokeFailureUnkownMode() {
            Base base = new Base(19, 6);
            AccessHelper.Methods.invoke(base, SETTER_VALUE, MODE_FAILURE_UNKNOWN);
        }
    }

    /**
     * Check fields access helper behavior.
     */
    @RunWith(Suite.class)
    @Suite.SuiteClasses(
        {
            ClassesBehavior.DefaultBehavior.class,
            ClassesBehavior.FindBehavior.class,
            ClassesBehavior.GenericBehavior.class
        }
    )
    public static final class ClassesBehavior {

        /**
         * Beans access helper behavior.
         */
        @FixMethodOrder(MethodSorters.JVM)
        @RunWith(BlockJUnit4ClassRunner.class)
        public static final class DefaultBehavior implements Unknown {

            /**
             * Test class resolution for class with given name.
             */
            @Test
            public void resolveByName() {
                Class<Base> type = AccessHelper.Classes.resolve(NAME_BASE);
                Assert.assertThat(type, CoreMatchers.notNullValue());
                Assert.assertThat(type, CoreMatchers.equalTo(Base.class));
            }

            /**
             * Test class resolution for class with given name.
             */
            @Test
            public void resolveByTypeAndName() {
                Class<Base> type = AccessHelper.Classes.resolve(AccessHelperTest.class, "Base");
                Assert.assertThat(type, CoreMatchers.notNullValue());
                Assert.assertThat(type, CoreMatchers.equalTo(Base.class));
            }

            /**
             * Test class resolution for none existing class with default failure mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "invalid class failure [name=x]")
            public void resolveFailureDefaultMode() {
                AccessHelper.Classes.resolve("x");
            }

            /**
             * Test class resolution for none existing v with throw-exception failure mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "invalid method failure [name=x]")
            public void resolveFailureThrowExceptionMode() {
                AccessHelper.Classes.resolve("x", AccessHelper.Failure.Mode.THROW_EXCEPTION);
            }

            /**
             * Test class resolution for none existing class with return-null failure mode.
             */
            @Test
            public void resolveFailureReturnNullMode() {
                Class<?> type = AccessHelper.Classes.resolve("x", AccessHelper.Failure.Mode.RETURN_NULL);
                Assert.assertThat(type, CoreMatchers.nullValue());
            }

            /**
             * Test class resolution for none existing class with unknown failure mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "mode not supported [" + NAME_UNKNOWN + "]")
            public void resolveFailureUnkownMode() {
                AccessHelper.Classes.resolve("x", MODE_FAILURE_UNKNOWN);
            }
        }

        /**
         * Class access helper find behavior test.
         */
        public static final class FindBehavior extends ParameterTest {

            /**
             * Class name of class to find.
             */
            private final String name;

            /**
             * Failure handling mode.
             */
            private final AccessHelper.Failure.Mode mode;

            /**
             * Create find behavior test with given class name of class to find, failure handling
             * mode, and expected test result/failure.
             *
             * @param  name    class name of class to find.
             * @param  mode    failure handling mode.
             * @param  result  expected test result/failure.
             */
            public FindBehavior(String name, AccessHelper.Failure.Mode mode, Object result) {
                super(result);
                this.name = name;
                this.mode = mode;
            }

            /**
             * Create test data.
             *
             * @return  test data.
             */
            @Parameterized.Parameters(name = "{index}: name={0}, mode{1}, result={2}")
            public static Iterable<Object[]> data() {
                Builder builder = ParameterTest.builder();
                for (Class<?> type : new Class<?>[] {
                        Long.class, Integer.class, Short.class, Byte.class,
                        long.class, int.class, short.class, byte.class,
                        Double.class, Float.class, Boolean.class, Character.class,
                        double.class, float.class, boolean.class, char.class,
                        String.class, Class.class
                    }) {
                    builder.add(type.getName(), AccessHelper.Failure.Mode.THROW_EXCEPTION, type);
                    builder.add(type.getSimpleName(), AccessHelper.Failure.Mode.THROW_EXCEPTION, type);
                }
                for (Class<?> type : new Class<?>[] { Base.class, Child.class }) {
                    builder.add(type.getName(), AccessHelper.Failure.Mode.THROW_EXCEPTION, type);
                }
                builder.add("x", AccessHelper.Failure.Mode.RETURN_NULL, null);
                builder.add("x", AccessHelper.Failure.Mode.DEFAULT,
                    Expect.Builder.create(AccessHelper.Failure.class, "invalid class failure [name=x]") //
                    .cause(ClassNotFoundException.class, "x"));
                builder.add("x", AccessHelper.Failure.Mode.THROW_EXCEPTION,
                    Expect.Builder.create(AccessHelper.Failure.class, "invalid class failure [name=x]") //
                    .cause(ClassNotFoundException.class, "x"));
                return builder;
            }

            /**
             * Test class access helper find behavior.
             */
            @Test
            public void test() { // NOPMD: handled by rule!
                this.rule.actual(AccessHelper.Classes.find(this.name, this.mode));
            }
        }

        /**
         * Class access helper generic behavior test.
         */
        public static final class GenericBehavior extends ParameterTest {

            /**
             * Class type to analyse for generic parameters.
             */
            private final Class<?> type;

            /**
             * Index of parameter type.
             */
            private final int param;

            /**
             * Index of parameter bound.
             */
            private final int bound;

            /**
             * Create generic behavior test with given class type, index of parameter type, index of
             * parameter bound, and expected test result/failure.
             *
             * @param  type    class type.
             * @param  param   index of parameter type.
             * @param  bound   index of parameter bound.
             * @param  result  expected test result/failure.
             */
            public GenericBehavior(Class<?> type, int param, int bound, Object result) {
                super(result);
                this.type = type;
                this.param = param;
                this.bound = bound;
            }

            /**
             * Create test data.
             *
             * @return  test data.
             */
            @Parameterized.Parameters(name = "{index}: type={0}, param={1}, bound={2}, result={3}")
            public static Iterable<Object[]> data() {
                Builder builder = ParameterTest.builder();
                builder.add(Object.class, 0, 0,
                    Expect.Builder.create(AccessHelper.Failure.class, "not parameterized type [java.lang.Object]"));

                Map<String, String> map = new HashMap<String, String>();
                builder.add(map.getClass(), 0, 0, Object.class);
                builder.add(map.getClass(), 2, 0,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "invalid parameter index [type=java.util.HashMap, param=2, bound=0]"));
                builder.add(map.getClass(), 1, 1,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "invalid parameter bound index [type=java.util.HashMap, param=1, bound=1]"));
                builder.add(map.getClass(), 1, 0, Object.class);

                @SuppressWarnings("unchecked")
                Map<String, String>[] array = (HashMap<String, String>[]) Array.newInstance(map.getClass(), 4);
                builder.add(array.getClass(), 0, 0, Object.class);
                return builder;
            }

            /**
             * Test class access helper generic behavior.
             */
            @Test
            public void test() { // NOPMD: handled by rule!
                this.rule.actual(AccessHelper.Classes.generic(this.type, this.param, this.bound));
            }
        }
    }

    /**
     * Check object access helper behavior.
     */
    @RunWith(Suite.class)
    @Suite.SuiteClasses(
        {
            ObjectsBehavior.DefaultBehavior.class,
            ObjectsBehavior.CopyBehavior.class
        }
    )
    public static final class ObjectsBehavior {

        /**
         * Base constructor types.
         */
        private static final Class<?>[] FACTORY_TYPES = //
            new Class<?>[] { long.class, int.class };

        /**
         * Base constructor.
         */
        private static final Constructor<Base> FACTORY_BASE = //
            AccessHelper.Objects.resolve(Base.class, FACTORY_TYPES);

        /**
         * Object access helper default behavior.
         */
        @FixMethodOrder(MethodSorters.JVM)
        @RunWith(BlockJUnit4ClassRunner.class)
        public static final class DefaultBehavior implements Unknown {

            /**
             * Activate expectation rule.
             */
            @Rule
            public final ExpectRule expect = new ExpectRule();

            /**
             * Test successful constructor resolution by class type and argument types.
             */
            @Test
            public void resolveByTypes() {
                Constructor<Base> factory = //
                    AccessHelper.Objects.resolve(Base.class, FACTORY_TYPES);
                Assert.assertThat(factory, CoreMatchers.notNullValue());
                Assert.assertThat(factory, CoreMatchers.is(FACTORY_BASE));
            }

            /**
             * Test constructor resolution for none existing class with default failure mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "invalid method failure [type=" + NAME_BASE + ", types=[java.lang.Long]]")
            public void resolveFailureDefaultMode() {
                AccessHelper.Objects.resolve(Base.class, new Class[] { Long.class });
            }

            /**
             * Test class resolution for none existing v with throw-exception failure mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "invalid method failure [type=" + NAME_BASE + ", types=[]]")
            public void resolveFailureThrowExceptionMode() {
                AccessHelper.Objects.resolve(Base.class, AccessHelper.Failure.Mode.THROW_EXCEPTION);
            }

            /**
             * Test class resolution for none existing class with return-null failure mode.
             */
            @Test
            public void resolveFailureReturnNullMode() {
                Constructor<Base> factory = //
                    AccessHelper.Objects.resolve(Base.class, AccessHelper.Failure.Mode.RETURN_NULL);
                Assert.assertThat(factory, CoreMatchers.nullValue());
            }

            /**
             * Test class resolution for none existing class with unknown failure mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "mode not supported [" + NAME_UNKNOWN + "]")
            public void resolveFailureUnkownMode() {
                AccessHelper.Objects.resolve(Base.class, MODE_FAILURE_UNKNOWN);
            }

            /**
             * Test successful object creation by class type.
             */
            @Test
            public void createByFactory() {
                Base base = AccessHelper.Objects.create(FACTORY_BASE, new Object[] { 14L, -1 });
                Assert.assertThat(base, CoreMatchers.notNullValue());
                Assert.assertThat(base.value(), CoreMatchers.is(14L));
                Assert.assertThat(base.getInteger(), CoreMatchers.is(-1));
            }

            /**
             * Test object creation by class type with illegal arguments.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "illegal argument failure [type=" + NAME_BASE + ", types=[long, int], args=[14]]")
            public void createByFactoryIllegalArgument() {
                AccessHelper.Objects.create(FACTORY_BASE, new Object[] { 14L });
            }

            /**
             * Test object creation by class type with illegal arguments and return-null failure
             * mode.
             */
            @Test
            public void createByFactoryIllegalArgumentReturnNull() {
                Base base = AccessHelper.Objects.create(FACTORY_BASE, //
                        AccessHelper.Failure.Mode.RETURN_NULL, new Object[] { 14L });
                Assert.assertThat(base, CoreMatchers.nullValue());
            }

            /**
             * Test object creation by class type with invocation target exception.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(
                message = "invocation target failure [type=" + NAME_BASE + ", types=[long, int], args=[" //
                    + Long.MIN_VALUE + ", -1]]"
            )
            public void createByFactoryInvokationTarget() {
                AccessHelper.Objects.create(FACTORY_BASE, new Object[] { Long.MIN_VALUE, -1 });
            }

            /**
             * Test object creation by class type with invocation target exception and return-null
             * failure mode.
             */
            @Test
            public void createByFactoryInvokationTargetReturnNull() {
                Base base = AccessHelper.Objects.create(FACTORY_BASE, //
                        AccessHelper.Failure.Mode.RETURN_NULL, new Object[] { Long.MIN_VALUE, -1 });
                Assert.assertThat(base, CoreMatchers.nullValue());
            }

            /**
             * Test object creation by class type for none existing constructor with throw-exception
             * failure mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "illegal argument failure [type=" + NAME_BASE + ", types=[long, int], args=[failure]]")
            public void createByFactoryFailureThrowException() {
                AccessHelper.Objects.create(FACTORY_BASE, AccessHelper.Failure.Mode.THROW_EXCEPTION, //
                    new Object[] { "failure" });
            }

            /**
             * Test object creation by class type for none existing constructor with unknown failure
             * mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "mode not supported [" + NAME_UNKNOWN + "]")
            public void createByFactoryFailureUnkownMode() {
                AccessHelper.Objects.create(FACTORY_BASE, MODE_FAILURE_UNKNOWN, new Object[] { "failure" });
            }

            /**
             * Test successful object creation by class type.
             */
            @Test
            public void createByTypes() {
                Base base = AccessHelper.Objects.create(Base.class, //
                        FACTORY_TYPES, new Object[] { 14L, -1 });
                Assert.assertThat(base, CoreMatchers.notNullValue());
                Assert.assertThat(base.value(), CoreMatchers.is(14L));
                Assert.assertThat(base.getInteger(), CoreMatchers.is(-1));
            }

            /**
             * Test successful object creation of enumeration value by class type.
             */
            @Test
            public void createByTypesEnum() {
                AccessHelper.Failure.Type type = AccessHelper.Objects.create(AccessHelper.Failure.Type.class, //
                        new Class<?>[] { String.class }, new Object[] { AccessHelper.Failure.Type.UNKNOWN.name() });
                Assert.assertThat(type, CoreMatchers.notNullValue());
                Assert.assertThat(type, CoreMatchers.is(AccessHelper.Failure.Type.UNKNOWN));
            }

            /**
             * Test object creation of enumeration value by class type with invalid request
             * arguments.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "support failure [type=" + NAME_HELPER + "$Failure$Type, types=null, args=[]]")
            public void createByTypesEnumFailure() {
                AccessHelper.Objects.create(AccessHelper.Failure.Type.class, null, new Object[] {});
            }

            /**
             * Test object creation by class type on abstract class.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "instantiation failure [type=" + NAME_HELPER + ", types=null, args=[]]")
            public void createByTypesInstantiation() {
                AccessHelper.Objects.create(AccessHelper.class, null);
            }

            /**
             * Test object creation by class type with illegal arguments.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "illegal argument failure [type=" + NAME_BASE + ", types=[long, int], args=[14]]")
            public void createByTypesIllegalArgument() {
                AccessHelper.Objects.create(Base.class, //
                    FACTORY_TYPES, new Object[] { 14L });
            }

            /**
             * Test object creation by class type with illegal arguments and return-null failure
             * mode.
             */
            @Test
            public void createByTypesIllegalArgumentReturnNull() {
                Base base = AccessHelper.Objects.create(Base.class, AccessHelper.Failure.Mode.RETURN_NULL, //
                        FACTORY_TYPES, new Object[] { 14L });
                Assert.assertThat(base, CoreMatchers.nullValue());
            }

            /**
             * Test object creation by class type with invocation target exception.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(
                message = "invocation target failure [type=" + NAME_BASE + ", types=[long, int], args=[" //
                    + Long.MIN_VALUE + ", -1]]"
            )
            public void createByTypesInvokationTarget() {
                AccessHelper.Objects.create(Base.class, //
                    FACTORY_TYPES, new Object[] { Long.MIN_VALUE, -1 });
            }

            /**
             * Test object creation by class type with invocation target exception and return-null
             * failure mode.
             */
            @Test
            public void createByTypesInvokationTargetReturnNull() {
                Base base = AccessHelper.Objects.create(Base.class, AccessHelper.Failure.Mode.RETURN_NULL, //
                        FACTORY_TYPES, new Object[] { Long.MIN_VALUE, -1 });
                Assert.assertThat(base, CoreMatchers.nullValue());
            }

            /**
             * Test object creation by class type for none existing constructor with default failure
             * mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "invalid method failure [type=" + NAME_BASE + ", types=[java.lang.Long], args=[]]")
            public void createByTypesFailureDefault() {
                AccessHelper.Objects.create(Base.class, //
                    new Class<?>[] { Long.class }, new Object[] {});
            }

            /**
             * Test object creation by class type for none existing constructor with throw-exception
             * failure mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "invalid method failure [type=" + NAME_BASE + ", types=[], args=[failure]]")
            public void createByTypesFailureThrowException() {
                AccessHelper.Objects.create(Base.class, AccessHelper.Failure.Mode.THROW_EXCEPTION, //
                    new Class<?>[] {}, new Object[] { "failure" });
            }

            /**
             * Test object creation by class type for none existing constructor with return-null
             * failure mode.
             */
            @Test
            public void createByTypesFailureReturnNull() {
                Base base = AccessHelper.Objects.create(Base.class, AccessHelper.Failure.Mode.RETURN_NULL, //
                        new Class<?>[] { String.class }, new Object[] { "failure" });
                Assert.assertThat(base, CoreMatchers.nullValue());
            }

            /**
             * Test object creation by class type for none existing constructor with unknown failure
             * mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "mode not supported [" + NAME_UNKNOWN + "]")
            public void createByTypesFailureUnkownMode() {
                AccessHelper.Objects.create(Base.class, MODE_FAILURE_UNKNOWN, //
                    new Class<?>[] { String.class }, new Object[] { "failure" });
            }

            /**
             * Test successful object creation by name.
             */
            @Test
            public void createByName() {
                Base base = AccessHelper.Objects.create(NAME_BASE, //
                        FACTORY_TYPES, new Object[] { 14L, -1 });
                Assert.assertThat(base, CoreMatchers.notNullValue());
                Assert.assertThat(base.value(), CoreMatchers.is(14L));
                Assert.assertThat(base.getInteger(), CoreMatchers.is(-1));
            }

            /**
             * Test successful object creation of enumeration value by name.
             */
            @Test
            public void createByNameEnum() {
                AccessHelper.Failure.Type type = AccessHelper.Objects.create(AccessHelper.Failure.Type.class.getName(), //
                        new Class<?>[] { String.class }, new Object[] { AccessHelper.Failure.Type.UNKNOWN.name() });
                Assert.assertThat(type, CoreMatchers.notNullValue());
                Assert.assertThat(type, CoreMatchers.is(AccessHelper.Failure.Type.UNKNOWN));
            }

            /**
             * Test object creation of enumeration value by name with invalid request arguments.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "support failure [type=" + NAME_HELPER + "$Failure$Type, types=null, args=[]]")
            public void createByNameEnumFailure() {
                AccessHelper.Objects.create(AccessHelper.Failure.Type.class.getName(), null, new Object[] {});
            }

            /**
             * Test object creation by name with return null failure handling.
             */
            @Test
            public void createByNameNull() {
                AccessHelper.Failure.Type type = AccessHelper.Objects.create("x", AccessHelper.Failure.Mode.RETURN_NULL,
                        null, (Object) null);
                Assert.assertThat(type, CoreMatchers.nullValue());
            }

            /**
             * Test object creation by name with illegal arguments.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "illegal argument failure [type=" + NAME_BASE + ", types=[long, int], args=[14]]")
            public void createByNameIllegalArgument() {
                AccessHelper.Objects.create(NAME_BASE, //
                    FACTORY_TYPES, new Object[] { 14L });
            }

            /**
             * Test object creation by name with illegal arguments and return-null failure mode.
             */
            @Test
            public void createByNameIllegalArgumentReturnNull() {
                Base base = AccessHelper.Objects.create(NAME_BASE, AccessHelper.Failure.Mode.RETURN_NULL, //
                        FACTORY_TYPES, new Object[] { 14L });
                Assert.assertThat(base, CoreMatchers.nullValue());
            }

            /**
             * Test object creation by name with invocation target exception.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(
                message = "invocation target failure [type=" + NAME_BASE + ", types=[long, int], args=[" //
                    + Long.MIN_VALUE + ", -1]]"
            )
            public void createByNameInvokationTarget() {
                AccessHelper.Objects.create(NAME_BASE, //
                    FACTORY_TYPES, new Object[] { Long.MIN_VALUE, -1 });
            }

            /**
             * Test object creation by name with invocation target exception and return-null failure
             * mode.
             */
            @Test
            public void createByNameInvokationTargetReturnNull() {
                Base base = AccessHelper.Objects.create(NAME_BASE, AccessHelper.Failure.Mode.RETURN_NULL, //
                        FACTORY_TYPES, new Object[] { Long.MIN_VALUE, -1 });
                Assert.assertThat(base, CoreMatchers.nullValue());
            }

            /**
             * Test object creation by name for none existing constructor with default failure mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "invalid method failure [type=" + NAME_BASE + ", types=[java.lang.Long], args=[]]")
            public void createByNameFailureDefault() {
                AccessHelper.Objects.create(NAME_BASE, //
                    new Class<?>[] { Long.class }, new Object[] {});
            }

            /**
             * Test object creation by name for none existing constructor with throw-exception
             * failure mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "invalid method failure [type=" + NAME_BASE + ", types=[], args=[failure]]")
            public void createByNameFailureThrowException() {
                AccessHelper.Objects.create(NAME_BASE, AccessHelper.Failure.Mode.THROW_EXCEPTION, //
                    new Class<?>[] {}, new Object[] { "failure" });
            }

            /**
             * Test object creation by name for none existing constructor with return-null failure
             * mode.
             */
            @Test
            public void createByNameFailureReturnNull() {
                Base base = AccessHelper.Objects.create(NAME_BASE, AccessHelper.Failure.Mode.RETURN_NULL, //
                        new Class<?>[] { String.class }, new Object[] { "failure" });
                Assert.assertThat(base, CoreMatchers.nullValue());
            }

            /**
             * Test object creation by name for none existing constructor with unknown failure mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "mode not supported [" + NAME_UNKNOWN + "]")
            public void createByNameFailureUnkownMode() {
                AccessHelper.Objects.create(NAME_BASE, MODE_FAILURE_UNKNOWN, //
                    new Class<?>[] { String.class }, new Object[] { "failure" });
            }

            /**
             * Test object injection by type.
             */
            @Test
            public void injectByType() {
                Child actual = AccessHelper.Objects.inject(new Child(new Base(0, 0)), null, new Base(1, 2));
                Assert.assertThat(actual, CoreMatchers.notNullValue());
                Assert.assertThat(actual.base, CoreMatchers.is(new Base(1, 2)));
            }

            /**
             * Test object injection by name.
             */
            @Test
            public void injectByName() {
                Child actual = AccessHelper.Objects.inject(new Child(new Base(0, 0)), "base", null);
                Assert.assertThat(actual, CoreMatchers.notNullValue());
                Assert.assertThat(actual.base, CoreMatchers.nullValue());
            }

            /**
             * Test object copy with null target.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "target must not be null")
            public void copyWithNullTarget() {
                AccessHelper.Objects.copy(null, AccessHelper.Failure.Mode.DEFAULT, new Class<?>[] {});
            }
        }

        /**
         * Object access helper copy behavior test.
         */
        public static final class CopyBehavior extends ParameterTest implements Unknown {

            /**
             * Copy test type.
             */
            private final Type type;

            /**
             * Failure handling mode.
             */
            private final AccessHelper.Failure.Mode mode;

            /**
             * Target object.
             */
            private final Object target;

            /**
             * Copy test type.
             */
            private static enum Type {

                BOTH, COPY, CLONE;
            }

            /**
             * Create copy behavior test with given copy test type, failure handling mode, target
             * object, and expected test result/failure.
             *
             * @param  type    copy test type.
             * @param  mode    failure handling mode.
             * @param  target  target object.
             * @param  result  expected test result/failure.
             */
            public CopyBehavior(Type type, AccessHelper.Failure.Mode mode, Object target, Object result) {
                super(result);
                this.type = type;
                this.mode = mode;
                this.target = target;
            }

            /**
             * Conduct copy test with given copy test type, failure handling mode, and given target
             * object.
             *
             * @param   type    copy test type.
             * @param   mode    failure handling mode.
             * @param   target  target object.
             *
             * @return  actual result object.
             */
            private static Object copy(Type type, AccessHelper.Failure.Mode mode, Object target) {
                switch (type) {
                    case BOTH:
                        return AccessHelper.copy(target);

                    case COPY:
                        return AccessHelper.Objects.copy(target, mode);

                    case CLONE:
                        return AccessHelper.Objects.clone(target, mode);

                    default:
                        throw new IllegalArgumentException("invalid test type [" + type + "]");
                }
            }

            /**
             * Create test data.
             *
             * @return  test data.
             */
            @Parameterized.Parameters(name = "{index}: type={0}, mode={1}, target={2}, result={3}")
            public static Iterable<Object[]> data() {
                Builder builder = ParameterTest.builder();

                // check for valid targets
                for (Type type : Type.values()) {
                    List<Object> xlist = new ArrayList<Object>();
                    AccessHelper.builder(xlist).add(1L).add(2L);
                    builder.add(type, AccessHelper.Failure.Mode.DEFAULT, xlist, xlist);

                    Map<Object, Object> map = new HashMap<Object, Object>();
                    AccessHelper.builder(map).add("1", 1L).add("2", 2L);
                    builder.add(type, AccessHelper.Failure.Mode.DEFAULT, map, map);

                    Long[] array = new Long[] { 0L, 1L, null };
                    builder.add(type, AccessHelper.Failure.Mode.DEFAULT, array, array);
                }

                // check for null target
                for (Type type : Type.values()) {
                    builder.add(type, AccessHelper.Failure.Mode.DEFAULT, null,
                        Expect.Builder.create(AccessHelper.Failure.class, "target must not be null"));
                }

                // check for failure types
                for (Type type : EnumSet.of(Type.COPY, Type.CLONE)) {
                    builder.add(type, AccessHelper.Failure.Mode.RETURN_NULL, new Object(), null);
                    for (AccessHelper.Failure.Mode mode
                        : EnumSet.of(AccessHelper.Failure.Mode.DEFAULT, AccessHelper.Failure.Mode.THROW_EXCEPTION)) {
                        builder.add(type, mode, new Object(),
                            Expect.Builder.create(AccessHelper.Failure.class, "type not supported [java.lang.Object]"));
                    }
                    builder.add(type, MODE_FAILURE_UNKNOWN, new Object(),
                        Expect.Builder.create(AccessHelper.Failure.class, "mode not supported [" + NAME_UNKNOWN + "]"));
                }
                builder.add(Type.BOTH, AccessHelper.Failure.Mode.RETURN_NULL, new Object(),
                    Expect.Builder.create(AccessHelper.Failure.class, "type not supported [java.lang.Object]"));
                return builder;
            }

            /**
             * Test object access helper copy behavior.
             */
            @Test
            public void test() { // NOPMD: handled by rule!
                this.rule.actual(CopyBehavior.copy(this.type, this.mode, this.target));
            }
        }
    }

    /**
     * Check property access helper creation and behavior.
     */
    @FixMethodOrder(MethodSorters.JVM)
    @RunWith(BlockJUnit4ClassRunner.class)
    public static final class EnumsBehavior implements Unknown {

        /**
         * Safe days before execution.
         */
        private static final Day[] days = Day.values().clone();

        /**
         * Activate expectation rule.
         */
        @Rule
        public final ExpectRule expect = new ExpectRule();

        /**
         * Test day enumeration.
         */
        private enum Day {

            /**
             * Monday.
             */
            MONDAY("monday"),

            /**
             * Tuesday.
             */
            TUESDAY("thuesday");

            /**
             * Name of day.
             */
            private final String name;

            /**
             * Create day using default name.
             */
            private Day() {
                this.name = this.name().toLowerCase();
            }

            /**
             * Create day using given name.
             *
             * @param  name  name of day.
             */
            private Day(String name) {
                this.name = name;
            }

            /**
             * {@inheritDoc}
             */
            public String toString() {
                return this.name;
            }
        }

        /**
         * Test failure enumeration resolution with default failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "illegal argument failure [type=java.lang.Enum, name=x]",
            cause =
                @Expect.Cause(
                    type = AccessHelper.Failure.class,
                    message = "illegal enum type [java.lang.Enum]"
                )
        )
        @SuppressWarnings("unchecked")
        public void resolveFailureEnumDefaultMode() {
            AccessHelper.Enums.resolve(Enum.class, "x", AccessHelper.Failure.Mode.DEFAULT);
        }

        /**
         * Test failure enumeration resolution with throw exception failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "illegal argument failure [type=java.lang.Enum, name=x]",
            cause =
                @Expect.Cause(
                    type = AccessHelper.Failure.class,
                    message = "illegal enum type [java.lang.Enum]"
                )
        )
        @SuppressWarnings("unchecked")
        public void resolveFailureEnumThrowExceptionMode() {
            AccessHelper.Enums.resolve(Enum.class, "x", AccessHelper.Failure.Mode.THROW_EXCEPTION);
        }

        /**
         * Test failure enumeration resolution with illegal enumeration type.
         */
        @Test
        @SuppressWarnings("unchecked")
        public void resolveFailureEnumWithReturnNullMode() {
            AccessHelper.Enums.resolve(Enum.class, "x", AccessHelper.Failure.Mode.RETURN_NULL);
        }

        /**
         * Test failure enumeration resolution with unknown failure mode.
         */
        @SuppressWarnings("unchecked")
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "mode not supported [" + NAME_UNKNOWN + "]")
        public void resolveFailureEnumUnknownMode() {
            AccessHelper.Enums.resolve(Enum.class, "x", MODE_FAILURE_UNKNOWN);
        }

        /**
         * Test failure enumeration resolution with default failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "unknown enum value [x]")
        public void resolveFailureDayDefaultMode() {
            AccessHelper.Enums.resolve(Day.class, "x", AccessHelper.Failure.Mode.DEFAULT);
        }

        /**
         * Test failure enumeration resolution with throw exception failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "unknown enum value [x]")
        public void resolveFailureDayThrowExceptionMode() {
            AccessHelper.Enums.resolve(Day.class, "x", AccessHelper.Failure.Mode.THROW_EXCEPTION);
        }

        /**
         * Test failure enumeration resolution with return null failure mode.
         */
        @Test
        public void resolveFailureDayReturnNullMode() {
            Day actual = AccessHelper.Enums.resolve(Day.class, "x", AccessHelper.Failure.Mode.RETURN_NULL);
            Assert.assertThat(actual, CoreMatchers.nullValue());
        }

        /**
         * Test failure enumeration resolution with unknown failure mode.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(message = "mode not supported [" + NAME_UNKNOWN + "]")
        public void resolveFailureDayUnknownMode() {
            AccessHelper.Enums.resolve(Day.class, "x", MODE_FAILURE_UNKNOWN);
        }

        /**
         * Test enumeration resolution.
         */
        @Test
        public void resolveDay() {
            Day actual = AccessHelper.Enums.resolve(Day.class, Day.MONDAY.name(),
                    AccessHelper.Failure.Mode.RETURN_NULL);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.isA(Day.class));
            Assert.assertThat(actual, CoreMatchers.is(Day.MONDAY));
        }

        /**
         * Test enumeration creation.
         */
        @Test
        public void createNewDay() {
            Day actual = AccessHelper.Enums.create(Day.class, "WEDNESDAY", Day.values().length, null);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.isA(Day.class));
        }

        /**
         * Test enumeration insertion of additional value.
         */
        @Test
        public void insertNewDay() {
            String name = "my-wednesday";
            Day actual = AccessHelper.Enums.insert(Day.class, "WEDNESDAY", Day.values().length, //
                    new Class[] { String.class }, name);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.isA(Day.class));
            Assert.assertThat(actual.toString(), CoreMatchers.is(name));
            Assert.assertThat(Arrays.asList(Day.values()), CoreMatchers.hasItem(actual));
        }

        /**
         * Test enumeration insert with new value replacing another.
         */
        @Test
        public void insertNewDayReplacingOther() {
            String name = "vacation";
            Day actual = AccessHelper.Enums.insert(Day.class, "VACATION", Day.TUESDAY.ordinal(),
                    new Class[] { String.class }, name);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.isA(Day.class));
            Assert.assertThat(actual.toString(), CoreMatchers.is(name));
            Assert.assertThat(Arrays.asList(Day.values()), CoreMatchers.hasItem(actual));
            Assert.assertThat(Arrays.asList(Day.values()), CoreMatchers.not(CoreMatchers.hasItem(Day.TUESDAY)));
        }

        /**
         * Test enumeration insertion of additional value.
         */
        @Test
        public void insertNewDayInsertedBefore() {
            String name = "my-thuesday";
            Day actual = AccessHelper.Enums.insert(Day.class, "TUESDAY", -1, new Class[] { String.class }, name);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual, CoreMatchers.isA(Day.class));
            Assert.assertThat(actual.toString(), CoreMatchers.is(CoreMatchers.not(name)));
            Assert.assertThat(Arrays.asList(Day.values()), CoreMatchers.hasItem(actual));
        }

        /**
         * Test enumeration update with new enumeration values deleting all other values.
         */
        @Test
        public void updateAllDaysDeletingOriginalDays() {
            Day first = AccessHelper.Enums.create(Day.class, "FIRST", 0, null);
            Day[] actual = AccessHelper.Enums.update(Day.class, new Day[] {}, first);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual.length, CoreMatchers.is(1));
            Assert.assertThat(Arrays.asList(actual), CoreMatchers.hasItems(first));
            Assert.assertThat(actual, CoreMatchers.is(Day.values()));
        }

        /**
         * Test enumeration update with new enumeration values.
         */
        @Test
        public void updateAllDaysMergingOriginalDays() {
            Day second = AccessHelper.Enums.create(Day.class, "SECOND", 1, null);
            Day[] actual = AccessHelper.Enums.update(Day.class, Day.values(), second);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual.length, CoreMatchers.is(2));
            Assert.assertThat(Arrays.asList(actual), CoreMatchers.hasItems(second));
            Assert.assertThat(actual, CoreMatchers.is(Day.values()));
        }

        /**
         * Test enumeration update with new enumeration values.
         */
        @Test
        public void updateAllDaysMergingOriginalDaysWithNone() {
            Day[] actual = AccessHelper.Enums.update(Day.class, Day.values(), (Day[]) null);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual.length, CoreMatchers.is(2));
            Assert.assertThat(actual, CoreMatchers.is(Day.values()));
        }

        /**
         * Test enumeration update with new enumeration values.
         */
        @Test
        public void updateAllDaysMergingOriginalDaysWithSorting() {
            Day friday = AccessHelper.Enums.create(Day.class, "FRIDAY", 4, null);
            Day saturday = AccessHelper.Enums.create(Day.class, "SATURDAY", 5, null);
            Day sunday = AccessHelper.Enums.create(Day.class, "SATURDAY", 6, null);
            Day[] actual = AccessHelper.Enums.update(Day.class, //
                    new Day[] { Day.TUESDAY, Day.MONDAY }, sunday, saturday, friday);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual.length, CoreMatchers.is(7));
            Assert.assertThat(Arrays.asList(actual), CoreMatchers.hasItems(sunday, saturday, friday));
            Assert.assertThat(actual, CoreMatchers.is(Day.values()));
        }

        /**
         * Test enumeration deletion with empty array.
         */
        @Test
        public void deleteAllDaysWithEmptyArrays() {
            Day[] actual = AccessHelper.Enums.update(Day.class, new Day[] {}, new Day[] {});
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual.length, CoreMatchers.is(0));
            Assert.assertThat(actual, CoreMatchers.is(Day.values()));
        }

        /**
         * Test enumeration deletion with null array.
         */
        @Test
        public void deleteAllDaysWithNullArray() {
            Day[] actual = AccessHelper.Enums.update(Day.class);
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual.length, CoreMatchers.is(0));
            Assert.assertThat(actual, CoreMatchers.is(Day.values()));
        }

        /**
         * Rest days after each test.
         */
        @After
        public void after() {
            Day[] actual = AccessHelper.Enums.update(Day.class, EnumsBehavior.days.clone());
            Assert.assertThat(actual, CoreMatchers.notNullValue());
            Assert.assertThat(actual.length, CoreMatchers.is(EnumsBehavior.days.length));
            Assert.assertThat(actual, CoreMatchers.is(Day.values()));
            Assert.assertThat(actual, CoreMatchers.is(EnumsBehavior.days));
        }
    }

    /**
     * Beans access helper behavior.
     */
    @RunWith(Suite.class)
    @Suite.SuiteClasses(
        {
            BeansBehavior.DefaultBehavior.class,
            BeansBehavior.TypeBehavior.class,
            BeansBehavior.FieldBehavior.class,
            BeansBehavior.GetterBehavior.class,
            BeansBehavior.SetterBehavior.class,
            BeansBehavior.ReadBehavior.class,
            BeansBehavior.WriteBehavior.class
        }
    )
    public static final class BeansBehavior {

        /**
         * Beans access helper behavior.
         */
        @FixMethodOrder(MethodSorters.JVM)
        @RunWith(BlockJUnit4ClassRunner.class)
        public static final class DefaultBehavior implements Unknown {

            /**
             * Activate expectation rule.
             */
            @Rule
            public final ExpectRule expect = new ExpectRule();

            /**
             * Test get property failure with null target.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "target must not be null")
            public void getWithNullTarget() {
                AccessHelper.Beans.get(null, "x");
            }

            /**
             * Test get property failure with null name.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "name must not be null or empty [null]")
            public void getWithNullName() {
                AccessHelper.Beans.get(new Base(0, 0), null);
            }

            /**
             * Test get property failure with empty name.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "name must not be null or empty []")
            public void getWithEmptyName() {
                AccessHelper.Beans.get(new Base(0, 0), "");
            }

            /**
             * Test set property failure with null target.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "target must not be null")
            public void setWithNullTarget() {
                AccessHelper.Beans.set(null, "x", null);
            }

            /**
             * Test set property failure with null name.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "name must not be null or empty [null]")
            public void setWithNullName() {
                AccessHelper.Beans.set(new Base(0, 0), null, null);
            }

            /**
             * Test set property failure with empty name.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "name must not be null or empty []")
            public void setWithEmptyName() {
                AccessHelper.Beans.set(new Base(0, 0), "", null);
            }

            /**
             * Test read property failure with null target.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "target must not be null")
            public void readWithNullTarget() {
                AccessHelper.Beans.read(null, "x");
            }

            /**
             * Test read property failure with null name.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "name must not be null or empty [null]")
            public void readWithNullName() {
                AccessHelper.Beans.read(new Base(0, 0), null);
            }

            /**
             * Test read property failure with empty name.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "name must not be null or empty []")
            public void readWithEmptyName() {
                AccessHelper.Beans.read(new Base(0, 0), "");
            }

            /**
             * Test write property failure with null target.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "target must not be null")
            public void writeWithNullTarget() {
                AccessHelper.Beans.write(null, "x", null);
            }

            /**
             * Test write property failure with null name.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "name must not be null or empty [null]")
            public void writeWithNullName() {
                AccessHelper.Beans.write(new Base(0, 0), null, null);
            }

            /**
             * Test write property failure with empty name.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "name must not be null or empty []")
            public void writeWithEmptyName() {
                AccessHelper.Beans.write(new Base(0, 0), "", null);
            }
        }

        /**
         * Beans access helper bean property field behavior test.
         */
        public static final class TypeBehavior extends ParameterTest {

            /**
             * Bean property owner class type.
             */
            private final Class<?> type;

            /**
             * Bean property name to resolve.
             */
            private final String name;

            /**
             * Create type behavior test with given bean property owner class type, bean property
             * name to resolve type for, and test result object.
             *
             * @param  type    bean property owner class type.
             * @param  name    bean property name to resolve.
             * @param  result  expected test result/failure.
             */
            public TypeBehavior(Class<?> type, String name, Object result) {
                super(result);
                this.type = type;
                this.name = name;
            }

            /**
             * Create test data.
             *
             * @return  test data.
             */
            @Parameterized.Parameters(name = "{index}: type={0}, name={1}, result={2}")
            public static Iterable<Object[]> data() {
                Builder builder = ParameterTest.builder();
                builder.add(Base.class, null, Base.class);
                builder.add(Base.class, "", Base.class);
                builder.add(Base.class, "value", long.class);
                builder.add(Child.class, "array", Base[].class);
                builder.add(Child.class, "array,*", Base.class);
                builder.add(Child.class, "array,*,value", long.class);
                builder.add(Child.class, "getOther", long.class);
                builder.add(Child.class, "base,,value", long.class);
                builder.add(Child.class, "list,*,value",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "illegal argument failure [type=" + Child.class.getName() + ", name=list,*,value]") //
                    .cause(AccessHelper.Failure.class, //
                        "property type failure [type=java.util.List, name=*]"));
                builder.add(Child.class, "list," + NAME_BASE + "=*,value", long.class);
                builder.add(Child.class, "x",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "illegal argument failure [type=" + Child.class.getName() + ", name=x]") //
                    .cause(AccessHelper.Failure.class, //
                        "property type failure [type=" + Child.class.getName() + ", name=x]")
                    /*.cause(AccessHelper.Failure.class, "not parameterized type [" + NAME_BASE + "]")*/);
                return builder;
            }

            /**
             * Test beans access helper bean property field behavior.
             */
            @Test
            public void test() { // NOPMD: handled by rule!
                this.rule.actual(AccessHelper.Beans.type(this.type, this.name));
            }
        }

        /**
         * Beans access helper bean property field behavior test.
         */
        public static final class FieldBehavior extends ParameterTest implements Unknown {

            /**
             * Class type of property owner.
             */
            private final Class<?> owner;

            /**
             * Class type of property to find.
             */
            private final Class<?> type;

            /**
             * Field name of property to find.
             */
            private final String name;

            /**
             * Bean property resolution mode.
             */
            private final AccessHelper.Beans.Mode mode;

            /**
             * Create find behavior test with given class type of property owner, class type of
             * property to find, field name of property to find, bean property resolution mode, and
             * expected test result/failure.
             *
             * @param  owner   class type of property owner.
             * @param  type    class type of property to find.
             * @param  name    field name of property to find.
             * @param  mode    bean property resolution mode.
             * @param  result  expected test result/failure.
             */
            public FieldBehavior(Class<?> owner, Class<?> type, String name, AccessHelper.Beans.Mode mode,
                    Object result) {
                super(result);
                this.owner = owner;
                this.type = type;
                this.name = name;
                this.mode = mode;
            }

            /**
             * Create test data.
             *
             * @return  test data.
             */
            @Parameterized.Parameters(name = "{index}: owner={0}, type={1}, name={2}, mode={3}, result={4}")
            public static Iterable<Object[]> data() {
                Builder builder = ParameterTest.builder();
                for (AccessHelper.Beans.Mode mode
                    : EnumSet.of(AccessHelper.Beans.Mode.KNOWN, AccessHelper.Beans.Mode.TYPED)) {
                    builder.add(Base.class, long.class, NAME_VALUE, mode, FIELD_VALUE);
                    builder.add(Base.class, null, NAME_VALUE, mode,
                        Expect.Builder.create(AccessHelper.Failure.class, "type must not be null"));
                    builder.add(Base.class, int.class, NAME_VALUE, mode, null);
                    builder.add(Base.class, long.class, "x", mode, null);
                }
                for (AccessHelper.Beans.Mode mode
                    : EnumSet.of(AccessHelper.Beans.Mode.NAMED, AccessHelper.Beans.Mode.AUTO)) {
                    builder.add(Base.class, long.class, NAME_VALUE, mode, FIELD_VALUE);
                    builder.add(Base.class, null, NAME_VALUE, mode, FIELD_VALUE);
                    builder.add(Base.class, int.class, NAME_VALUE, mode, null);
                    builder.add(Base.class, long.class, "x", mode, null);
                }
                builder.add(Base.class, long.class, "x", MODE_BEAN_UNKNOWN,
                    Expect.Builder.create(AccessHelper.Failure.class, "mode not supported [" + NAME_UNKNOWN + "]"));
                return builder;
            }

            /**
             * Test beans access helper bean property field behavior.
             */
            @Test
            public void test() { // NOPMD: handled by rule!
                this.rule.actual(AccessHelper.Beans.field(this.owner, this.type, this.name, this.mode));
            }
        }

        /**
         * Beans access helper bean property getter behavior test.
         */
        public static final class GetterBehavior extends ParameterTest implements Unknown {

            /**
             * Class type of property owner.
             */
            private final Class<?> owner;

            /**
             * Class type of property to find.
             */
            private final Class<?> type;

            /**
             * Getter name of property to find.
             */
            private final String name;

            /**
             * Bean property resolution mode.
             */
            private final AccessHelper.Beans.Mode mode;

            /**
             * Create find behavior test with given class type of property owner, class type of
             * property to find, field name of property to find, bean property resolution mode, and
             * expected test result/failure.
             *
             * @param  owner   class type of property owner.
             * @param  type    class type of property to find.
             * @param  name    getter name of property to find.
             * @param  mode    bean property resolution mode.
             * @param  result  expected test result/failure.
             */
            public GetterBehavior(Class<?> owner, Class<?> type, String name, AccessHelper.Beans.Mode mode,
                    Object result) {
                super(result);
                this.owner = owner;
                this.type = type;
                this.name = name;
                this.mode = mode;
            }

            /**
             * Create test data.
             *
             * @return  test data.
             */
            @Parameterized.Parameters(name = "{index}: owner={0}, type={1}, name={2}, mode={3}, result={4}")
            public static Iterable<Object[]> data() {
                Builder builder = ParameterTest.builder();
                for (AccessHelper.Beans.Mode mode
                    : EnumSet.of(AccessHelper.Beans.Mode.KNOWN, AccessHelper.Beans.Mode.TYPED)) {
                    builder.add(Base.class, long.class, NAME_VALUE, mode, GETTER_VALUE);
                    builder.add(Base.class, null, NAME_VALUE, mode,
                        Expect.Builder.create(AccessHelper.Failure.class, "type must not be null"));
                    builder.add(Base.class, int.class, NAME_VALUE, mode, null);
                    builder.add(Base.class, long.class, "x", mode, null);
                }
                for (AccessHelper.Beans.Mode mode
                    : EnumSet.of(AccessHelper.Beans.Mode.NAMED, AccessHelper.Beans.Mode.AUTO)) {
                    builder.add(Base.class, long.class, NAME_VALUE, mode, GETTER_VALUE);
                    builder.add(Base.class, null, NAME_VALUE, mode, GETTER_VALUE);
                    builder.add(Base.class, int.class, NAME_VALUE, mode, null);
                    builder.add(Base.class, long.class, "x", mode, null);
                }
                builder.add(Base.class, long.class, "x", MODE_BEAN_UNKNOWN,
                    Expect.Builder.create(AccessHelper.Failure.class, "mode not supported [" + NAME_UNKNOWN + "]"));
                return builder;
            }

            /**
             * Test beans access helper bean property getter behavior.
             */
            @Test
            public void test() { // NOPMD: handled by rule!
                this.rule.actual(AccessHelper.Beans.getter(this.owner, this.type, this.name, this.mode));
            }
        }

        /**
         * Beans access helper bean property setter behavior test.
         */
        public static final class SetterBehavior extends ParameterTest implements Unknown {

            /**
             * Class type of property owner.
             */
            private final Class<?> owner;

            /**
             * Class type of property to find.
             */
            private final Class<?> type;

            /**
             * Setter name of property to find.
             */
            private final String name;

            /**
             * Bean property resolution mode.
             */
            private final AccessHelper.Beans.Mode mode;

            /**
             * Create find behavior test with given class type of property owner, class type of
             * property to find, field name of property to find, bean property resolution mode, and
             * expected test result/failure.
             *
             * @param  owner   class type of property owner.
             * @param  type    class type of property to find.
             * @param  name    setter name of property to find.
             * @param  mode    bean property resolution mode.
             * @param  result  expected test result/failure.
             */
            public SetterBehavior(Class<?> owner, Class<?> type, String name, AccessHelper.Beans.Mode mode,
                    Object result) {
                super(result);
                this.owner = owner;
                this.type = type;
                this.name = name;
                this.mode = mode;
            }

            /**
             * Create test data.
             *
             * @return  test data.
             */
            @Parameterized.Parameters(name = "{index}: owner={0}, type={1}, name={2}, mode={3}, result={4}")
            public static Iterable<Object[]> data() {
                Builder builder = ParameterTest.builder();
                for (AccessHelper.Beans.Mode mode
                    : EnumSet.of(AccessHelper.Beans.Mode.KNOWN, AccessHelper.Beans.Mode.TYPED)) {
                    builder.add(Base.class, long.class, NAME_VALUE, mode, SETTER_VALUE);
                    builder.add(Base.class, null, NAME_VALUE, mode,
                        Expect.Builder.create(AccessHelper.Failure.class, "type must not be null"));
                    builder.add(Base.class, int.class, NAME_VALUE, mode, null);
                    builder.add(Base.class, long.class, "x", mode, null);
                }
                for (AccessHelper.Beans.Mode mode
                    : EnumSet.of(AccessHelper.Beans.Mode.NAMED, AccessHelper.Beans.Mode.AUTO)) {
                    builder.add(Base.class, long.class, NAME_VALUE, mode, SETTER_VALUE);
                    builder.add(Base.class, null, NAME_VALUE, mode, SETTER_VALUE);
                    builder.add(Base.class, int.class, NAME_VALUE, mode, null);
                    builder.add(Base.class, long.class, "x", mode, null);
                }
                builder.add(Base.class, long.class, "x", MODE_BEAN_UNKNOWN,
                    Expect.Builder.create(AccessHelper.Failure.class, "mode not supported [" + NAME_UNKNOWN + "]"));
                return builder;
            }

            /**
             * Test beans access helper bean property setter behavior.
             */
            @Test
            public void test() { // NOPMD: handled by rule!
                this.rule.actual(AccessHelper.Beans.setter(this.owner, this.type, this.name, this.mode));
            }
        }

        /**
         * Beans access helper write behavior test.
         */
        public static final class WriteBehavior extends ParameterTest {

            /**
             * Target object.
             */
            private final Object target;

            /**
             * Target property name.
             */
            private final String name;

            /**
             * Value object.
             */
            private final Object value;

            /**
             * Create write behavior test with given target object, target property name, value
             * object, and expected test result/failure.
             *
             * @param  target  target object.
             * @param  name    target property name.
             * @param  value   value object.
             * @param  result  expected test result/failure.
             */
            public WriteBehavior(Object target, String name, Object value, Object result) {
                super(result);
                this.target = target;
                this.name = name;
                this.value = value;
            }

            /**
             * Create test data.
             *
             * @return  test data.
             */
            @Parameterized.Parameters(name = "{index}: target={0}, name={1}, value={2}, result={3}")
            public static Iterable<Object[]> data() {
                Builder builder = ParameterTest.builder();
                builder.add(WriteBehavior.arrays());
                builder.add(WriteBehavior.lists());
                builder.add(WriteBehavior.maps());
                builder.add(WriteBehavior.colls());
                builder.add(WriteBehavior.beans());
                builder.add(WriteBehavior.chains());
                return builder;
            }

            /**
             * Create array property tests.
             *
             * @return  array property tests.
             */
            private static Builder arrays() {
                Builder builder = ParameterTest.builder();
                builder.add(new Long[] { 0L, 1L, null }, "0", 10L, 0L);
                builder.add(new Long[] { 0L, 1L, null }, "1", 11L, 1L);
                builder.add(new Long[] { 0L, 1L, null }, "2", 12L, null);
                builder.add(new Long[] { 0L, 1L, null }, "3", 13L,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "index out of bounds failure [target=[0, 1, null], name=3, value=13]") //
                    .cause(AccessHelper.Failure.class, "index out of bounds [index=3, size=3]"));
                builder.add(new Long[] { 0L, 1L, null }, "*", 14L,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "number format failure [target=[0, 1, null], name=*, value=14]") //
                    .cause(NumberFormatException.class, "For input string: \"*\""));
                builder.add(new Long[] { 0L, 1L, null }, "1", null, 1L);
                builder.add(new Long[] { 0L, 1L, null }, "2", 15,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "illegal argument failure [target=[0, 1, null], name=2, value=15]") //
                    .cause(IllegalArgumentException.class, "array element type mismatch"));
                return builder;
            }

            /**
             * Create list property tests.
             *
             * @return  list property tests.
             */
            private static Builder lists() {
                Builder builder = ParameterTest.builder();
                for (Class<?> type : new Class<?>[] { ArrayList.class }) {
                    Iterable<?> iterable = (Iterable<?>) AccessHelper.Objects.create(type, null);
                    AccessHelper.builder(iterable).add(0L).add(1L).build();
                    builder.add(AccessHelper.copy(iterable), "0", 10L, 0L);
                    builder.add(AccessHelper.copy(iterable), "1", 11L, 1L);
                    builder.add(AccessHelper.copy(iterable), "2", 12L,
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "index out of bounds failure [target=[0, 1], name=2, value=12]") //
                        .cause(AccessHelper.Failure.class, "index out of bounds [index=2, size=2]"));
                    builder.add(AccessHelper.copy(iterable), "*", 13L, null);
                    builder.add(AccessHelper.copy(iterable), "1", null, 1L);
                }
                return builder;
            }

            /**
             * Create map property tests.
             *
             * @return  map property tests.
             */
            private static Builder maps() {
                Builder builder = ParameterTest.builder();
                for (String name : new String[] {
                        Long.class.getSimpleName(), Long.class.getName(),
                        Integer.class.getSimpleName(), Integer.class.getName(),
                        Short.class.getSimpleName(), Short.class.getName(),
                        Byte.class.getSimpleName(), Byte.class.getName(),
                    }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=0", 0L).add("1", 1L);
                    builder.add(AccessHelper.copy(map), "2", 2L, null);
                    builder.add(AccessHelper.copy(map), "1", 10L, 1L);
                    builder.add(AccessHelper.copy(map), name + "=1", 10L, 1L);
                    builder.add(AccessHelper.copy(map), "0x01", 10L, 1L);
                    builder.add(AccessHelper.copy(map), "#01", 10L, 1L);
                    builder.add(AccessHelper.copy(map), "01", 10L, 1L);
                    builder.add(AccessHelper.copy(map), "*", 11L,
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "number format failure [target={0=0, 1=1}, name=*, value=11]") //
                        .cause(NumberFormatException.class, "For input string: \"*\""));
                    builder.add(AccessHelper.copy(map), "1", null, 1L);
                }
                for (String name : new String[] {
                        Double.class.getSimpleName(), Double.class.getName(),
                        Float.class.getSimpleName(), Float.class.getName(),
                    }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=0", 0L).add("1", 1L);
                    builder.add(AccessHelper.copy(map), "2", 2L, null);
                    builder.add(AccessHelper.copy(map), "1", 10L, 1L);
                    builder.add(AccessHelper.copy(map), name + "=1", 10L, 1L);
                    builder.add(AccessHelper.copy(map), "*", 11L,
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "number format failure [target={0.0=0, 1.0=1}, name=*, value=11]") //
                        .cause(NumberFormatException.class, "For input string: \"*\""));
                    builder.add(AccessHelper.copy(map), "1", null, 1L);
                }
                for (String name : new String[] { BigInteger.class.getName(), BigDecimal.class.getName() }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=0", 0L).add("1", 1L);
                    builder.add(AccessHelper.copy(map), "2", 2L, null);
                    builder.add(AccessHelper.copy(map), "1", 10L, 1L);
                    builder.add(AccessHelper.copy(map), name + "=1", 10L, 1L);
                    builder.add(AccessHelper.copy(map), "*", 11L,
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "number format failure [target={0=0, 1=1}, name=*, value=11]") //
                        .cause(NumberFormatException.class));
                    builder.add(AccessHelper.copy(map), "1", null, 1L);
                }
                for (String name : new String[] { AtomicInteger.class.getName(), AtomicLong.class.getName() }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    builder.add(map, name + "=1", 11L,
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "invalid class failure [target={}, name=" + name + "=1, value=11]") //
                        .cause(AccessHelper.Failure.class, "type not supported [" + name + "]"));
                }
                for (String name : new String[] {
                        Character.class.getSimpleName(), Character.class.getName(),
                        String.class.getSimpleName(), String.class.getName()
                    }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=0", 0L).add("1", 1L);
                    builder.add(AccessHelper.copy(map), "2", 2L, null);
                    builder.add(AccessHelper.copy(map), "1", 10L, 1L);
                    builder.add(AccessHelper.copy(map), "*", 11L, null);
                    builder.add(AccessHelper.copy(map), "1", null, 1L);
                }
                for (String name : new String[] { Boolean.class.getSimpleName(), Boolean.class.getName(), }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=true", 0L).add("false", 1L);
                    builder.add(AccessHelper.copy(map), "true", 10L, 0L);
                    builder.add(AccessHelper.copy(map), "true", null, 0L);
                    builder.add(AccessHelper.copy(map), "false", 11L, 1L);
                    builder.add(AccessHelper.copy(map), "*", 11L, 1L);
                    builder.add(AccessHelper.copy(map), "*", null, 1L);
                }
                for (String name : new String[] { Class.class.getSimpleName(), Class.class.getName() }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=long", 0L).add("int", 1L);
                    builder.add(AccessHelper.copy(map), "short", 2L, null);
                    builder.add(AccessHelper.copy(map), "long", 10L, 0L);
                    builder.add(AccessHelper.copy(map), "*", 11L,
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "invalid class failure [target={long=0, int=1}, name=*, value=11]") //
                        .cause(AccessHelper.Failure.class, "invalid class failure [name=*]") //
                        .cause(ClassNotFoundException.class, "*"));
                    builder.add(AccessHelper.copy(map), "long", null, 0L);
                }
                for (String name : new String[] { NAME_BASE }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=0", 0L).add("1", 1L);
                    builder.add(AccessHelper.copy(map), "2", 2L, null);
                    builder.add(AccessHelper.copy(map), "1", 10L, 1L);
                    builder.add(AccessHelper.copy(map), "*", 11L,
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "invocation target failure [target=" + map + ", name=*, value=11]") //
                        .cause(AccessHelper.Failure.class,
                            "invocation target failure [type=" + NAME_BASE + ", types=[java.lang.String], args=[*]]") //
                        .cause(NumberFormatException.class, "For input string: \"*\""));
                    builder.add(AccessHelper.copy(map), "1", null, 1L);
                }
                return builder;
            }

            /**
             * Create iterable and collection property tests.
             *
             * @return  iterable and collection property tests.
             */
            private static Builder colls() {
                Builder builder = ParameterTest.builder();
                for (Class<?> type : new Class<?>[] { LinkedHashSet.class, ArrayDeque.class }) {
                    Iterable<?> iterable = (Iterable<?>) AccessHelper.Objects.create(type, null);
                    AccessHelper.builder(iterable).add("x").add("y").add(2L).build();
                    builder.add(AccessHelper.copy(iterable), "0", 10L, "x");
                    builder.add(AccessHelper.copy(iterable), "1", 11L, "y");
                    builder.add(AccessHelper.copy(iterable), "3", 12L,
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "index out of bounds failure [target=" + iterable + ", name=3, value=12]") //
                        .cause(AccessHelper.Failure.class, "index out of bounds [index=3, size=3]"));
                    builder.add(AccessHelper.copy(iterable), "*", 13L, null);
                    builder.add(AccessHelper.copy(iterable), "1", null, "y");
                    builder.add(AccessHelper.copy(iterable), "Long=3", 14L, null);
                    builder.add(AccessHelper.copy(iterable), "Long=2", null, 2L);
                    builder.add(AccessHelper.copy(iterable), "2", null, 2L);
                    builder.add(AccessHelper.copy(iterable), "x", null, "x");
                    builder.add(AccessHelper.copy(iterable), "x", "a", "x");
                }
                builder.add(new HashSet<Object>(), "x", null,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "illegal argument failure [target=[], name=x, value=null]") //
                    .cause(AccessHelper.Failure.class, "could not determine type"));

                builder.add(new Child(1, 2, 3), "value", 4L, 1L);
                Child child = new Child(-1, 2, 3);
                builder.add(child, "value", 5L,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", name=value, value=5]") //
                    .cause(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", method=value, args=[]]") //
                    .cause(NullPointerException.class));
                builder.add(new Child(1, 2, 3, (Base[]) null), "list", new ArrayList<Base>(), null);
                child = new Child(1, 2, 3, new Base[] { new Base(0, 0), new Base(1, 1) });
                builder.add(AccessHelper.copy(child), "0", null, new Base(0, 0));
                builder.add(AccessHelper.copy(child), "1", null, new Base(1, 1));
                builder.add(AccessHelper.copy(child), "1", new Base(0, 0), new Base(1, 1));
                builder.add(AccessHelper.copy(child), "2", null,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "index out of bounds failure [target=" + child + ", name=2, value=null]") //
                    .cause(AccessHelper.Failure.class, "index out of bounds [index=2, size=2]"));
                return builder;
            }

            /**
             * Create bean property tests.
             *
             * @return  bean property tests.
             */
            private static Builder beans() {
                Builder builder = ParameterTest.builder();
                builder.add(new Base(1, 2), "*", null,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "illegal argument failure [target=" + new Base(1, 2) + ", name=*, value=null]") //
                    .cause(AccessHelper.Failure.class, "invalid property [type=null, name=*]"));
                builder.add(new Base(1, 2), "value", 3L, 1L);
                builder.add(new Base(1, 2), "long=value", 4L, 1L);
                builder.add(new Base(1, 2), "Long=value", 5L,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "illegal argument failure [target=" + new Base(1, 2) + ", name=Long=value, value=5]") //
                    .cause(AccessHelper.Failure.class, "invalid property [type=java.lang.Long, name=value]"));

                builder.add(new Base(1, 2), "integer", 6, 2);
                builder.add(new Child(1, 2, 3), "value", 7L, 1L);
                builder.add(new Child(1, 2, 3), "long=value", 8L, 1L);
                builder.add(new Child(1, 2, 3), "Long=value", 9L, 1L);
                Child child = new Child(-1, 2, 3);
                builder.add(new Child(-1, 2, 3), "value", 10L,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", name=value, value=10]") //
                    .cause(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", method=value, args=[]]") //
                    .cause(NullPointerException.class));
                builder.add(new Child(-1, 2, 3), "long=value", 11L,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", name=long=value, value=11]") //
                    .cause(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", method=value, args=[]]") //
                    .cause(NullPointerException.class));
                builder.add(new Child(-1, 2, 3), "Long=value", 12L, null);
                return builder;
            }

            /**
             * Create chained name property tests.
             *
             * @return  chained name property tests.
             */
            private static Builder chains() {
                Builder builder = ParameterTest.builder();
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "0", new Long[] { 0L },
                    new Long[] { 0L, 1L });
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "1", new Long[] { 1L }, null);
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "0,0", 10L, 0L);
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "0,1", 11L, 1L);
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "0,,1", 12L, 1L);
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "0[1]", 13L, 1L);
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "0[][1]", 14L, 1L);
                builder.add(new Object[] { new Object[] { 0L, 1L }, null }, "0,,2", 15L,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "index out of bounds failure [target=[[0, 1], null], name=0,,2, value=15]") //
                    .cause(AccessHelper.Failure.class, "index out of bounds [index=2, size=2]"));
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "1,1", null,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "illegal argument failure [target=[[0, 1], null], name=1,1, value=null]") //
                    .cause(AccessHelper.Failure.class, "target must not be null"));
                builder.add(new Object[][] { new Object[] { new Long[] { 0L, 1L } } }, "0[0,1]", 16L, 1L);
                builder.add(new Object[][] { new Object[] { new Long[] { 0L, 1L } } }, "0,0[1]", 17L, 1L);
                builder.add(new Object[][] { new Object[] { new Long[] { 0L, 1L } } }, "0[][0],1", 17L, 1L);
                return builder;
            }

            /**
             * Test beans access helper write behavior.
             */
            @Test
            public void test() { // NOPMD: handled by rule!
                this.rule.actual(AccessHelper.Beans.write(this.target, this.name, this.value));
            }
        }

        /**
         * Beans access helper read behavior test.
         */
        public static final class ReadBehavior extends ParameterTest {

            /**
             * Target object.
             */
            private final Object target;

            /**
             * Target property name.
             */
            private final String name;

            /**
             * Create write behavior test with given target object, target property name, and
             * expected test result/failure.
             *
             * @param  target  target object.
             * @param  name    target property name.
             * @param  result  expected test result/failure.
             */
            public ReadBehavior(Object target, String name, Object result) {
                super(result);
                this.target = target;
                this.name = name;
            }

            /**
             * Create test data.
             *
             * @return  test data.
             */
            @Parameterized.Parameters(name = "{index}: target={0}, name={1}, result={2}")
            public static Iterable<Object[]> data() {
                Builder builder = ParameterTest.builder();
                builder.add(ReadBehavior.arrays());
                builder.add(ReadBehavior.lists());
                builder.add(ReadBehavior.maps());
                builder.add(ReadBehavior.colls());
                builder.add(ReadBehavior.beans());
                builder.add(ReadBehavior.chains());

                // test to cover pretty printing!!
                builder.add(
                    new Object[] {
                        null, new float[] { 0.0f }, new double[] { 2.0 }, new char[] { 'a', '*' },
                        new long[] { 1 }, new int[] { 2 }, new short[] { 3 }, new byte[] { 4 },
                        new boolean[] { true, false }, new Class<?>[] { Base.class, Object.class, null },
                        new Method[] { GETTER_VALUE, SETTER_VALUE, null },
                        new Field[] { FIELD_VALUE, FIELD_VALUE, null },
                        Base.class, GETTER_VALUE, SETTER_VALUE, FIELD_VALUE, null
                    }, "0,1",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "illegal argument failure [target=[null, [0.0], [2.0], [a, *], [1], [2], [3], [4], [true, false], ["
                        + NAME_BASE + ", java.lang.Object, null], [value, value, null], [value, value, null], "
                        + NAME_BASE + ", value, value, value, null], name=0,1]") //
                    .cause(AccessHelper.Failure.class, "target must not be null"));

                // test to cover tree object builder!!
                builder.add(AccessHelper.builder(new ArrayList<Object>()).add(new ArrayList<Object>()) //
                    .push(new ArrayList<Object>()).add("1").pop().push("0").add("0").build(), "0,1,1",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "index out of bounds failure [target=[[0], [1]], name=0,1,1]") //
                    .cause(AccessHelper.Failure.class, "index out of bounds [index=1, size=1]"));
                return builder;
            }

            /**
             * Create array property tests.
             *
             * @return  array property tests.
             */
            private static Builder arrays() {
                Builder builder = ParameterTest.builder();
                builder.add(new Object[] { 0L, 1L, null }, "0", 0L);
                builder.add(new Object[] { 0L, 1L, null }, "1", 1L);
                builder.add(new Object[] { 0L, 1L, null }, "2", null);
                builder.add(new Object[] { 0L, 1L, null }, "3",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "index out of bounds failure [target=[0, 1, null], name=3]") //
                    .cause(AccessHelper.Failure.class, "index out of bounds [index=3, size=3]"));
                builder.add(new Long[] { 0L, 1L, null }, "*",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "number format failure [target=[0, 1, null], name=*]") //
                    .cause(NumberFormatException.class, "For input string: \"*\""));
                builder.add(new Long[] { 0L, 1L }, "2",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "index out of bounds failure [target=[0, 1], name=2]") //
                    .cause(AccessHelper.Failure.class, "index out of bounds [index=2, size=2]"));
                return builder;
            }

            /**
             * Create list property tests.
             *
             * @return  list property tests.
             */
            private static Builder lists() {
                Builder builder = ParameterTest.builder();
                for (Class<?> type : new Class<?>[] { ArrayList.class }) {
                    Iterable<?> iterable = (Iterable<?>) AccessHelper.Objects.create(type, null);
                    AccessHelper.builder(iterable).add(0L).add(1L).build();
                    builder.add(AccessHelper.copy(iterable), "0", 0L);
                    builder.add(AccessHelper.copy(iterable), "1", 1L);
                    builder.add(AccessHelper.copy(iterable), "2",
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "index out of bounds failure [target=[0, 1], name=2]") //
                        .cause(AccessHelper.Failure.class, "index out of bounds [index=2, size=2]"));
                    builder.add(AccessHelper.copy(iterable), "*",
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "number format failure [target=[0, 1], name=*]") //
                        .cause(NumberFormatException.class, "For input string: \"*\""));
                }
                return builder;
            }

            /**
             * Create map property tests.
             *
             * @return  map property tests.
             */
            private static Builder maps() {
                Builder builder = ParameterTest.builder();
                builder.add(new LinkedHashMap<Object, Object>(), "0", null);
                for (String name : new String[] {
                        Long.class.getSimpleName(), Long.class.getName(),
                        Integer.class.getSimpleName(), Integer.class.getName(),
                        Short.class.getSimpleName(), Short.class.getName(),
                        Byte.class.getSimpleName(), Byte.class.getName()
                    }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=0", 0L).add("1", 1L);
                    builder.add(AccessHelper.copy(map), name + "=1", 1L);
                    builder.add(AccessHelper.copy(map), "1", 1L);
                    builder.add(AccessHelper.copy(map), "0x01", 1L);
                    builder.add(AccessHelper.copy(map), "#01", 1L);
                    builder.add(AccessHelper.copy(map), "01", 1L);
                    builder.add(AccessHelper.copy(map), "*",
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "number format failure [target={0=0, 1=1}, name=*]") //
                        .cause(NumberFormatException.class, "For input string: \"*\""));
                }
                for (String name : new String[] {
                        Double.class.getSimpleName(), Double.class.getName(),
                        Float.class.getSimpleName(), Float.class.getName()
                    }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=0", 0L).add("1", 1L);
                    builder.add(AccessHelper.copy(map), name + "=1", 1L);
                    builder.add(AccessHelper.copy(map), "1", 1L);
                    builder.add(AccessHelper.copy(map), "*",
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "number format failure [target={0.0=0, 1.0=1}, name=*]") //
                        .cause(NumberFormatException.class, "For input string: \"*\""));
                }
                for (String name : new String[] { BigInteger.class.getName(), BigDecimal.class.getName() }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=0", 0L).add("1", 1L);
                    builder.add(AccessHelper.copy(map), name + "=1", 1L);
                    builder.add(AccessHelper.copy(map), "1", 1L);
                    builder.add(AccessHelper.copy(map), "*",
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "number format failure [target={0=0, 1=1}, name=*]") //
                        .cause(NumberFormatException.class));
                }
                for (String name : new String[] { AtomicInteger.class.getName(), AtomicLong.class.getName() }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    builder.add(map, name + "=1",
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "invalid class failure [target={}, name=" + name + "=1]") //
                        .cause(AccessHelper.Failure.class, "type not supported [" + name + "]"));
                }
                for (String name : new String[] {
                        Character.class.getSimpleName(), Character.class.getName(),
                        String.class.getSimpleName(), String.class.getName()
                    }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=0", 0L).add("1", 1L);
                    builder.add(AccessHelper.copy(map), "1", 1L);
                    builder.add(AccessHelper.copy(map), "*", null);
                }
                for (String name : new String[] { Boolean.class.getSimpleName(), Boolean.class.getName() }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=true", 0L).add("false", 1L);
                    builder.add(AccessHelper.copy(map), "true", 0L);
                    builder.add(AccessHelper.copy(map), "false", 1L);
                    builder.add(AccessHelper.copy(map), "*", 1L);
                }
                for (String name : new String[] { Class.class.getSimpleName(), Class.class.getName() }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=long", 0L).add("int", 1L);
                    builder.add(AccessHelper.copy(map), "short", null);
                    builder.add(AccessHelper.copy(map), "long", 0L);
                    builder.add(AccessHelper.copy(map), "*",
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "invalid class failure [target={long=0, int=1}, name=*]") //
                        .cause(AccessHelper.Failure.class, "invalid class failure [name=*]") //
                        .cause(ClassNotFoundException.class, "*"));
                }
                for (String name : new String[] { NAME_BASE }) {
                    Map<Object, Object> map = new LinkedHashMap<Object, Object>();
                    AccessHelper.builder(map).add(name + "=0", 0L).add("1", 1L);
                    builder.add(AccessHelper.copy(map), "1", 1L);
                    builder.add(AccessHelper.copy(map), "*",
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "invocation target failure [target=" + map + ", name=*]") //
                        .cause(AccessHelper.Failure.class,
                            "invocation target failure [type=" + NAME_BASE + ", types=[java.lang.String], args=[*]]") //
                        .cause(NumberFormatException.class, "For input string: \"*\""));
                }
                return builder;
            }

            /**
             * Create iterable and collection property tests.
             *
             * @return  iterable and collection property tests.
             */
            private static Builder colls() {
                Builder builder = ParameterTest.builder();
                for (Class<?> type : new Class<?>[] { LinkedHashSet.class, ArrayDeque.class }) {
                    Iterable<?> iterable = (Iterable<?>) AccessHelper.Objects.create(type, null);
                    AccessHelper.builder(iterable).add("x").add("y").add(2L).build();
                    builder.add(iterable, "0", "x");
                    builder.add(iterable, "1", "y");
                    builder.add(iterable, "3",
                        Expect.Builder.create(AccessHelper.Failure.class,
                            "index out of bounds failure [target=" + iterable + ", name=3]") //
                        .cause(AccessHelper.Failure.class, "index out of bounds [index=3, size=3]"));
                    builder.add(iterable, "*", null);
                    builder.add(iterable, "Long=3", null);
                    builder.add(iterable, "Long=2", 2L);
                    builder.add(iterable, "2", 2L);
                    builder.add(iterable, "x", "x");
                }
                builder.add(new HashSet<Object>(), "x",
                    Expect.Builder.create(AccessHelper.Failure.class, "illegal argument failure [target=[], name=x]") //
                    .cause(AccessHelper.Failure.class, "could not determine type"));

                builder.add(new Child(1, 2, 3), "value", 1L);
                Child child = new Child(-1, 2, 3);
                builder.add(child, "value",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", name=value]") //
                    .cause(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", method=value, args=[]]") //
                    .cause(NullPointerException.class));
                builder.add(new Child(1, 2, 3, (Base[]) null), "list", null);
                child = new Child(1, 2, 3, new Base[] { new Base(0, 0), new Base(1, 1) });
                builder.add(child, "0", new Base(0, 0));
                builder.add(child, "1", new Base(1, 1));
                return builder;
            }

            /**
             * Create bean property tests.
             *
             * @return  bean property tests.
             */
            private static Builder beans() {
                Builder builder = ParameterTest.builder();
                builder.add(new Base(1, 2), "*",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "illegal argument failure [target=" + new Base(1, 2) + ", name=*]") //
                    .cause(AccessHelper.Failure.class, "invalid property [type=null, name=*]"));
                builder.add(new Base(1, 2), "value", 1L);
                builder.add(new Base(1, 2), "long=value", 1L);
                builder.add(new Base(1, 2), "Long=value",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "illegal argument failure [target=" + new Base(1, 2) + ", name=Long=value]") //
                    .cause(AccessHelper.Failure.class, "invalid property [type=java.lang.Long, name=value]"));

                builder.add(new Base(1, 2), "integer", 2);
                builder.add(new Child(1, 2, 3), "value", 1L);
                builder.add(new Child(1, 2, 3), "long=value", 1L);
                builder.add(new Child(1, 2, 3), "Long=value", 1L);
                Child child = new Child(-1, 2, 3);
                builder.add(new Child(-1, 2, 3), "value",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", name=value]") //
                    .cause(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", method=value, args=[]]") //
                    .cause(NullPointerException.class));
                builder.add(new Child(-1, 2, 3), "long=value",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", name=long=value]") //
                    .cause(AccessHelper.Failure.class,
                        "invocation target failure [target=" + child + ", method=value, args=[]]") //
                    .cause(NullPointerException.class));
                builder.add(new Child(-1, 2, 3), "Long=value", null);
                return builder;
            }

            /**
             * Create chain property tests.
             *
             * @return  chain property tests.
             */
            private static Builder chains() {
                Builder builder = ParameterTest.builder();
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "0", new Long[] { 0L, 1L });
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "1", null);
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "0,0", 0L);
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "0,,1", 1L);
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "0[1]", 1L);
                builder.add(new Object[][] { new Long[] { 0L, 1L }, null }, "0[][1]", 1L);
                builder.add(new Object[] { new Object[] { 0L, 1L }, null }, "0,,2",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "index out of bounds failure [target=[[0, 1], null], name=0,,2]") //
                    .cause(AccessHelper.Failure.class, "index out of bounds [index=2, size=2]"));
                builder.add(new Object[] { new Object[] { 0L, 1L }, null }, "1,1",
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "illegal argument failure [target=[[0, 1], null], name=1,1]") //
                    .cause(AccessHelper.Failure.class, "target must not be null"));
                return builder;
            }

            /**
             * Test beans access helper read behavior.
             */
            @Test
            public void test() { // NOPMD: handled by rule!
                this.rule.actual(AccessHelper.Beans.read(this.target, this.name));
            }
        }
    }

    /**
     * Annotation implementation getter/setter test class.
     */
    protected static class AnnoImpl implements Anno {

        /**
         * Primitive long value.
         */
        private final long value; // NOPMD: personal style!

        /**
         * Create annotation implementation getter/setter test class.
         *
         * @param  value  primitive long value.
         */
        public AnnoImpl(long value) {
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        public Class<? extends Annotation> annotationType() {
            return Anno.class;
        }

        /**
         * {@inheritDoc}
         */
        public long value() {
            return this.value;
        }
    }

    /**
     * Annotation getter/setter test class.
     */
    @Retention(RetentionPolicy.RUNTIME)
    protected static @interface Anno {

        /**
         * Primitive long value.
         */
        public long value() default 0L;
    }
}
