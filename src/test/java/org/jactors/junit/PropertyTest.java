package org.jactors.junit;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;

import org.jactors.junit.helper.AccessHelper;
import org.jactors.junit.helper.BeanHelper;
import org.jactors.junit.rule.ExpectRule;
import org.jactors.junit.test.SimpleTest;
import org.jactors.junit.theory.BeanTheory;
import org.jactors.junit.theory.ObjectTheory;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Suite;

/**
 * Property test.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
    {
        PropertyTest.CheckBehavior.class,
        PropertyTest.CheckBeanTheory.class,
        PropertyTest.AtomBehavior.class,
        PropertyTest.AtomObjectTheory.class
    }
)
public final class PropertyTest implements EnumTest.Unknown {

    /**
     * Private atom class type.
     */
    protected static final Class<Object> CLASS_ATOM = //
        AccessHelper.Classes.resolve(Property.Checker.class, "Atom");

    /**
     * Private atom constructor argument types.
     */
    @SuppressWarnings("rawtypes")
    protected static final Class[] TYPES_ATOM = new Class[] {
            BeanHelper.Property.class, BeanHelper.Accessor.class, Expect.Rule.class, Object.class
        };

    /**
     * Field for value.
     */
    private static final Field FIELD_VALUE = //
        AccessHelper.Fields.resolve(Primitive.class, "value");

    /**
     * Property description for value.
     */
    protected static final BeanHelper.Property<Long> PROPERTY_VALUE = //
        BeanHelper.create(FIELD_VALUE);

    /**
     * Property access helper for value.
     */
    protected static final BeanHelper.Accessor<Long> ACCESS_VALUE = //
        BeanHelper.create(Primitive.class, PROPERTY_VALUE);

    /**
     * Field for integer.
     */
    private static final Field FIELD_INTEGER = //
        AccessHelper.Fields.resolve(Primitive.class, "integer");

    /**
     * Property description for integer.
     */
    protected static final BeanHelper.Property<Integer> PROPERTY_INTEGER = //
        BeanHelper.create(FIELD_INTEGER);

    /**
     * Property access helper for integer.
     */
    protected static final BeanHelper.Accessor<Integer> ACCESS_INTEGER = //
        BeanHelper.create(Primitive.class, PROPERTY_INTEGER);

    /**
     * Property based interface.
     */
    private static interface Value {

        /**
         * None-conforming getter for long value.
         *
         * @return  long value.
         */
        public long value();

        /**
         * None-conforming setter for long value.
         *
         * @param  value  long value.
         */
        public void value(long value);
    }

    /**
     * Primitive bean getter/setter test class.
     */
    @SuppressWarnings("unused")
    private static class Primitive implements Value {

        /**
         * Primitive long value.
         */
        private long value;

        /**
         * Primitive integer value.
         */
        private int integer;

        /**
         * Primitive short value.
         */
        private short shorts;

        /**
         * Primitive byte value.
         */
        private byte bytes;

        /**
         * Primitive float value.
         */
        private float floats;

        /**
         * Primitive double value.
         */
        private double doubles;

        /**
         * Primitive boolean value.
         */
        private boolean active;

        /**
         * Primitive character value.
         */
        private char chars;

        /**
         * Create new primitive value with given boolean value and given character value.
         *
         * @param  active  boolean value.
         * @param  chars   character value.
         */
        public Primitive(boolean active, char chars) {
            this.active = active;
            this.chars = chars;
        }

        /**
         * {@inheritDoc}
         */
        public void value(long value) {
            this.value = value;
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
        public long getValues() {
            return this.value;
        }

        /**
         * None-conforming setter for long value with return value.
         *
         * @param   value  long value.
         *
         * @return  same long value.
         */
        public long setValues(long value) { // CHECKSTYLE:OFF intentional for testing.
            return this.value = value; // CHECKSTYLE:ON
        }

        /**
         * Getter for integer value.
         *
         * @return  integer value
         */
        public int getInteger() {
            return this.integer;
        }

        /**
         * Setter for integer value.
         *
         * @param  integer  integer value.
         */
        public void setInteger(int integer) {
            this.integer = integer;
        }

        /**
         * Getter for short value.
         *
         * @return  short value.
         */
        public short getShorts() {
            return this.shorts;
        }

        /**
         * Setter for byte value.
         *
         * @param  bytes  byte value.
         */
        public void setBytes(byte bytes) {
            this.bytes = bytes;
        }

        /**
         * Getter for float value.
         *
         * @return  float value.
         */
        public float getFloats() {
            return this.floats;
        }

        /**
         * Getter for double value.
         *
         * @return  double value.
         */
        public double getDoubles() {
            return this.doubles;
        }

        /**
         * Getter for character value.
         *
         * @return  character value.
         */
        public char getChars() {
            return this.chars;
        }

        /**
         * Setter for character value.
         *
         * @param  chars  character value.
         */
        public void setChars(char chars) {
            if ((chars == 'x') || (chars == 'X')) {
                throw new IllegalArgumentException("must not be 'x' or 'X' [" + chars + "]");
            }
            this.chars = chars;
        }

        /**
         * Getter for boolean value.
         *
         * @return  boolean value.
         */
        public boolean isActive() {
            return this.active;
        }

        /**
         * Setter for boolean value.
         *
         * @param  active  boolean value.
         */
        public void setActive(boolean active) {
            this.active = active;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "Primitive[value=" + this.value + ", integer=" + this.integer + ", shorts=" + this.shorts
                + ", bytes=" + this.bytes + ", floats=" + this.floats + ", doubles=" + this.doubles + ", active="
                + this.active + ", chars=" + this.chars + "]";
        }
    }

    /**
     * Primitive bean getter/setter test class.
     */
    @SuppressWarnings("unused")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings
    private static class Boxed {

        /**
         * Primitive long value.
         */
        private Long value = null;

        /**
         * Primitive integer value.
         */
        private Integer integer = null;

        /**
         * Primitive short value.
         */
        private Short shorts = null;

        /**
         * Primitive byte value.
         */
        private Byte bytes = null;

        /**
         * Primitive float value.
         */
        private Float floats = null;

        /**
         * Primitive double value.
         */
        private Double doubles = null;

        /**
         * Primitive boolean value.
         */
        private Boolean active = null;

        /**
         * Primitive character value.
         */
        private Character chars = null;

        /**
         * Create default boxed value.
         */
        public Boxed() {
            this(Boolean.TRUE, 'X');
        }

        /**
         * Create boxed value with given boolean value and given character value.
         *
         * @param  active  boolean value.
         * @param  chars   character value.
         */
        public Boxed(Boolean active, Character chars) {
            this.active = active;
            this.chars = chars;
        }

        /**
         * Getter for long value.
         *
         * @return  long value.
         */
        public Long getValues() {
            return this.value;
        }

        /**
         * None-conforming setter for long value with return value.
         *
         * @param   value  long value.
         *
         * @return  same long value.
         */
        public Long setValues(Long value) { // CHECKSTYLE:OFF intentional for testing.
            return this.value = value; // CHECKSTYLE:ON
        }

        /**
         * Getter for integer value.
         *
         * @return  integer value
         */
        public Integer getInteger() {
            return this.integer;
        }

        /**
         * Setter for integer value.
         *
         * @param  integer  integer value.
         */
        public void setInteger(Integer integer) {
            this.integer = integer;
        }

        /**
         * Getter for short value.
         *
         * @return  short value.
         */
        public Short getShorts() {
            return this.shorts;
        }

        /**
         * Setter for byte value.
         *
         * @param  bytes  byte value.
         */
        public void setBytes(Byte bytes) {
            this.bytes = bytes;
        }

        /**
         * Getter for float value.
         *
         * @return  float value.
         */
        public Float getFloats() {
            return this.floats;
        }

        /**
         * Getter for double value.
         *
         * @return  double value.
         */
        public Double getDoubles() {
            return this.doubles;
        }

        /**
         * Setter for character value.
         *
         * @param  chars  character value.
         */
        public void setChars(Character chars) {
            if ((chars != null) && ((chars == 'x') || (chars == 'X'))) {
                throw new IllegalArgumentException("must not be 'x' or 'X' [" + chars + "]");
            }
            this.chars = chars;
        }

        /**
         * Getter for boolean value.
         *
         * @return  boolean value.
         */
        public Boolean isActive() {
            return this.active;
        }

        /**
         * Setter for boolean value.
         *
         * @param  active  boolean value.
         */
        public void setActive(Boolean active) {
            this.active = active;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "Boxed[value=" + this.value + ", integer=" + this.integer + ", shorts=" + this.shorts + ", bytes="
                + this.bytes + ", floats=" + this.floats + ", doubles=" + this.doubles + ", active=" + this.active
                + ", chars=" + this.chars + "]";
        }
    }

    /**
     * Complex bean getter/setter test class.
     */
    @SuppressWarnings("unused")
    @edu.umd.cs.findbugs.annotations.SuppressWarnings
    private static class Complex extends Primitive {

        /**
         * Complex value.
         */
        private Boxed boxed;

        /**
         * Enumeration value.
         */
        private Expect.Matcher match;

        /**
         * Any enumeration type.
         */
        private Enum<?> anyenum;

        /**
         * No value enumeration.
         */
        private NoEnum noenum;

        /**
         * Interface type.
         */
        private Value iface;

        /**
         * Array value.
         */
        private Value[] array;

        /**
         * Arbitrary value.
         */
        private Object object;

        /**
         * String value.
         */
        private String string;

        /**
         * Class value.
         */
        private Class<?> type;

        /**
         * Date value.
         */
        private Date date;

        /**
         * No value enumeration.
         */
        private static enum NoEnum {
            // nothing to do.
        }

        /**
         * Create complex type with given values.
         *
         * @param  boxed    boxed value.
         * @param  match    match enumeration value.
         * @param  anyenum  any enumeration type value.
         * @param  noenum   no enumeration value.
         * @param  iface    interface value.
         * @param  array    array value.
         * @param  object   object value.
         * @param  string   string value.
         * @param  type     type value.
         * @param  date     data value.
         */
        // CHECKSTYLE:OFF
        public Complex(Boxed boxed, Expect.Matcher match, Enum<?> anyenum, NoEnum noenum, Value iface, Value[] array,
                Object object, String string, Class<?> type, Date date) { // CHECKSTYLE:ON
            super(false, 'X');
            this.boxed = boxed;
            this.match = match;
            this.anyenum = anyenum;
            this.noenum = noenum;
            this.iface = iface;
            this.array = array;
            this.object = object;
            this.string = string;
            this.type = type;
            this.date = date;
        }

        /**
         * Getter for complex value.
         *
         * @return  complex value.
         */
        public Boxed getBoxed() {
            return this.boxed;
        }

        /**
         * Setter for complex value.
         *
         * @param  boxed  complex value.
         */
        public void setBoxed(Boxed boxed) {
            this.boxed = boxed;
        }

        /**
         * Getter for array value.
         *
         * @return  array value.
         */
        public Value[] getArray() {
            return this.array;
        }

        /**
         * Setter for array value.
         *
         * @param  array  array value.
         */
        public void setArray(Value[] array) {
            this.array = array;
        }

        /**
         * Getter for enumeration value.
         *
         * @return  enumeration value.
         */
        public Expect.Matcher getMatcher() {
            return this.match;
        }

        /**
         * Setter for enumeration value.
         *
         * @param  matcher  enumeration value.
         */
        public void setMatcher(Expect.Matcher matcher) {
            this.match = matcher;
        }

        /**
         * Getter for any enumeration type value.
         *
         * @return  any enumeration type value.
         */
        public Enum<?> getAnyEnum() {
            return this.anyenum;
        }

        /**
         * Setter for any enumeration type value.
         *
         * @param  anyenum  any enumeration type value.
         */
        public void setAnyEnum(Enum<?> anyenum) {
            this.anyenum = anyenum;
        }

        /**
         * Getter for enumeration value.
         *
         * @return  enumeration value.
         */
        public NoEnum getNoEnum() {
            return this.noenum;
        }

        /**
         * Setter for no-enumeration value.
         *
         * @param  noenum  no-enumeration value.
         */
        public void setNoEnum(NoEnum noenum) {
            this.noenum = noenum;
        }

        /**
         * Getter for interface type.
         *
         * @return  interface type.
         */
        public Value getIface() {
            return this.iface;
        }

        /**
         * Setter for interface type.
         *
         * @param  iface  interface type.
         */
        public void setIface(Value iface) {
            this.iface = iface;
        }

        /**
         * Getter for arbitrary value.
         *
         * @return  arbitrary value.
         */
        public Object getObject() {
            return this.object;
        }

        /**
         * Setter for arbitrary value.
         *
         * @param  object  arbitrary value.
         */
        public void setObject(Object object) {
            this.object = object;
        }

        /**
         * Getter for string value.
         *
         * @return  string value.
         */
        public String getString() {
            return this.string;
        }

        /**
         * Setter for string value.
         *
         * @param  string  string value.
         */
        public void setString(String string) {
            this.string = string;
        }

        /**
         * Getter for class value.
         *
         * @return  class value.
         */
        public Class<?> getType() {
            return this.type;
        }

        /**
         * Setter for class value.
         *
         * @param  type  class value.
         */
        public void setType(Class<?> type) {
            this.type = type;
        }

        /**
         * Getter for data value.
         *
         * @return  data value.
         */
        public Date getDate() {
            return this.date;
        }

        /**
         * Setter for data value.
         *
         * @param  date  data value.
         */
        public void setDate(Date date) {
            this.date = date;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "Complex[boxed=" + this.boxed + ", match=" + this.match + ", noenum=" + this.noenum + ", iface="
                + this.iface + ", array=" + Arrays.toString(this.array) + ", object=" + this.object + ", string="
                + this.string + ", type=" + this.type + ", date=" + this.date + ", super=" + super.toString() + "]";
        }
    }

    /**
     * Check property definition atom helper behavior.
     */
    @FixMethodOrder(MethodSorters.JVM)
    public static final class AtomBehavior extends SimpleTest {

        /**
         * Default expectation.
         */
        private static final Expect.Rule EXPECT = new Expect.Rule();

        /**
         * Test failed creation without expected exception.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "invocation target failure",
            matcher = Expect.Matcher.STARTS_WITH,
            cause =
                @Expect.Cause(
                    type = IllegalArgumentException.class,
                    message = "expected exception must not be null"
                )
        )
        public void createWithNullExpect() {
            AccessHelper.Objects.create(CLASS_ATOM, TYPES_ATOM, //
                new Object[] { PROPERTY_VALUE, ACCESS_VALUE, null, null });
        }

        /**
         * Test failed creation without property access helper.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "invocation target failure",
            matcher = Expect.Matcher.STARTS_WITH,
            cause =
                @Expect.Cause(
                    type = IllegalArgumentException.class,
                    message = "property access helper must not be null"
                )
        )
        public void createWithNullAccess() {
            AccessHelper.Objects.create(CLASS_ATOM, TYPES_ATOM, //
                new Object[] { PROPERTY_VALUE, null, EXPECT, null });
        }

        /**
         * Test failed creation without property definition.
         */
        @Test(expected = AccessHelper.Failure.class)
        @Expect(
            message = "invocation target failure",
            matcher = Expect.Matcher.STARTS_WITH,
            cause =
                @Expect.Cause(
                    type = IllegalArgumentException.class,
                    message = "property definition must not be null"
                )
        )
        public void createWithNullProperty() {
            AccessHelper.Objects.create(CLASS_ATOM, TYPES_ATOM, //
                new Object[] { null, ACCESS_VALUE, EXPECT, null });
        }
    }

    /**
     * Property definition atom helper object theory test.
     */
    @RunWith(Theories.class)
    public static final class AtomObjectTheory extends ObjectTheory {

        /**
         * Default expectation.
         */
        private static final Expect.Rule EXPECT = new Expect.Rule();

        /**
         * Property accesses helper for object theory testing.
         */
        @DataPoints
        public static final Object[] ATOMS = new Object[] {
                AccessHelper.Objects.create(CLASS_ATOM, TYPES_ATOM, //
                    new Object[] { PROPERTY_VALUE, ACCESS_VALUE, EXPECT, null }),
                AccessHelper.Objects.create(CLASS_ATOM, TYPES_ATOM, //
                    new Object[] { PROPERTY_VALUE, ACCESS_VALUE, EXPECT, null }),
                AccessHelper.Objects.create(CLASS_ATOM, TYPES_ATOM,
                    new Object[] { PROPERTY_INTEGER, ACCESS_VALUE, EXPECT, null }),
                AccessHelper.Objects.create(CLASS_ATOM, TYPES_ATOM,
                    new Object[] { PROPERTY_INTEGER, ACCESS_INTEGER, EXPECT, null }),
                AccessHelper.Objects.create(CLASS_ATOM, TYPES_ATOM,
                    new Object[] { PROPERTY_INTEGER, ACCESS_INTEGER, EXPECT, 0 }),
                AccessHelper.Objects.create(CLASS_ATOM, TYPES_ATOM,
                    new Object[] { PROPERTY_INTEGER, ACCESS_INTEGER, EXPECT, 0 }),
                AccessHelper.Objects.create(CLASS_ATOM, TYPES_ATOM,
                    new Object[] { PROPERTY_INTEGER, ACCESS_INTEGER, new Expect.Rule(), null })
            };
    }

    /**
     * Property definition check behavior test.
     */
    @FixMethodOrder(MethodSorters.JVM)
    public static final class CheckBehavior extends SimpleTest {

        /**
         * Check property configuration check with null configuration value.
         *
         * @throws  Throwable  if test fails.
         */
        @Test
        public void nullConfigValue() throws Throwable {
            new Property.Checker().check(new Boxed(null, null), null);
        }
        // FIXME: add tests with other target objects! to for checking accessor failures ...
    }

    /**
     * Property definition check bean theory test.
     */
    @RunWith(Theories.class)
    public static final class CheckBeanTheory extends BeanTheory {

        /**
         * Primitive instance for bean theory testing.
         */
        @DataPoints
        public static final Primitive[] PRIMITIVE = new Primitive[] {
                new Primitive(Boolean.TRUE, 'y'), new Primitive(Boolean.FALSE, 'Y')
            };

        /**
         * Boxed instance for bean theory testing.
         */
        @DataPoints
        public static final Boxed[] BOXED = new Boxed[] {
                new Boxed(null, null), new Boxed(Boolean.TRUE, 'y'), new Boxed(Boolean.FALSE, 'Y')
            };

        /**
         * Complex instance for bean theory testing.
         */
        @DataPoints
        public static final Complex[] COMPLEX = new Complex[] {
                new Complex(null, Expect.Matcher.EQUALS, null, null, new Primitive(true, 'x'), //
                    null, null, null, null, null),
                new Complex(new Boxed(null, null), null, Expect.Matcher.CONTAINS, null, null, //
                    null, null, "test", Object.class, new Date()),
                new Complex(null, null, SingleEnum.SINGLE, null, new Primitive(false, '\0'), //
                    null, null, "", Class.class, new Date(0))
            };

        /**
         * Activate expectation rule.
         */
        @Rule
        public final ExpectRule expect = new ExpectRule(false);

        /**
         * Alternative value for primitive character.
         */
        @Property
        @Expect(
            type = IllegalArgumentException.class,
            message = "must not be 'x' or 'X' [X]"
        )
        public final char chars = 'X';

        /**
         * Single value test enmum.
         */
        private static enum SingleEnum {

            /**
             * Single Value.
             */
            SINGLE;
        }
    }
}
