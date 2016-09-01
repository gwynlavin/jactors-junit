package org.jactors.junit.helper; // NOPMD: test suite

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.jactors.junit.Expect;
import org.jactors.junit.Property;
import org.jactors.junit.helper.AccessHelper;
import org.jactors.junit.helper.BeanHelper;
import org.jactors.junit.rule.ExpectRule;
import org.jactors.junit.test.ParameterTest;
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
        BeanHelperTest.PropertyBehavior.class,
        BeanHelperTest.AccessorBehavior.class,
    }
)
public class BeanHelperTest extends AccessHelperTest { // NOPMD: test suite

    /**
     * Name for integer.
     */
    private static final String NAME_INTEGER = "integer";

    /**
     * Field for integer.
     */
    private static final Field FIELD_INTEGER = //
        AccessHelper.Fields.resolve(Base.class, NAME_INTEGER);

    /**
     * Getter for integer.
     */
    private static final Method GETTER_INTEGER = //
        AccessHelper.Methods.resolve(Base.class, "getInteger", new Class[] {});

    /**
     * Setter for integer.
     */
    private static final Method SETTER_INTEGER = //
        AccessHelper.Methods.resolve(Base.class, "setInteger", new Class[] { int.class });

    /**
     * Accessor helper to create .
     */
    private static final class AccessorHelper {

        private static final Class<?>[] ACCESSOR_TYPES = new Class<?>[] {
                BeanHelper.Accessor.class, Class.class, Field.class, Method.class, Method.class, String.class
            };

        @SuppressWarnings("rawtypes")
        private static final Constructor<BeanHelper.Accessor> ACCESSOR_FACTORY = //
            AccessHelper.Objects.resolve(BeanHelper.Accessor.class, ACCESSOR_TYPES);

        public static <Type> BeanHelper.Accessor<Type> create(Class<Type> type, Field field, Method getter,
                Method setter) {
            return AccessorHelper.create(null, type, field, getter, setter, null);
        }

        @SuppressWarnings("unchecked")
        public static <Type> BeanHelper.Accessor<Type> create(BeanHelper.Accessor<?> parent, Class<Type> type,
                Field field, Method getter, Method setter, String key) {
            try {
                return AccessHelper.Objects.create(ACCESSOR_FACTORY,
                        new Object[] { parent, type, field, getter, setter, key });
            } catch (AccessHelper.Failure failure) {
                if (failure.getCause() == null) {
                    throw failure;
                } else if ((failure.getType() == AccessHelper.Failure.Type.TARGET)
                        && (failure.getCause() instanceof RuntimeException)) {
                    throw (RuntimeException) failure.getCause();
                }
                throw failure;
            }
        }
    }

    /**
     * Check property definitions behavior.
     */
    @RunWith(Suite.class)
    @Suite.SuiteClasses(
        {
            PropertyBehavior.BeanTheory.class,
            PropertyBehavior.ObjectTheory.class,
            PropertyBehavior.DefaultBehavior.class,
        }
    )
    public static final class PropertyBehavior extends AccessHelperTest {

        /**
         * Check bean property definition bean theory.
         */
        @RunWith(Theories.class)
        public static final class BeanTheory extends org.jactors.junit.theory.BeanTheory {

            /**
             * Default bean property definition for bean theory testing.
             */
            @DataPoint
            public static final BeanHelper.Property<?> PROPERTY = BeanHelper.create(Object.class, "name");

            /**
             * Alternate class type.
             */
            @Property(field = "type")
            public static final Class<?> type = BeanTheory.class;

            /**
             * Alternate field name (use object, if type is generic - type erasure).
             */
            @Property
            public static final Object field = "field";
        }

        /**
         * Check bean property definition object theory.
         */
        @RunWith(Theories.class)
        public static final class ObjectTheory extends org.jactors.junit.theory.ObjectTheory {

            /**
             * Bean property definitions for object theory testing.
             */
            @DataPoints
            public static final BeanHelper.Property<?>[] PROPERTIES = new BeanHelper.Property<?>[] {
                    BeanHelper.create(int.class, NAME_INTEGER),
                    BeanHelper.create(long.class, NAME_VALUE),
                    BeanHelper.create(long.class, null, NAME_VALUE, NAME_VALUE),
                    BeanHelper.create(long.class, null, NAME_VALUE, NAME_VALUE),
                    BeanHelper.create(long.class, NAME_VALUE, null, null),
                    BeanHelper.create(long.class, NAME_VALUE, null, null),
                    BeanHelper.create(long.class, NAME_VALUE, NAME_VALUE, null),
                    BeanHelper.create(long.class, NAME_VALUE, NAME_VALUE, null),
                    BeanHelper.create(long.class, NAME_VALUE, NAME_VALUE, NAME_VALUE),
                    BeanHelper.create(int.class, NAME_VALUE, NAME_VALUE, NAME_VALUE),
                };
        }

        /**
         * Check bean property definition creation and behavior.
         */
        @FixMethodOrder(MethodSorters.JVM)
        @RunWith(BlockJUnit4ClassRunner.class)
        public static final class DefaultBehavior implements AccessHelperTest.Unknown {

            /**
             * Invalid getter/setter test method.
             */
            private static final Method INVALID_GETTER_SETTER = //
                AccessHelper.Methods.resolve(BeanHelper.Accessor.class, "write",
                    new Class<?>[] { Object.class, String.class, Object.class });

            /**
             * Activate expectation rule.
             */
            @Rule
            public ExpectRule expect = new ExpectRule();

            /**
             * Test failed creation with null type.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "type must not be null")
            public void createByNameNullType() throws Throwable {
                BeanHelper.create(null, NAME_VALUE);
            }

            /**
             * Test failed creation with null name.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "name must not be null or empty [null]")
            public void createByNameNullName() throws Throwable {
                BeanHelper.create(Object.class, (String) null);
            }

            /**
             * Test failed creation with empty name.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "name must not be null or empty []")
            public void createByNameEmptyName() throws Throwable {
                BeanHelper.create(Object.class, "");
            }

            /**
             * Test failed creation with null type.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "type must not be null")
            public void createByNameWithNullType() throws Throwable {
                BeanHelper.create(null, NAME_VALUE, NAME_VALUE, NAME_VALUE);
            }

            /**
             * Test failed creation with null field, getter, and setter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "incomplete property [field=null, getter=null, setter=null]")
            public void createByNameFailedNull() throws Throwable {
                BeanHelper.create(Object.class, (String) null, (String) null, (String) null);
            }

            /**
             * Test failed creation with null field and setter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "incomplete property [field=null, getter=" + NAME_VALUE + ", setter=null]")
            public void createByNameFailedNullWithGetter() throws Throwable {
                BeanHelper.create(Object.class, (String) null, NAME_VALUE, (String) null);
            }

            /**
             * Test failed creation with null field and getter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "incomplete property [field=null, getter=null, setter=" + NAME_VALUE + "]")
            public void createByNameFailedNullWithSetter() throws Throwable {
                BeanHelper.create(Object.class, (String) null, (String) null, NAME_VALUE);
            }

            /**
             * Test failed creation with empty field, getter, and setter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "incomplete property [field=, getter=, setter=]")
            public void createByNameFailedEmpty() throws Throwable {
                BeanHelper.create(Object.class, "", "", "");
            }

            /**
             * Test failed creation with empty field, getter, and setter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "incomplete property [field=, getter=" + NAME_VALUE + ", setter=]")
            public void createByNameFailedEmptyWithGetter() throws Throwable {
                BeanHelper.create(Object.class, "", NAME_VALUE, "");
            }

            /**
             * Test failed creation with empty field, getter, and setter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "incomplete property [field=, getter=, setter=" + NAME_VALUE + "]")
            public void createByNameFailedEmptyWithSetter() throws Throwable {
                BeanHelper.create(Object.class, "", "", NAME_VALUE);
            }

            /**
             * Test failed creation with empty field, getter, and setter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "incomplete property [field=, getter=, setter=]")
            public void createByNameWithEmptyName() throws Throwable {
                BeanHelper.create(Object.class, "", "", "");
            }

            /**
             * Test property definition creation by field.
             */
            @Test
            public void createByField() {
                BeanHelper.Property<Long> actual = BeanHelper.create(FIELD_VALUE);
                Assert.assertThat(actual, CoreMatchers.is(BeanHelper.create(long.class, "value", "*", "*")));
            }

            /**
             * Test property definition creation by getter method.
             */
            @Test
            public void createByMethodGetter() {
                BeanHelper.Property<Long> actual = BeanHelper.create(GETTER_VALUE, AccessHelper.Failure.Mode.DEFAULT);
                Assert.assertThat(actual, CoreMatchers.is(BeanHelper.create(long.class, "value", "*", "*")));
            }

            /**
             * Test property definition creation by setter method.
             */
            @Test
            public void createByMethodSetter() {
                BeanHelper.Property<Long> actual = BeanHelper.create(SETTER_VALUE, AccessHelper.Failure.Mode.DEFAULT);
                Assert.assertThat(actual, CoreMatchers.is(BeanHelper.create(long.class, "value", "*", "*")));
            }

            /**
             * Test property definition creation by invalid method using default failure handling
             * mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "method is no getter/setter [write]")
            public void createByMethodFailedDefault() {
                BeanHelper.create(INVALID_GETTER_SETTER, AccessHelper.Failure.Mode.DEFAULT);
            }

            /**
             * Test property definition creation by invalid method using throw exception failure
             * handling mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "method is no getter/setter [write]")
            public void createByMethodFailedThrowException() {
                BeanHelper.create(INVALID_GETTER_SETTER, AccessHelper.Failure.Mode.THROW_EXCEPTION);
            }

            /**
             * Test property definition creation by invalid method using return null failure
             * handling mode.
             */
            @Test
            public void createByMethodFailedReturnNull() {
                BeanHelper.Property<Long> actual = BeanHelper.create(INVALID_GETTER_SETTER,
                        AccessHelper.Failure.Mode.RETURN_NULL);
                Assert.assertThat(actual, CoreMatchers.nullValue());
            }

            /**
             * Test property definition creation by invalid method using unknown failure handling
             * mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "mode not supported [UNKNOWN]")
            public void createByMethodFailedUnknown() {
                BeanHelper.create(INVALID_GETTER_SETTER, MODE_FAILURE_UNKNOWN);
            }

            /**
             * Test property definition creation by invalid method using unknown failure handling
             * mode.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "method is no getter/setter [write]")
            public void nameByMethodFailed() {
                AccessHelper.Beans.name(INVALID_GETTER_SETTER);
            }
        }
    }

    /**
     * Check property definitions behavior.
     */
    @RunWith(Suite.class)
    @Suite.SuiteClasses(
        {
            AccessorBehavior.BeanTheory.class,
            AccessorBehavior.ObjectTheory.class,
            AccessorBehavior.DefaultBehavior.class,
            AccessorBehavior.ComplexBehavior.class
        }
    )
    public static final class AccessorBehavior extends AccessHelperTest {

        /**
         * Check bean property accessor bean theory.
         */
        @RunWith(Theories.class)
        public static final class BeanTheory extends org.jactors.junit.theory.BeanTheory {

            /**
             * Bean property accessor for bean theory testing.
             */
            @DataPoint
            public static final BeanHelper.Accessor<?> ACCESS = BeanHelper.create(Base.class,
                    BeanHelper.create(long.class, FIELD_VALUE.getName(), GETTER_VALUE.getName(),
                        SETTER_VALUE.getName()));

            /**
             * Alternative property accessor.
             */
            @Property
            public static final BeanHelper.Accessor<?> parent = ACCESS;

            /**
             * Alternative property definition.
             */
            @Property
            public static final BeanHelper.Property<?> property = BeanHelper.create(int.class, "integer");

            /**
             * Alternative property field (use object, if type is generic - type erasure).
             */
            @Property
            public static final Object field = FIELD_INTEGER;

            /**
             * Alternative property getter (use object, if type is generic - type erasure).
             */
            @Property
            public static final Object getter = GETTER_INTEGER;

            /**
             * Alternative property setter (use object, if type is generic - type erasure).
             */
            @Property
            public static final Object setter = SETTER_INTEGER;
        }

        /**
         * Check bean property accessor object theory.
         */
        @RunWith(Theories.class)
        public static final class ObjectTheory extends org.jactors.junit.theory.ObjectTheory {

            /**
             * Default bean property accessors for object theory testing.
             */
            @DataPoint
            public static final BeanHelper.Accessor<?> DEFAULT = //
                AccessorHelper.create(long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE);

            /**
             * Bean property accessors for object theory testing.
             */
            @DataPoints
            public static final BeanHelper.Accessor<?>[] ACCESS = new BeanHelper.Accessor[] {
                    AccessorHelper.create(null, long.class, (Field) null, null, null, null),
                    AccessorHelper.create(null, long.class, (Field) null, null, null, null),
                    AccessorHelper.create(DEFAULT, long.class, (Field) null, null, null, null),
                    AccessorHelper.create(DEFAULT, int.class, FIELD_INTEGER, null, null, null),
                    AccessorHelper.create(DEFAULT, long.class, FIELD_VALUE, null, null, null),
                    AccessorHelper.create(DEFAULT, long.class, FIELD_VALUE, GETTER_VALUE, null, null),
                    AccessorHelper.create(DEFAULT, long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE, null),
                    AccessorHelper.create(DEFAULT, long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE, "x"),
                    AccessorHelper.create(DEFAULT, long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE, "x")
                };
        }

        /**
         * Check bean property accessor creation and behavior.
         */
        @FixMethodOrder(MethodSorters.JVM)
        @RunWith(BlockJUnit4ClassRunner.class)
        public static final class DefaultBehavior extends AccessHelperTest {

            /**
             * Activate expectation rule.
             */
            @Rule
            public ExpectRule expect = new ExpectRule();

            /**
             * Test incompatible type in field.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "type must not be null")
            public void invalidType() {
                AccessorHelper.create(null, FIELD_INTEGER, GETTER_INTEGER, SETTER_INTEGER);
            }

            /**
             * Test invalid getter.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(
                message = "^invalid getter \\[.*\\]$",
                matcher = Expect.Matcher.PATTERN
            )
            public void invalidGetter() {
                AccessorHelper.create(long.class, null, SETTER_VALUE, null);
            }

            /**
             * Test invalid setter.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(
                message = "^invalid setter \\[.*\\]$",
                matcher = Expect.Matcher.PATTERN
            )
            public void invalidSetter() {
                AccessorHelper.create(long.class, null, null, GETTER_VALUE);
            }

            /**
             * Test incompatible type in field.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(
                message = "^incompatible types \\[type=.*, field=.*\\]$",
                matcher = Expect.Matcher.PATTERN
            )
            public void incompTypeField() {
                AccessorHelper.create(long.class, FIELD_INTEGER, null, null);
            }

            /**
             * Test incompatible type in getter.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(
                message = "^incompatible types \\[type=.*, getter=.*\\]$",
                matcher = Expect.Matcher.PATTERN
            )
            public void incompTypeGetter() {
                AccessorHelper.create(long.class, null, GETTER_INTEGER, null);
            }

            /**
             * Test incompatible types in getter.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(
                message = "^incompatible types \\[type=.*, setter=.*\\]$",
                matcher = Expect.Matcher.PATTERN
            )
            public void incompTypeSetter() {
                AccessorHelper.create(long.class, null, null, SETTER_INTEGER);
            }

            /**
             * Test successful creation by field.
             */
            @Test
            public void createByField() {
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(long.class, FIELD_VALUE, null, null);
                Assert.assertThat(accessor.property().field(), CoreMatchers.is(FIELD_VALUE.getName()));
                Assert.assertThat(accessor.property().getter(), CoreMatchers.nullValue());
                Assert.assertThat(accessor.property().setter(), CoreMatchers.nullValue());
            }

            /**
             * Test failure without getter.
             */
            @Test
            public void createWithoutGetter() {
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(long.class, null, null, SETTER_VALUE);
                Assert.assertThat(accessor.property().field(), CoreMatchers.nullValue());
                Assert.assertThat(accessor.property().getter(), CoreMatchers.nullValue());
                Assert.assertThat(accessor.property().setter(), CoreMatchers.is(SETTER_VALUE.getName()));
            }

            /**
             * Test failure without setter.
             */
            @Test
            public void createWithoutSetter() {
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(long.class, null, GETTER_VALUE, null);
                Assert.assertThat(accessor.property().field(), CoreMatchers.nullValue());
                Assert.assertThat(accessor.property().getter(), CoreMatchers.is(GETTER_VALUE.getName()));
                Assert.assertThat(accessor.property().setter(), CoreMatchers.nullValue());
            }

            /**
             * Test successful creation by getter and setter.
             */
            @Test
            public void createByGetterSetter() {
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(long.class, null, GETTER_VALUE, SETTER_VALUE);
                Assert.assertThat(accessor.property().field(), CoreMatchers.nullValue());
                Assert.assertThat(accessor.property().getter(), CoreMatchers.is(GETTER_VALUE.getName()));
                Assert.assertThat(accessor.property().setter(), CoreMatchers.is(SETTER_VALUE.getName()));
            }

            /**
             * Test successful creation by field, getter, and setter.
             */
            @Test
            public void createByFieldGetterSetter() {
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE);
                Assert.assertThat(accessor.property().field(), CoreMatchers.is(FIELD_VALUE.getName()));
                Assert.assertThat(accessor.property().getter(), CoreMatchers.is(GETTER_VALUE.getName()));
                Assert.assertThat(accessor.property().setter(), CoreMatchers.is(SETTER_VALUE.getName()));
            }

            /**
             * Test successful creation by field.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test
            public void setGetTargetByField() throws Throwable {
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(long.class, FIELD_VALUE, null, null);
                Base target = new Base(0, 0);
                Assert.assertThat(accessor.set(target, 1L), CoreMatchers.is(0L));
                Assert.assertThat(accessor.get(target), CoreMatchers.is(1L));
            }

            /**
             * Test failure without getter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "access without getter [field=null, getter=null]")
            public void setGetTargetWithoutGetter() throws Throwable {
                AccessorHelper.create(long.class, null, null, SETTER_VALUE).get(new Base(0, 0));
            }

            /**
             * Test failure without setter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "access without setter [field=null, setter=null]")
            public void setGetTargetWithoutSetter() throws Throwable {
                AccessorHelper.create(long.class, null, GETTER_VALUE, null).set(new Base(0, 0), 1L);
            }

            /**
             * Test successful creation by getter and setter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test
            public void setGetTargetByGetterSetter() throws Throwable {
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(long.class, null, GETTER_VALUE, SETTER_VALUE);
                Base target = new Base(0, 0);
                Assert.assertThat(accessor.set(target, 1L), CoreMatchers.is(0L));
                Assert.assertThat(accessor.get(target), CoreMatchers.is(1L));
            }

            /**
             * Test successful creation by field, getter, and setter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test
            public void setGetTargetByFieldGetterSetter() throws Throwable {
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE);
                Base target = new Base(0, 0);
                Assert.assertThat(accessor.set(target, 1L), CoreMatchers.is(0L));
                Assert.assertThat(accessor.get(target), CoreMatchers.is(1L));
            }

            /**
             * Test target failure propagation on getter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = IllegalArgumentException.class)
            @Expect(message = "not allowed [" + Long.MIN_VALUE + "]")
            public void getGetTargetFailure() throws Throwable {
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE);
                Base base = new Base(0, 0);
                base.setValues(Long.MIN_VALUE);
                accessor.get(base);
            }

            /**
             * Test target failure propagation on getter.
             *
             * @throws  Throwable  what ever failure happens.
             */
            @Test(expected = IllegalArgumentException.class)
            @Expect(message = "not allowed [" + Long.MIN_VALUE + "]")
            public void setGetTargetFailure() throws Throwable {
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE);
                accessor.set(new Base(0, 0), Long.MIN_VALUE);
            }

            /**
             * Test writing of child target by field.
             */
            @Test
            public void writeReadChildWithField() {
                BeanHelper.Accessor<Base> parent = AccessorHelper.create(null, Base.class,
                        AccessHelper.Fields.resolve(Child.class, "list"), null, null, "0");
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(parent, long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE, "");
                Child child = new Child(new Base(1, 2));
                Assert.assertThat(accessor.write(child, 2L), CoreMatchers.is(1L));
                Assert.assertThat(accessor.read(child), CoreMatchers.is(2L));
            }

            /**
             * Test writing of child target without getter.
             */
            @Test(expected = AccessHelper.Failure.class)
            @Expect(message = "access without getter [field=null, getter=null]")
            public void writeReadChildWithoutGetter() {
                BeanHelper.Accessor<Base> parent = AccessorHelper.create(null, Base.class, null, null,
                        AccessHelper.Methods.resolve(Child.class, "setList", new Class<?>[] { List.class }), "0");
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(parent, long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE, "");
                accessor.write(new Child(new Base(1, 2)), 2L);
            }

            /**
             * Test writing of child target without setter.
             */
            @Test
            public void writeReadChildWithoutSetter() {
                BeanHelper.Accessor<Base> parent = AccessorHelper.create(null, Base.class, null,
                        AccessHelper.Methods.resolve(Child.class, "getList", new Class<?>[] {}), null, "0");
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(parent, long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE, "");
                Child child = new Child(new Base(1, 2));
                Assert.assertThat(accessor.write(child, 2L), CoreMatchers.is(1L));
                Assert.assertThat(accessor.read(child), CoreMatchers.is(2L));
            }

            /**
             * Test writing of child target by getter and setter.
             */
            @Test
            public void writeReadChildWithGetterSetter() {
                BeanHelper.Accessor<Base> parent = AccessorHelper.create(null, Base.class, null,
                        AccessHelper.Methods.resolve(Child.class, "getList", new Class<?>[] {}),
                        AccessHelper.Methods.resolve(Child.class, "setList", new Class<?>[] { List.class }), "0");
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(parent, long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE, "");
                Child child = new Child(new Base(1, 2));
                Assert.assertThat(accessor.write(child, 2L), CoreMatchers.is(1L));
                Assert.assertThat(accessor.read(child), CoreMatchers.is(2L));
            }

            /**
             * Test writing of child target by field, getter, and setter.
             */
            @Test
            public void writeReadChildWithFieldGetterSetter() {
                BeanHelper.Accessor<Base> parent = AccessorHelper.create(null, Base.class,
                        AccessHelper.Fields.resolve(Child.class, "list"),
                        AccessHelper.Methods.resolve(Child.class, "getList", new Class<?>[] {}),
                        AccessHelper.Methods.resolve(Child.class, "setList", new Class<?>[] { List.class }), "0");
                BeanHelper.Accessor<Long> accessor = //
                    AccessorHelper.create(parent, long.class, FIELD_VALUE, GETTER_VALUE, SETTER_VALUE, "");
                Child child = new Child(new Base(1, 2));
                Assert.assertThat(accessor.write(child, 2L), CoreMatchers.is(1L));
                Assert.assertThat(accessor.read(child), CoreMatchers.is(2L));
            }

            /**
             * Test writing of child target by field, getter, and setter.
             */
            @Test
            public void writeReadParentWithFieldGetterSetter() {
                BeanHelper.Accessor<Base> accessor = AccessorHelper.create(null, Base.class,
                        AccessHelper.Fields.resolve(Child.class, "list"),
                        AccessHelper.Methods.resolve(Child.class, "getList", new Class<?>[] {}),
                        AccessHelper.Methods.resolve(Child.class, "setList", new Class<?>[] { List.class }), "0");
                Child child = new Child(new Base(1, 2));
                Assert.assertThat(accessor.write(child, new Base(3, 4)), CoreMatchers.is(new Base(1, 2)));
                Assert.assertThat(accessor.read(child), CoreMatchers.is(new Base(3, 4)));
            }
        }

        /**
         * Check complex bean property accessor creation and write behavior.
         *
         * @param  <Type>  property type.
         */
        public static final class ComplexBehavior<Type> extends ParameterTest {

            /**
             * Target object.
             */
            private final Object target;

            /**
             * Property definition.
             */
            private final BeanHelper.Property<Type> property;

            /**
             * Target property value.
             */
            private final Type value;

            /**
             * Create complex bean property accessor creation and write behavior test with given
             * target object, property definition, target property value , and expected test
             * result/failure.
             *
             * @param  target    target object.
             * @param  property  property description.
             * @param  value     target property value.
             * @param  result    expected test result/failure.
             */
            public ComplexBehavior(Object target, BeanHelper.Property<Type> property, Type value, Object result) {
                super(result);
                this.property = property;
                this.target = target;
                this.value = value;
            }

            /**
             * Create test data.
             *
             * @return  test data.
             */
            @Parameterized.Parameters(name = "{index}: property={1}, target={0}, value={2}, result={3}")
            public static Iterable<Object[]> data() {
                Builder builder = ParameterTest.builder();
                BeanHelper.Property<Long> property = BeanHelper.create(long.class, "base.value", null, null);
                builder.add(new Child(new Base(1, 2)), property, 10L, 1L);
                property = BeanHelper.create(long.class, null, "base.value", "base.value");
                builder.add(new Child(new Base(1, 2)), property, 11L,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "creation failure [owner=" + Child.class.getName() + ", property=" + property + "]").cause(
                        AccessHelper.Failure.class, "type must not be null"));
                property = BeanHelper.create(long.class, "base.value", "base.value", "base.value");
                builder.add(new Child(new Base(1, 2)), property, 12L, 1L);
                property = BeanHelper.create(long.class, "list[" + Base.class.getName() + "=0].value", null, null);
                builder.add(new Child(new Base(1, 2)), property, 13L, 1L);
                property = BeanHelper.create(long.class, "list[" + Base.class.getName() + "=0].value",
                        "list[" + Base.class.getName() + "=1].value", null);
                builder.add(new Child(new Base(1, 2)), property, 14L,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "creation failure [owner=" + Child.class.getName() + ", property=" + property + "]") //
                    .cause(AccessHelper.Failure.class,
                        "inconsistent sub-property [name=" + Base.class.getName() + "=0, field=" + Base.class.getName()
                        + "=1, property=" + property + "]"));
                builder.add(new Child(new Base(1, 2)), BeanHelper.create(long.class, "x", null, null), null,
                    Expect.Builder.create(AccessHelper.Failure.class,
                        "access without setter [field=null, setter=null]"));
                return builder;
            }

            /**
             * Test complex bean property accessor creation and write behavior.
             */
            @Test
            public void test() { // NOPMD: handled by rule!
                this.rule.actual(
                    BeanHelper.create(this.target.getClass(), this.property) //
                    .write(this.target, this.value));
            }
        }
    }
}
