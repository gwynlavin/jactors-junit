package org.jactors.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.hamcrest.CoreMatchers;
import org.jactors.junit.helper.AccessHelper;
import org.jactors.junit.helper.BeanHelper;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Property definition.
 */
@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ ElementType.FIELD })
public @interface Property {

    /**
     * Constant for auto naming.
     */
    public static final String AUTO = "*";

    /**
     * Property class type definition.
     */
    public abstract Class<?> type() default Object.class;

    /**
     * Property field name.
     */
    public abstract String field() default AUTO;

    /**
     * Property getter method name.
     */
    public abstract String getter() default AUTO;

    /**
     * Property setter method name.
     */
    public abstract String setter() default AUTO;

    /**
     * Property checker for validating bean properties.
     */
    public static final class Checker {

        /**
         * Check/validated properties for given property target object using given property
         * configuration object.
         *
         * @param   target  property target object.
         * @param   config  property configuration object (may be null).
         *
         * @throws  Throwable  any exception that is not expected.
         */
        public void check(Object target, Object config) throws Throwable {
            List<Atom<?>> atoms = new ArrayList<Atom<?>>();
            if (config != null) {
                atoms.addAll(this.atoms(target, config));
            }
            Set<BeanHelper.Property<?>> properties = this.properties(target.getClass());
            properties.removeAll(this.properties(atoms));
            atoms.addAll(this.atoms(target, properties));
            for (Atom<?> atom : atoms) {
                try {
                    atom.check(target);
                } catch (RuntimeException except) {
                    throw new IllegalStateException("check failed [target=" //
                        + target + ", atom=" + atom + "]", except);
                }
            }
        }

        /**
         * Create list of test atoms for given target object and given configuration object.
         *
         * @param   target  property target object.
         * @param   config  property configuration object.
         *
         * @return  list of test atoms.
         *
         * @throws  Throwable  any exception that is not expected.
         */
        private List<Atom<?>> atoms(Object target, Object config) throws Throwable {
            Field[] fields = config.getClass().getFields();
            List<Atom<?>> atoms = new ArrayList<Atom<?>>(fields.length);
            for (Field field : fields) {
                BeanHelper.Property<?> property = Helper.create(field);
                if (property == null) {
                    continue;
                }
                BeanHelper.Accessor<?> accessor = BeanHelper.create(target.getClass(), property);
                if (Helper.valid(accessor)) {
                    Expect.Rule expect = new Expect.Rule().expect(field.getAnnotation(Expect.class));
                    Object value = AccessHelper.Fields.<Object>get(config, field);
                    atoms.add(Helper.create(property, accessor, expect, value));
                }
            }
            return atoms;
        }

        /**
         * Create list of test atoms for given target object and set of default property
         * definitions.
         *
         * @param   target      property target object.
         * @param   properties  default property definitions.
         *
         * @return  list of test atoms.
         *
         * @throws  Throwable  any exception that is not expected.
         */
        private List<? extends Atom<?>> atoms(Object target, Set<BeanHelper.Property<?>> properties) throws Throwable {
            List<Atom<?>> atoms = new ArrayList<Atom<?>>(properties.size());
            for (BeanHelper.Property<?> property : properties) {
                BeanHelper.Accessor<?> accessor = BeanHelper.create(target.getClass(), property);
                if (Helper.valid(accessor)) {
                    Expect.Rule expect = new Expect.Rule();
                    Object value = Helper.value(property, accessor.get(target));
                    atoms.add(Helper.create(property, accessor, expect, value));
                }
            }
            return atoms;
        }

        /**
         * Create list of property definitions from given list of test atoms.
         *
         * @param   atoms  list of test atoms.
         *
         * @return  set of property definitions.
         */
        private Set<BeanHelper.Property<?>> properties(List<Atom<?>> atoms) {
            Set<BeanHelper.Property<?>> props = new HashSet<BeanHelper.Property<?>>();
            for (Atom<?> atom : atoms) {
                props.add(atom.property);
            }
            return props;
        }

        /**
         * Create list of property definitions from given target class type.
         *
         * @param   type  target class type.
         *
         * @return  list of property definitions.
         */
        private Set<BeanHelper.Property<?>> properties(Class<?> type) {
            if (type == Object.class) {
                return new HashSet<BeanHelper.Property<?>>();
            }
            Set<BeanHelper.Property<?>> props = this.properties(type.getSuperclass());
            for (Field field : type.getDeclaredFields()) {
                BeanHelper.Property<?> property = Helper.create(type, field);
                if ((property != null) && !props.contains(property)) {
                    props.add(property);
                }
            }
            for (Method method : type.getDeclaredMethods()) {
                BeanHelper.Property<?> property = Helper.create(type, method);
                if ((property != null) && !props.contains(property)) {
                    props.add(property);
                }
            }
            return props;
        }

        /**
         * Property helper.
         */
        private abstract static class Helper { // NOPMD: helper!

            private static enum AnyEnum {

                VALUE;
            }

            /**
             * Create property definition using given property field respecting its {@link Property}
             * annotation if available.
             *
             * @param   <Type>  property type.
             * @param   field   property field.
             *
             * @return  property definition.
             */
            protected static <Type> BeanHelper.Property<Type> create(Field field) {
                Property value = field.getAnnotation(Property.class);
                if (value != null) {
                    @SuppressWarnings("unchecked")
                    Class<Type> type = (Class<Type>) field.getType();
                    if (!Property.AUTO.equals(value.field())) {
                        return BeanHelper.create(type, value.field(), value.getter(), value.setter());
                    }
                    return BeanHelper.create(type, field.getName(), value.getter(), value.setter());
                }
                return null;
            }

            /**
             * Create property definition for given declaring class type and given property field.
             *
             * @param   <Type>  property type.
             * @param   type    declaring class type.
             * @param   field   property field.
             *
             * @return  property definition.
             */
            protected static <Type> BeanHelper.Property<Type> create(Class<?> type, Field field) {
                int modifiers = field.getModifiers();
                if (!(Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers))) {
                    @SuppressWarnings("unchecked")
                    BeanHelper.Property<Type> property = //
                        BeanHelper.create((Class<Type>) field.getType(), field.getName());
                    BeanHelper.Accessor<Type> accessor = BeanHelper.create(type, property);
                    return (Helper.valid(accessor)) ? property : null;
                }
                return null;
            }

            /**
             * Create property definition for given declaring class type and given getter/setter
             * method.
             *
             * @param   <Type>  property type.
             * @param   type    declaring class type.
             * @param   method  getter/setter method.
             *
             * @return  property definition.
             */
            protected static <Type> BeanHelper.Property<Type> create(Class<?> type, Method method) {
                BeanHelper.Property<Type> property = //
                    BeanHelper.create(method, AccessHelper.Failure.Mode.RETURN_NULL);
                if (property != null) {
                    BeanHelper.Accessor<Type> accessor = BeanHelper.create(type, property);
                    return (Helper.valid(accessor)) ? property : null;
                }
                return null;
            }

            /**
             * Create test atom using given property description, property access helper, exception
             * expectation builder, and changed property value.
             *
             * @param   property  property definition.
             * @param   accessor  property access helper.
             * @param   expect    exception expectation builder.
             * @param   value     changed property value.
             *
             * @return  test atom.
             */
            @SuppressWarnings({ "rawtypes", "unchecked" })
            protected static Atom<?> create(BeanHelper.Property<?> property, BeanHelper.Accessor<?> accessor,
                    Expect.Rule expect, Object value) {
                return new Atom(property, accessor, expect, value);
            }

            /**
             * Validate whether given property accessor defines full property with field or with
             * getter and setter.
             *
             * @param   accessor  property accessor (may be {@code null}).
             *
             * @return  whether given property accessor defines full property with field or with
             *          getter and setter.
             */
            protected static boolean valid(BeanHelper.Accessor<?> accessor) {
                if (accessor != null) {
                    BeanHelper.Property<?> property = accessor.property();
                    if (property.field() != null) {
                        return true;
                    } else if ((property.getter() != null) && (property.setter() != null)) {
                        return true;
                    }
                }
                return false;
            }

            /**
             * Create changed property value for given property definition and given actual property
             * value.
             *
             * @param   property  property definition.
             * @param   value     actual property value.
             *
             * @return  changed property value.
             */
            @SuppressWarnings("unchecked")
            protected static Object value(BeanHelper.Property<?> property, Object value) {
                Class<?> type = property.type();
                if (type.isArray()) {
                    return Array.newInstance(type.getComponentType(), 1);
                } else if (type.isInterface()) {
                    return EasyMock.createMock(type);
                } else if (type.isEnum()) {
                    for (Object next : type.getEnumConstants()) {
                        if (next != value) {
                            return next;
                        }
                    }
                    try {
                        return AccessHelper.Enums.create(type.asSubclass(Enum.class), value + "*", 0,
                                new Class<?>[] {});
                    } catch (AccessHelper.Failure failure) {
                        Assert.fail("invalid enum value [type=" + type.getName() + ", property=" + property + "]");
                    }
                } else if (type == Enum.class) {
                    return AnyEnum.VALUE;
                } else if (type == String.class) {
                    return ((value != null) && !((String) value).isEmpty()) ? (((String) value) + "-test") : "test";
                } else if ((type == long.class) || (type == Long.class)) {
                    return Long.valueOf((value != null) ? (((Long) value) + 1) : 1);
                } else if ((type == int.class) || (type == Integer.class)) {
                    return Integer.valueOf((value != null) ? (((Integer) value) + 1) : 1);
                } else if ((type == short.class) || (type == Short.class)) {
                    return Short.valueOf((short) ((value != null) ? (((Short) value) + 1) : 1));
                } else if ((type == byte.class) || (type == Byte.class)) {
                    return Byte.valueOf((byte) ((value != null) ? (((Byte) value) + 1) : 1));
                } else if ((type == double.class) || (type == Double.class)) {
                    return Double.valueOf((value != null) ? (((Double) value) + 1.0d) : 1.0d);
                } else if ((type == float.class) || (type == Float.class)) {
                    return Float.valueOf((value != null) ? (((Float) value) + 1.0f) : 1.0f);
                } else if ((type == boolean.class) || (type == Boolean.class)) {
                    return ((value != null) && (Boolean) value) ? Boolean.FALSE : Boolean.TRUE;
                } else if ((type == char.class) || (type == Character.class)) {
                    return Character.valueOf(((value != null) && ((Character) value != 'Y')) ? 'Y' : 'y');
                } else if (type == Class.class) {
                    return (value != Object.class) ? Object.class : String.class;
                } else if (type == Date.class) {
                    return new Date(System.currentTimeMillis());
                }
                try {
                    return AccessHelper.Objects.create(type, new Class[] {}, new Object[] {});
                } catch (RuntimeException except) {
                    Assert.fail("unable to create value [type=" + type.getName() + ", property=" + property + "]");
                }
                return null;
            }
        }

        /**
         * Test atom for checking property.
         */
        private static final class Atom<Type> {

            /**
             * Logger instance for auditing output.
             */
            private static final Logger LOG = LoggerFactory.getLogger(Atom.class);

            /**
             * Property definition.
             */
            private final BeanHelper.Property<Type> property;

            /**
             * Property access helper.
             */
            private final BeanHelper.Accessor<Type> accessor;

            /**
             * Exception expectation builder.
             */
            private final Expect.Rule expect;

            /**
             * Changed property value for checking.
             */
            private final Type value;

            /**
             * Create atom with given property definition, property access helper, exception
             * expectation builder, and property value.
             *
             * @param  property  property definition.
             * @param  accessor  property access helper.
             * @param  expect    exception expectation builder.
             * @param  value     changed property value.
             */
            private Atom(BeanHelper.Property<Type> property, BeanHelper.Accessor<Type> accessor, Expect.Rule expect,
                    Type value) {
                LOG.debug("create atom [property={}, value={}]", property, value);
                if (property == null) {
                    throw new IllegalArgumentException("property definition must not be null");
                } else if (accessor == null) {
                    throw new IllegalArgumentException("property access helper must not be null");
                } else if (expect == null) {
                    throw new IllegalArgumentException("expected exception must not be null");
                }
                this.property = property;
                this.accessor = accessor;
                this.expect = expect;
                this.value = value;
            }

            /**
             * Check property for given target object.
             *
             * @param   target  target object.
             *
             * @throws  Throwable  any exception that is not expected.
             */
            public void check(Object target) throws Throwable {
                LOG.info("check atom [property={}, target={}, value={}]",
                    new Object[] { this.property, target, this.value });
                String context = "property=" + this.property + ", target=" + target + ", value=" + this.value;
                Type before = this.accessor.get(target);
                try {
                    Assert.assertThat("returns original value [" + context + "]", before,
                        CoreMatchers.is(CoreMatchers.equalTo(this.accessor.set(target, this.value))));
                    this.expect.success();
                } catch (Exception except) {
                    this.expect.failure(except);
                    return;
                }
                Assert.assertThat("value is changed [" + context + "]", this.accessor.get(target),
                    CoreMatchers.is(CoreMatchers.equalTo(this.value)));
                Assert.assertThat("value changed back [" + context + "]", this.accessor.set(target, before),
                    CoreMatchers.is(CoreMatchers.equalTo(this.value)));
            }

            /**
             * {@inheritDoc}
             */
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = (prime * result) + this.accessor.hashCode();
                result = (prime * result) + this.expect.hashCode();
                result = (prime * result) + this.property.hashCode();
                result = (prime * result) + ((this.value == null) ? 0 : this.value.hashCode());
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
                Atom<?> other = (Atom<?>) obj;
                if (!this.accessor.equals(other.accessor)) {
                    return false;
                } else if (!this.expect.equals(other.expect)) {
                    return false;
                } else if (!this.property.equals(other.property)) {
                    return false;
                } else if (this.value == null) {
                    if (other.value != null) {
                        return false;
                    }
                } else if (!this.value.equals(other.value)) {
                    return false;
                }
                return true;
            }

            /**
             * {@inheritDoc}
             */
            public String toString() {
                return "Atom[property=" + this.property + ", access=" + this.accessor + ", expect=" + this.expect
                    + ", value=" + this.value + "]";
            }
        }
    }
}
