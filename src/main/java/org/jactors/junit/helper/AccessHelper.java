package org.jactors.junit.helper;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Access helper that allows access to private classes, methods, and fields. This can be used to
 * test inner classes and private methods.
 */
public abstract class AccessHelper {

    /**
     * Create generic object tree builder with given root object, that allows to create objects in a
     * fluent way for testing.
     *
     * @param   <Type>  root object type.
     * @param   target  target root object.
     *
     * @return  object tree builder
     */
    protected static <Type> Builder<Type> builder(Type target) {
        return new Builder<Type>(target);
    }

    /**
     * Copy given target object by either calling clone or by searching a copy constructor that
     * allows creation of objects based on instances of the same class type (including parent class
     * types) or one of the declared interface types.
     *
     * @param   <Type>  target object type.
     * @param   target  target object.
     *
     * @return  cloned target object.
     */
    protected static <Type> Type copy(Type target) {
        if (target == null) {
            throw Failure.create(Failure.Type.ARGUMENT, "target must not be null");
        } else if (target instanceof Cloneable) {
            return Objects.clone(target, Failure.Mode.RETURN_NULL);
        }
        return Objects.copy(target, Failure.Mode.DEFAULT);
    }

    /**
     * Resolve interface.
     */
    public static interface Resolve {

        /**
         * Access helper resolve type.
         */
        public static enum Type {

            /**
             * Resolve exact class types only.
             */
            EXACT,

            /**
             * Resolve super class types.
             */
            SUPER,

            /**
             * Resolve child class types.
             */
            CHILD
        }
    }

    /**
     * Internal constants.
     */
    private static abstract class Base {

        /**
         * Group index of bean path.
         */
        public static final int PATH_BEAN = 1;

        /**
         * Group index of list path.
         */
        public static final int PATH_LIST = 4;

        /**
         * Pattern for matching property path.
         */
        public static final Pattern PATH = //
            Pattern.compile("\\(?((?<=\\()[^=,]*=[^\\)]+(?=\\))|([^=,]+=)?[^.,\\(\\)\\[\\]]+)\\)?"
                + "((?<!\\\\)\\[(.*?)?(?<!\\\\)\\](?!\\[))*+(\\.|,)?");

        /**
         * Pattern for matching property lists.
         */
        public static final Pattern LIST = Pattern.compile("(?<!\\\\)(,|\\]\\[)");

        /**
         * Pattern for class type matching.
         */
        public static final Pattern TYPE = Pattern.compile("(?<!\\\\)=");

        /**
         * Pattern for checking index values.
         */
        public static final Pattern INDEX = Pattern.compile("-?\\d+|\\*|(#|0x)[0-9a-fA-F]*|0[0-7]*");

        /**
         * Empty argument list.
         */
        public static final Object[] EMPTY_ARGS = new Object[] {};

        /**
         * Empty type list.
         */
        public static final Class<?>[] EMPTY_TYPES = new Class<?>[] {};

        /**
         * Any type list.
         */
        public static final Class<?>[] ANY_TYPES = new Class<?>[] { null };

        /**
         * String type list.
         */
        public static final Class<?>[] STRING_TYPES = new Class<?>[] { String.class };

        /**
         * Make given accessible object accessible and return accessible object.
         *
         * @param   <Type>  object type.
         * @param   object  accessible object.
         *
         * @return  accessible object.
         */
        public static <Type extends AccessibleObject> Type accessible(Type object) {
            if (!object.isAccessible()) {
                object.setAccessible(true);
            }
            return object;
        }
    }

    /**
     * Generic object tree builder using a stack pattern to allow creation of abitrary object trees
     * in a fluent way for testing.
     *
     * @param  <Type>  root object type.
     */
    protected static final class Builder<Type> {

        /**
         * Internal stack to push and pop objects.
         */
        private final Deque<Object> stack;

        /**
         * Actual root object value in tree.
         */
        private final Type value;

        /**
         * Create object tree builder with given root object.
         *
         * @param  value  root object value.
         */
        protected Builder(Type value) {
            this.stack = new ArrayDeque<Object>();
            this.stack.add(value);
            this.value = value;
        }

        /**
         * Add (append) given object value to tree object on top of stack using default ('*')
         * container element name.
         *
         * @param   value  object value to append.
         *
         * @return  same builder for further extension.
         */
        public Builder<Type> add(Object value) {
            return this.add("*", value);
        }

        /**
         * Add (substitute) given object value to tree object on top of stack using given target
         * property name.
         *
         * @param   name   target property name.
         * @param   value  object value to append.
         *
         * @return  same builder for further extension.
         */
        public Builder<Type> add(String name, Object value) {
            Object target = this.stack.element();
            Beans.write(target, name, value);
            return this;
        }

        /**
         * Add (append) given object value to tree using default ('*') container element name, and
         * push given object to stop of stack.
         *
         * @param   value  object value to append.
         *
         * @return  same builder for further extension.
         */
        public Builder<Type> push(Object value) {
            return this.push("*", value);
        }

        /**
         * Add (substitute) given object value to tree object on top of stack using given target
         * property name, and push given object to stop of stack.
         *
         * @param   name   target property name.
         * @param   value  object value to append.
         *
         * @return  same builder for further extension.
         */
        public Builder<Type> push(String name, Object value) {
            this.add(name, value);
            this.stack.push(value);
            return this;
        }

        /**
         * Push object value defined by given target property name to top of stack.
         *
         * @param   name  target property name.
         *
         * @return  same builder for further extension.
         */
        public Builder<Type> push(String name) {
            Object target = this.stack.element();
            target = Beans.read(target, name);
            this.stack.push(target);
            return this;
        }

        /**
         * Remove object value from top of stack.
         *
         * @return  same builder for further extension.
         */
        public Builder<Type> pop() {
            this.stack.pop();
            return this;
        }

        /**
         * Return root object of given object tree builder.
         *
         * @return  root object of given object tree builder.
         */
        public Type build() {
            return this.value;
        }
    }

    /**
     * Access helper failure.
     */
    public static final class Failure extends RuntimeException {

        /**
         * Serial version unique identifier.
         */
        private static final long serialVersionUID = -3980309747676686470L;

        /**
         * List of per default not wrapped failures.
         */
        private static final Set<Type> UNWRAPPED = EnumSet.of(Type.SUPPORT);

        /**
         * Failure type.
         */
        private final Type type;

        /**
         * Failure type enumeration.
         */
        public static enum Type {

            /**
             * Unknown failure.
             */
            UNKNOWN("unknown failure"),

            /**
             * Index out of bounds failure.
             */
            BOUNDS("index out of bounds failure"),

            /**
             * Number format failure.
             */
            NUMBER("number format failure"),

            /**
             * Illegal argument failure.
             */
            ARGUMENT("illegal argument failure"),

            /**
             * Illegal access failure.
             */
            ACCESS("illegal access failure"),

            /**
             * Invocation target failure.
             */
            TARGET("invocation target failure"),

            /**
             * Invalid field failure.
             */
            FIELD("invalid field failure"),

            /**
             * Invalid method failure.
             */
            METHOD("invalid method failure"),

            /**
             * Invalid class failure.
             */
            CLASS("invalid class failure"),

            /**
             * Instantiation failure.
             */
            CREATION("instantiation failure"),

            /**
             * Security failure.
             */
            SECURITY("security failure"),

            /**
             * Failure handling mode not supported.
             */
            SUPPORT("support failure");

            /**
             * Failure specific message.
             */
            protected final String message;

            /**
             * Create failure type with given failure specific message.
             *
             * @param  message  failure specific message.
             */
            private Type(String message) {
                this.message = message;
            }
        }

        /**
         * Failure mode.
         */
        public static enum Mode {

            /**
             * Default failure handling mode (method specific).
             */
            DEFAULT,

            /**
             * Failure handling mode to throws exceptions.
             */
            THROW_EXCEPTION,

            /**
             * Failure handling mode to returns {@code null}.
             */
            RETURN_NULL,
        }

        /**
         * Failure mapping style.
         */
        protected static enum Style {

            /**
             * Default failure mapping style.
             */
            DEFAULT,

            /**
             * Merged failure mapping style.
             */
            MERGED,

            /**
             * Wrapped failure mapping style.
             */
            WRAPPED,

            /**
             * Unwrapped failure mapping style.
             */
            UNWRAPPED
        }

        /**
         * Create failure with given failure type, given failure message, and given failure cause.
         *
         * @param  type     failure type.
         * @param  message  failure message.
         * @param  cause    failure cause.
         */
        private Failure(Type type, String message, Throwable cause) {
            super(Failure.message(type, message), cause);
            if (type == null) {
                throw new IllegalArgumentException("type must not be null");
            }
            this.type = type;
        }

        /**
         * Create failure with given failure type and given failure message.
         *
         * @param   type     failure type.
         * @param   message  failure message.
         *
         * @return  failure exception.
         */
        protected static Failure create(Type type, String message) {
            return Failure.strip(new Failure(type, message, null), 1);
        }

        /**
         * Create type not supported failure with given class type.
         *
         * @param   type  class type.
         *
         * @return  failure exception.
         */
        protected static Failure create(Class<?> type) {
            return Failure.strip(new Failure(Failure.Type.CLASS, //
                        "type not supported [" + type.getName() + "]", null), 1);
        }

        /**
         * Create index out of bound failure with given index value and given container size.
         *
         * @param   index  index value.
         * @param   size   container size.
         *
         * @return  failure exception.
         */
        protected static Failure create(int index, int size) {
            return Failure.strip(new Failure(Failure.Type.BOUNDS, //
                        "index out of bounds " + Helper.message(index, size), null), 1);
        }

        /**
         * Create failure handling mode not supported failure with given failure handling mode and
         * given failure cause.
         *
         * @param   mode   failure handling mode.
         * @param   cause  failure cause.
         *
         * @return  failure exception.
         */
        protected static Failure create(Mode mode, Throwable cause) {
            return Failure.strip(new Failure(Failure.Type.SUPPORT, //
                        "mode not supported [" + mode + "]", cause), 1);
        }

        /**
         * Create wrapped failure using default failure mapping style with given failure message and
         * failure type derived from given failure cause.
         *
         * @param   message  failure message.
         * @param   cause    failure cause.
         *
         * @return  failure exception.
         */
        protected static Failure create(String message, Throwable cause) {
            return Failure.strip(Failure.wrap(Style.WRAPPED, message, cause), 2);
        }

        /**
         * Create failure using given failure mapping style with given failure message and failure
         * type derived from given failure cause.
         *
         * @param   style    failure mapping style.
         * @param   message  failure message.
         * @param   cause    failure cause.
         *
         * @return  failure exception.
         */
        protected static Failure create(Style style, String message, Throwable cause) {
            return Failure.strip(Failure.wrap(style, message, cause), 2);
        }

        /**
         * Create wrapped failure with given failure message and with failure type derived from
         * given failure cause.
         *
         * @param   style    failure style.
         * @param   message  failure message.
         * @param   cause    failure cause.
         *
         * @return  failure exception.
         */
        private static Failure wrap(Style style, String message, Throwable cause) {
            if (cause instanceof Failure) {
                Failure failure = (Failure) cause;
                if (UNWRAPPED.contains(failure.getType())) {
                    return failure;
                }
                switch (style) {
                    case MERGED:
                        return new Failure(failure.getType(), message, failure.getCause());

                    case UNWRAPPED:
                        return failure;

                    default:
                        return new Failure(failure.getType(), message, failure);
                }
            } else if (cause instanceof IndexOutOfBoundsException) {
                return new Failure(Failure.Type.BOUNDS, message, cause);
            } else if (cause instanceof NumberFormatException) {
                return new Failure(Failure.Type.NUMBER, message, cause);
            } else if (cause instanceof IllegalArgumentException) {
                return new Failure(Failure.Type.ARGUMENT, message, cause);
            } else if (cause instanceof IllegalAccessException) {
                return new Failure(Failure.Type.ACCESS, message, cause);
            } else if (cause instanceof InstantiationException) {
                return new Failure(Failure.Type.CREATION, message, cause);
            } else if (cause instanceof SecurityException) {
                return new Failure(Failure.Type.SECURITY, message, cause);
            } else if (cause instanceof NoSuchFieldException) {
                return new Failure(Failure.Type.FIELD, message, cause);
            } else if (cause instanceof NoSuchMethodException) {
                return new Failure(Failure.Type.METHOD, message, cause);
            } else if (cause instanceof ClassNotFoundException) {
                return new Failure(Failure.Type.CLASS, message, cause);
            } else if (cause instanceof InvocationTargetException) {
                return new Failure(Failure.Type.TARGET, message, cause.getCause());
            } else if (cause instanceof UnsupportedOperationException) {
                return new Failure(Failure.Type.SUPPORT, message, cause);
            }
            return new Failure(Failure.Type.UNKNOWN, message, cause);
        }

        /**
         * Strip stack trace for given number of entries of given failure exception to show actual
         * failure source as first position in stack trace.
         *
         * @param   failure  failure exception.
         * @param   entries  number of entries.
         *
         * @return  failure exception.
         */
        private static Failure strip(Failure failure, int entries) {
            StackTraceElement[] trace = failure.getStackTrace();
            failure.setStackTrace(Arrays.copyOfRange(trace, entries, trace.length));
            return failure;
        }

        /**
         * Create standard failure message using given failure type and failure message.
         *
         * @param   type     failure type.
         * @param   message  failure message.
         *
         * @return  standard failure message.
         */
        private static String message(Type type, String message) {
            if (message != null) {
                if (message.charAt(0) == '[') {
                    return type.message + " " + message;
                }
                return message;
            }
            return type.message;
        }

        /**
         * Return invocation target exception root cause with given failure class type. Note: if the
         * failure is not based on an invocation target exception, the failure itself is returned.
         *
         * @param   <Type>  failure type.
         * @param   type    failure class type.
         *
         * @return  invocation target exception root cause.
         */
        @SuppressWarnings("hiding")
        public <Type extends Throwable> Type getTarget(Class<Type> type) {
            if (this.type != Failure.Type.TARGET) {
                return type.cast(this);
            }
            Throwable cause = this.getCause();
            while ((cause != null) && (cause instanceof Failure)) {
                if (((Failure) cause).type != Failure.Type.TARGET) {
                    return type.cast(this); // Should never happen!
                }
                cause = cause.getCause();
            }
            return type.cast(((cause != null) && type.isAssignableFrom(cause.getClass())) ? cause : this);
        }

        /**
         * Return none-framework failure root cause with given failure class type. Note: if the
         * failure is not based on any none-framework failure root cause, the failure itself is
         * returned.
         *
         * @param   <Type>  failure type.
         * @param   type    failure class type.
         *
         * @return  failure root cause.
         */
        @SuppressWarnings("hiding")
        public <Type extends Throwable> Type getRoot(Class<Type> type) {
            Throwable cause = this.getCause();
            while ((cause != null) && (cause instanceof Failure)) {
                cause = cause.getCause();
            }
            return type.cast((cause != null) ? cause : this);
        }

        /**
         * Return failure type.
         *
         * @return  failure type.
         */
        public Type getType() {
            return this.type;
        }

        /**
         * Failure message helper.
         */
        protected static class Helper {

            /**
             * Return list of field names for given list of fields.
             *
             * @param   fields  list of fields.
             *
             * @return  list of field names.
             */
            private static String pretty(Field[] fields) {
                if (fields == null) {
                    return null;
                }
                StringBuilder builder = new StringBuilder("[");
                for (Field field : fields) {
                    if (builder.length() > 1) {
                        builder.append(", ");
                    }
                    builder.append((field != null) ? field.getName() : "null");
                }
                return builder.append("]").toString();
            }

            /**
             * Return list of method names for given list of methods.
             *
             * @param   methods  list of methods.
             *
             * @return  list of method names.
             */
            private static String pretty(Method[] methods) {
                if (methods == null) {
                    return null;
                }
                StringBuilder builder = new StringBuilder("[");
                for (Method method : methods) {
                    if (builder.length() > 1) {
                        builder.append(", ");
                    }
                    builder.append((method != null) ? method.getName() : "null");
                }
                return builder.append("]").toString();
            }

            /**
             * Return list of class names for given list of class types.
             *
             * @param   types  list of class types.
             *
             * @return  list of class names.
             */
            private static String pretty(Class<?>[] types) {
                if (types == null) {
                    return null;
                }
                StringBuilder builder = new StringBuilder("[");
                for (Class<?> type : types) {
                    if (builder.length() > 1) {
                        builder.append(", ");
                    }
                    builder.append((type != null) ? type.getName() : "null");
                }
                return builder.append("]").toString();
            }

            /**
             * Return list of object names for given list of objects.
             *
             * @param   objects  list of objects.
             *
             * @return  list of object names.
             */
            private static String pretty(Object[] objects) {
                if (objects == null) {
                    return null;
                }
                StringBuilder builder = new StringBuilder("[");
                for (Object object : objects) {
                    if (builder.length() > 1) {
                        builder.append(", ");
                    }
                    builder.append((object != null) ? Helper.pretty(object) : "null");
                }
                return builder.append("]").toString();
            }

            /**
             * Return pretty string representation of given target object.
             *
             * @param   object  target object.
             *
             * @return  pretty string representation.
             */
            private static String pretty(Object object) {
                if (object == null) {
                    return "null";
                }
                Class<?> type = object.getClass();
                if (type.isArray()) {
                    Class<?> ctype = type.getComponentType();
                    if (ctype.isPrimitive()) {
                        if (ctype == long.class) {
                            return Arrays.toString((long[]) object);
                        } else if (ctype == int.class) {
                            return Arrays.toString((int[]) object);
                        } else if (ctype == boolean.class) {
                            return Arrays.toString((boolean[]) object);
                        } else if (ctype == double.class) {
                            return Arrays.toString((double[]) object);
                        } else if (ctype == float.class) {
                            return Arrays.toString((float[]) object);
                        } else if (ctype == short.class) {
                            return Arrays.toString((short[]) object);
                        } else if (ctype == byte.class) {
                            return Arrays.toString((byte[]) object);
                        } else if (ctype == char.class) {
                            return Arrays.toString((char[]) object);
                        }

                        // should never happen!
                        throw Failure.create(Failure.Type.ARGUMENT, "not primitive [" + ctype.getName() + "]");
                    } else if (ctype == Class.class) {
                        return Helper.pretty((Class<?>[]) object);
                    } else if (ctype == Method.class) {
                        return Helper.pretty((Method[]) object);
                    } else if (ctype == Field.class) {
                        return Helper.pretty((Field[]) object);
                    }
                    return Helper.pretty((Object[]) object);
                } else if (object instanceof Class) {
                    return ((Class<?>) object).getName();
                } else if (object instanceof Method) {
                    return ((Method) object).getName();
                } else if (object instanceof Field) {
                    return ((Field) object).getName();
                }
                return object.toString();
            }

            /**
             * Return failure message details for given index value and given container size.
             *
             * @param   index  index value.
             * @param   size   container size.
             *
             * @return  failure message details.
             */
            public static String message(int index, int size) {
                return "[index=" + index + ", size=" + size + "]";
            }

            /**
             * Return failure message details for given target object and declared field name.
             *
             * @param   target  target object.
             * @param   name    declared field name.
             *
             * @return  failure message details.
             */
            public static String message(Object target, String name) {
                return "[target=" + Helper.pretty(target) + ", name=" + name + "]";
            }

            /**
             * Return failure message details for given target object, declared field name, value
             * object.
             *
             * @param   target  target object.
             * @param   name    declared field name.
             * @param   value   value object.
             *
             * @return  failure message details.
             */
            public static String message(Object target, String name, Object value) {
                return "[target=" + Helper.pretty(target) + ", name=" + name + ", value=" + value + "]";
            }

            /**
             * Return failure message details for given target object and declared field.
             *
             * @param   target  target object.
             * @param   field   declared field.
             *
             * @return  failure message details.
             */
            public static String message(Object target, Field field) {
                return "[target=" + Helper.pretty(target) + ", field=" + Helper.pretty(field) + "]";
            }

            /**
             * Return failure message details for given target object, declared field, and value
             * object.
             *
             * @param   target  target object.
             * @param   field   declared field.
             * @param   value   value object.
             *
             * @return  failure message details.
             */
            public static String message(Object target, Field field, Object value) {
                return "[target=" + Helper.pretty(target) + ", field=" + Helper.pretty(field) //
                    + ", value=" + value + "]";
            }

            /**
             * Return failure message details for given target object, declared method, and call
             * arguments.
             *
             * @param   target  target object.
             * @param   method  declared method.
             * @param   args    call arguments.
             *
             * @return  failure message details.
             */
            public static String message(Object target, Method method, Object[] args) {
                return "[target=" + target + ", method=" + Helper.pretty(method) //
                    + ", args=" + Helper.pretty(args) + "]";
            }

            /**
             * Return failure message details for given declaring class type and declared parameter
             * types.
             *
             * @param   type   declaring class type.
             * @param   types  declared parameter types.
             *
             * @return  failure message details.
             */
            public static String message(Class<?> type, Class<?>... types) {
                return "[type=" + Helper.pretty(type) + ", types=" + Helper.pretty(types) + "]";
            }

            /**
             * Return failure message details for given declaring class type and declared parameter
             * types.
             *
             * @param   type   declaring class type.
             * @param   types  declared parameter types.
             * @param   args   call arguments.
             *
             * @return  failure message details.
             */
            public static String message(Class<?> type, Class<?>[] types, Object... args) {
                return "[type=" + Helper.pretty(type) + ", types=" + Helper.pretty(types) //
                    + ", args=" + Helper.pretty(args) + "]";
            }

            /**
             * Return failure message details for given declaring class type and declared field
             * name.
             *
             * @param   type  declaring class type.
             * @param   name  declared field name.
             *
             * @return  failure message details.
             */
            public static String message(Class<?> type, String name) {
                return "[type=" + Helper.pretty(type) + ", name=" + name + "]";
            }

            /**
             * Return failure message details for given declaring class type, declared method name,
             * and declared parameter types.
             *
             * @param   type   declaring class type.
             * @param   name   declared method name.
             * @param   types  declared parameter types.
             *
             * @return  failure message details.
             */
            public static String message(Class<?> type, String name, Class<?>... types) {
                return "[type=" + Helper.pretty(type) + ", name=" + name //
                    + ", types=" + Helper.pretty(types) + "]";
            }
        }
    }

    /**
     * Field specific access methods.
     */
    public static abstract class Fields {

        /**
         * Resolve declared field for given declaring class type using given declared field name and
         * default failure handling mode.
         *
         * @param   type  declaring class type.
         * @param   name  declared field name.
         *
         * @return  declared field.
         */
        public static Field resolve(Class<?> type, String name) {
            return Fields.resolve(type, name, Failure.Mode.DEFAULT);
        }

        /**
         * Resolve declared field for given declaring class type using given declared field name and
         * given failure handling mode.
         *
         * @param   type  declaring class type.
         * @param   name  declared field name.
         * @param   mode  failure handling mode.
         *
         * @return  declared field.
         */
        public static Field resolve(Class<?> type, String name, Failure.Mode mode) {
            try {
                return Base.accessible(type.getDeclaredField(name));
            } catch (Exception except) {
                if (type.getSuperclass() != null) {
                    try {
                        return Fields.resolve(type.getSuperclass(), name, mode);
                    } catch (Failure failure) {
                        // ignore failure, report first!
                    }
                }
                switch (mode) {
                    case DEFAULT:
                    case THROW_EXCEPTION:
                        throw Failure.create(Failure.Helper.message(type, name), except);

                    case RETURN_NULL:
                        return null;

                    default:
                        throw Failure.create(mode, except);
                }
            }
        }

        /**
         * Resolve list of declared fields for given declaring class type, given field name, and
         * given field class type.
         *
         * @param   type   declaring class type.
         * @param   name   declared field name (may be null).
         * @param   ftype  field class type (may be null).
         *
         * @return  list of declared fields.
         */
        public static List<Field> resolve(Class<?> type, String name, Class<?> ftype) {
            return Fields.resolve(type, name, ftype, Resolve.Type.SUPER);
        }

        /**
         * Resolve list of declared fields for given declaring class type, given declaring field
         * name, given field class type, and field class type relation mode.
         *
         * @param   type   declaring class type.
         * @param   name   declared field name (may be null).
         * @param   ftype  field class type (may be null).
         * @param   rtype  field class type relation mode (may be null).
         *
         * @return  list of declared fields.
         */
        public static List<Field> resolve(Class<?> type, String name, Class<?> ftype, Resolve.Type rtype) {
            return Fields.resolve(type, (name != null) ? Arrays.asList(name.intern()) : null, ftype, rtype);
        }

        /**
         * Resolve list of declared fields for given declaring class type, given list of declared
         * field names, given field class type, and field class type relation mode.
         *
         * @param   type   declaring class type.
         * @param   names  list of declared field name (may be null).
         * @param   ftype  field class type (may be null).
         * @param   rtype  field class type relation mode.
         *
         * @return  list of declared fields.
         */
        public static List<Field> resolve(Class<?> type, List<String> names, Class<?> ftype, Resolve.Type rtype) {
            List<Field> list = new ArrayList<Field>();
            while (type != null) {
                for (Field field : type.getDeclaredFields()) {
                    if ((names != null) && !names.contains(field.getName())) {
                        continue;
                    } else if (ftype != null) {
                        switch (rtype) {
                            case EXACT:
                                if (!field.getType().equals(ftype)) {
                                    continue;
                                }
                                break;

                            case SUPER:
                                if (!Classes.assignable(field.getType(), ftype)) {
                                    continue;
                                }
                                break;

                            case CHILD:
                                if (!Classes.assignable(ftype, field.getType())) {
                                    continue;
                                }
                                break;

                            default:
                                throw Failure.create(Failure.Type.ARGUMENT,
                                    "resolve type not supported [" + rtype + "]");
                        }
                    }
                    list.add(Base.accessible(field));
                }
                type = type.getSuperclass();
            }
            return list;
        }

        /**
         * Resolve list of declared fields for given declaring class type, given field class type,
         * and field class type relation mode.
         *
         * @param   type   declaring class type.
         * @param   ftype  field class type (may be null).
         * @param   rtype  field class type relation mode (may be null).
         *
         * @return  list of declared fields.
         */
        public static List<Field> resolve(Class<?> type, Class<?> ftype, Resolve.Type rtype) {
            return Fields.resolve(type, (List<String>) null, ftype, rtype);
        }

        /**
         * Resolve annotation field value mapping for given annotation target object.
         *
         * @param   <Type>  annotation type.
         * @param   target  annotation target object.
         *
         * @return  annotation field value mapping.
         */
        private static <Type> Map<String, Type> resolve(Annotation target) {
            return Fields.get(Proxy.getInvocationHandler(target), "memberValues");
        }

        /**
         * Change modifier (see {@link Modifier}} of given declared field with given new modifier
         * value.
         *
         * @param   field     declared field.
         * @param   modifier  modifier value.
         *
         * @return  previous modifier value.
         */
        protected static int modify(Field field, int modifier) {
            return Fields.set(field, Fields.resolve(Field.class, "modifiers"), modifier);
        }

        /**
         * Resolve static field value from given target class type using given field name.
         *
         * @param   <Type>  target field value type.
         * @param   type    target class type.
         * @param   name    field name.
         *
         * @return  field value.
         */
        public static <Type> Type get(Class<?> type, String name) {
            try {
                return Fields.get(null, Fields.resolve(type, name));
            } catch (Exception except) {
                throw Failure.create(Failure.Helper.message(type, name), except);
            }
        }

        /**
         * Resolve field value from given target object using given field name.
         *
         * @param   <Type>  target field value type.
         * @param   target  target object.
         * @param   name    field name.
         *
         * @return  field value.
         */
        public static <Type> Type get(Object target, String name) {
            if (target == null) {
                throw Failure.create(Failure.Type.ARGUMENT, Failure.Helper.message((Object) null, name));
            } else if ((target instanceof Annotation) && Proxy.isProxyClass(target.getClass())) {
                return Fields.get((Annotation) target, name);
            }
            try {
                return Fields.get(target, Fields.resolve(target.getClass(), name));
            } catch (Exception except) {
                throw Failure.create(Failure.Helper.message(target, name), except);
            }
        }

        /**
         * Resolve field value from given target object using given declared field.
         *
         * @param   <Type>  target field value type.
         * @param   target  target object.
         * @param   field   declared field.
         *
         * @return  field value.
         */
        @SuppressWarnings("unchecked")
        public static <Type> Type get(Object target, Field field) {
            if ((target == null) && !Modifier.isStatic(field.getModifiers())) {
                throw Failure.create(Failure.Type.ACCESS, Failure.Helper.message((Object) null, field));
            }
            try {
                return (Type) field.get(target);
            } catch (Exception except) {
                throw Failure.create(Failure.Helper.message(target, field), except);
            }
        }

        /**
         * Resolve field value from given target annotation object using given field name.
         *
         * @param   <Type>  target field value type.
         * @param   target  target annotation object.
         * @param   name    field name.
         *
         * @return  field value.
         */
        @SuppressWarnings("unchecked")
        private static <Type> Type get(Annotation target, String name) {
            return (Type) Fields.resolve(target).get(name);
        }

        /**
         * Update field value of given target object using given declared field name and return
         * previous field value. The target object is used to determine the parent class of the
         * field.
         *
         * @param   <Type>  update field value type.
         * @param   target  target object (used to determine class type).
         * @param   name    declared field name.
         * @param   value   update field value.
         *
         * @return  previous field value.
         */
        public static <Type> Type set(Object target, String name, Type value) {
            if (target == null) {
                throw Failure.create(Failure.Type.ARGUMENT, Failure.Helper.message(null, name, value));
            } else if (target instanceof Annotation) {
                return Fields.set((Annotation) target, name, value);
            }
            try {
                return Fields.set(target, Fields.resolve(target.getClass(), name), value);
            } catch (Exception except) {
                throw Failure.create(Failure.Helper.message(target, name, value), except);
            }
        }

        /**
         * Update field value of given target object using given declared field and return previous
         * field value. Use target object {@code null} for static fields. Note: you have to change
         * the field modifier {@link #modify(Field, int)} to efficiently change final (static)
         * fields.
         *
         * @param   <Type>  update field value type.
         * @param   target  target object (null for static fields).
         * @param   field   declared field.
         * @param   value   update field value.
         *
         * @return  previous field value.
         */
        public static <Type> Type set(Object target, Field field, Type value) {
            try {
                Type before = Fields.get(target, field);
                field.set(target, value);
                return before;
            } catch (Exception except) {
                throw Failure.create(Failure.Helper.message(target, field, value), except);
            }
        }

        /**
         * Update field value of given target annotation object using given declared field name and
         * return previous field value. The target annotation object is used to determine the parent
         * class of the field.
         *
         * @param   <Type>  update field value type.
         * @param   target  target annotation object (used to determine class type).
         * @param   name    declared field name.
         * @param   value   update field value.
         *
         * @return  previous annotation field value.
         */
        private static <Type> Type set(Annotation target, String name, Type value) {
            if (value == null) {
                throw Failure.create(Failure.Type.ARGUMENT, Failure.Helper.message(target, name, null));
            }
            Map<String, Type> values = Fields.resolve(target);
            if (!Classes.assignable(Methods.resolve(target.getClass(), name).getReturnType(), value.getClass())) {
                throw Failure.create(Failure.Type.ARGUMENT, Failure.Helper.message(target, name, value));
            }
            Type before = values.get(name);
            values.put(name, value);
            return before;
        }
    }

    /**
     * Method specific access methods.
     */
    public static abstract class Methods {

        /**
         * Resolve declared method for given declaring class type with given declared method name,
         * given method argument types, and default failure handling mode.
         *
         * @param   type   declaring class type.
         * @param   name   declared method name.
         * @param   types  method argument types.
         *
         * @return  declared method.
         */
        public static Method resolve(Class<?> type, String name, Class<?>... types) {
            return Methods.resolve(type, name, Failure.Mode.DEFAULT, types);
        }

        /**
         * Resolve declared method for given declaring class type with given declared method name,
         * failure handling mode, and method argument types.
         *
         * @param   type   declaring class type.
         * @param   name   declared method name.
         * @param   mode   failure handling mode.
         * @param   types  method argument types.
         *
         * @return  declared method.
         */
        public static Method resolve(Class<?> type, String name, Failure.Mode mode, Class<?>... types) {
            try {
                return Base.accessible(type.getDeclaredMethod(name, types));
            } catch (Exception except) {
                if (type.getSuperclass() != null) {
                    try {
                        return Methods.resolve(type.getSuperclass(), name, mode, types);
                    } catch (Failure failure) {
                        // ignore failure, report first!
                    }
                }
                switch (mode) {
                    case DEFAULT:
                    case THROW_EXCEPTION:
                        throw Failure.create(Failure.Helper.message(type, name, types), except);

                    case RETURN_NULL:
                        return null;

                    default:
                        throw Failure.create(mode, except);
                }
            }
        }

        /**
         * Resolve list of declared method for given declaring class type with given declared method
         * name, method argument types, and method return type.
         *
         * @param   type    declaring class type.
         * @param   name    declared method name.
         * @param   atypes  method argument types.
         * @param   rtype   method return type.
         *
         * @return  list of declared method.
         */
        public static List<Method> resolve(Class<?> type, String name, Class<?>[] atypes, Class<?> rtype) {
            List<Method> list = new ArrayList<Method>();
            if (name != null) {
                name = name.intern();
            }
            while (type != null) {
                for (Method method : type.getDeclaredMethods()) {
                    if ((name != null) && (name != method.getName())) {
                        continue;
                    } else if (!Classes.assignable(method.getParameterTypes(), atypes)) {
                        continue;
                    } else if (!Classes.assignable(method.getReturnType(), rtype)) {
                        continue;
                    }
                    list.add(Base.accessible(method));
                }
                type = type.getSuperclass();
            }
            return list;
        }

        /**
         * Invoke declared method given by declared method name and method argument types on given
         * target object with default failure handler mode using given call arguments and return
         * result object.
         *
         * @param   <Type>  invocation result type.
         * @param   target  target object.
         * @param   name    declared method name.
         * @param   types   method argument types.
         * @param   args    call arguments.
         *
         * @return  result object.
         */
        public static <Type> Type invoke(Object target, String name, Class<?>[] types, Object... args) {
            return Methods.invoke(target, name, types, Failure.Mode.DEFAULT, args);
        }

        /**
         * Invoke declared method given by declared method name and method argument types on given
         * target object with given failure handler mode using given call arguments and return
         * result object.
         *
         * @param   <Type>  invocation result type.
         * @param   target  target object.
         * @param   name    declared method name.
         * @param   types   method argument types.
         * @param   mode    failure handler mode.
         * @param   args    call arguments.
         *
         * @return  result object.
         */
        public static <Type> Type invoke(Object target, String name, Class<?>[] types, //
                Failure.Mode mode, Object... args) {
            return Methods.invoke(target, target.getClass(), name, types, mode, args);
        }

        /**
         * Invoke declared method given by declared class type, declared method name, and method
         * argument types on given target object with default failure handler mode using given call
         * arguments and return result object.
         *
         * @param   <Type>  invocation result type.
         * @param   target  target object.
         * @param   type    declaring class type.
         * @param   name    declared method name.
         * @param   types   method argument types.
         * @param   args    call arguments.
         *
         * @return  result object.
         */
        public static <Type> Type invoke(Object target, Class<?> type, String name, Class<?>[] types, Object... args) {
            return Methods.invoke(target, type, name, types, Failure.Mode.DEFAULT, args);
        }

        /**
         * Invoke declared method given by declared class type, declared method name, and method
         * argument types on given target object with given failure handler mode using given call
         * arguments and return result object.
         *
         * @param   <Type>  invocation result type.
         * @param   target  target object.
         * @param   type    declaring class type.
         * @param   name    declared method name.
         * @param   types   method argument types.
         * @param   mode    failure handler mode.
         * @param   args    call arguments.
         *
         * @return  result object.
         */
        public static <Type> Type invoke(Object target, Class<?> type, String name, Class<?>[] types, //
                Failure.Mode mode, Object... args) {
            Method method = Methods.resolve(type, name, mode, types);
            return (method != null) ? Methods.<Type>invoke(target, method, mode, args) : null;
        }

        /**
         * Invoke given declared method on given target object using given call arguments and return
         * result object.
         *
         * @param   <Type>  invocation result type.
         * @param   target  target object.
         * @param   method  declared method.
         * @param   args    call arguments.
         *
         * @return  result object.
         */
        public static <Type> Type invoke(Object target, Method method, Object... args) {
            return Methods.invoke(target, method, Failure.Mode.DEFAULT, args);
        }

        /**
         * Invoke given declared method on given target object using given call arguments and return
         * result object.
         *
         * @param   <Type>  invocation result type.
         * @param   target  target object.
         * @param   method  declared method.
         * @param   mode    failure handler mode.
         * @param   args    call arguments.
         *
         * @return  result object.
         */
        @SuppressWarnings("unchecked")
        public static <Type> Type invoke(Object target, Method method, Failure.Mode mode, Object... args) {
            try {
                return (Type) method.invoke(target, args);
            } catch (Exception except) {
                switch (mode) {
                    case DEFAULT:
                    case THROW_EXCEPTION:
                        throw Failure.create(Failure.Helper.message(target, method, args), except);

                    case RETURN_NULL:
                        return null;

                    default:
                        throw Failure.create(mode, except);
                }
            }
        }
    }

    /**
     * Class specific access methods.
     */
    public static abstract class Classes {

        /**
         * Name to class type mapping.
         */
        private static final Map<String, Class<?>> NAME_MAP = //
            AccessHelper.builder(new HashMap<String, Class<?>>()) //
            .add(long.class.getName(), long.class).add(Long.class.getSimpleName(), Long.class) //
            .add(int.class.getName(), int.class).add(Integer.class.getSimpleName(), Integer.class) //
            .add(short.class.getName(), short.class).add(Short.class.getSimpleName(), Short.class) //
            .add(byte.class.getName(), byte.class).add(Byte.class.getSimpleName(), Byte.class) //
            .add(double.class.getName(), double.class).add(Double.class.getSimpleName(), Double.class) //
            .add(float.class.getName(), float.class).add(Float.class.getSimpleName(), Float.class) //
            .add(boolean.class.getName(), boolean.class).add(Boolean.class.getSimpleName(), Boolean.class) //
            .add(char.class.getName(), char.class).add(Character.class.getSimpleName(), Character.class) //
            .add(void.class.getName(), void.class).add(Void.class.getSimpleName(), Void.class) //
            .add("class", Class.class).add(Class.class.getSimpleName(), Class.class) //
            .build();

        /**
         * Primitive to boxed type mapping.
         */
        private static final Map<String, Class<?>> BOX_MAP = //
            AccessHelper.builder(new HashMap<String, Class<?>>()) //
            .add(long.class.getName(), Long.class).add(Long.class.getSimpleName(), long.class) //
            .add(int.class.getName(), Integer.class).add(Integer.class.getSimpleName(), int.class) //
            .add(short.class.getName(), Short.class).add(Short.class.getSimpleName(), short.class) //
            .add(byte.class.getName(), Byte.class).add(Byte.class.getSimpleName(), byte.class) //
            .add(double.class.getName(), Double.class).add(Double.class.getSimpleName(), double.class) //
            .add(float.class.getName(), Float.class).add(Float.class.getSimpleName(), float.class) //
            .add(boolean.class.getName(), Boolean.class).add(Boolean.class.getSimpleName(), boolean.class) //
            .add(char.class.getName(), Character.class).add(Character.class.getSimpleName(), char.class) //
            .add(void.class.getName(), Void.class).add(Void.class.getSimpleName(), void.class).build();

        /** */
        private static final Class<?>[] CREATE_TYPES = new Class[] {
            String.class, byte[].class, int.class, int.class
        };

        /**
         * Find and resolve class type using default class loader of actual thread, given declared
         * class name, and default failure handling mode.
         *
         * @param   <Type>  result class type.
         * @param   name    declared class name.
         *
         * @return  class type.
         */
        protected static <Type> Class<Type> find(String name) {
            return Classes.find(name, Failure.Mode.DEFAULT);
        }

        /**
         * Find and resolve class type using default class loader of actual thread, given declared
         * class name, and given failure handling mode.
         *
         * @param   <Type>  result class type.
         * @param   name    declared class name.
         * @param   mode    failure handling mode.
         *
         * @return  class type.
         */
        protected static <Type> Class<Type> find(String name, Failure.Mode mode) {
            @SuppressWarnings("unchecked")
            Class<Type> type = (Class<Type>) NAME_MAP.get(name);
            if (type != null) {
                return type;
            }
            type = Classes.resolve(name, Failure.Mode.RETURN_NULL);
            if (type != null) {
                return type;
            }
            type = Classes.resolve("java.lang." + name, Failure.Mode.RETURN_NULL);
            if (type != null) {
                return type;
            } else if (mode == Failure.Mode.RETURN_NULL) {
                return null;
            }

            // throw exception in case of failure!
            return Classes.resolve(name, mode);
        }

        /**
         * Create class type using default class loader of actual thread, given declared class name,
         * given byte code buffer, and given failure handling mode.
         *
         * @param   <Type>  result class type.
         * @param   name    declared class name.
         * @param   buffer  class byte code buffer.
         *
         * @return  class type.
         */
        public static <Type> Class<Type> create(String name, byte[] buffer) {
            return create(Thread.currentThread().getContextClassLoader(), name, buffer,
                    Failure.Mode.DEFAULT);
        }

        /**
         * Create class type using default class loader of actual thread, given declared class name,
         * given byte code buffer, and given failure handling mode.
         *
         * @param   <Type>  result class type.
         * @param   name    declared class name.
         * @param   buffer  class byte code buffer.
         * @param   mode    failure handling mode.
         *
         * @return  class type.
         */
        public static <Type> Class<Type> create(String name, byte[] buffer, Failure.Mode mode) {
            return create(Thread.currentThread().getContextClassLoader(), name, buffer, mode);
        }

        /**
         * Create class type using given class loader, given declared class name, given byte code
         * buffer, and default failure handling mode.
         *
         * @param   <Type>  result class type.
         * @param   loader  class loader.
         * @param   name    declared class name.
         * @param   buffer  class byte code buffer.
         *
         * @return  class type.
         */
        public static <Type> Class<Type> create(ClassLoader loader, String name, byte[] buffer) {
            return create(loader, name, buffer, Failure.Mode.DEFAULT);
        }

        /**
         * Create class type using given class loader, given declared class name, given byte code
         * buffer, and given failure handling mode.
         *
         * @param   <Type>  result class type.
         * @param   loader  class loader.
         * @param   name    declared class name.
         * @param   buffer  class byte code buffer.
         * @param   mode    failure handling mode.
         *
         * @return  class type.
         */
        public static <Type> Class<Type> create(ClassLoader loader, String name, byte[] buffer,
                Failure.Mode mode) {
            return Methods.invoke(loader, "defineClass", CREATE_TYPES, mode, new Object[] {
                name, buffer, 0, buffer.length
            });
        }

        /**
         * Resolve class type using default class loader of actual thread, given declared class
         * name, and default failure handling mode.
         *
         * @param   <Type>  result class type.
         * @param   name    declared class name.
         *
         * @return  class type.
         */
        public static <Type> Class<Type> resolve(String name) {
            return Classes.resolve(Thread.currentThread().getContextClassLoader(), name);
        }

        /**
         * Resolve class type using default class loader of actual thread, given declared class
         * name, and given failure handling mode.
         *
         * @param   <Type>  result class type.
         * @param   name    declared class name.
         * @param   mode    failure handling mode.
         *
         * @return  class type.
         */
        public static <Type> Class<Type> resolve(String name, Failure.Mode mode) {
            return Classes.resolve(Thread.currentThread().getContextClassLoader(), name, mode);
        }

        /**
         * Resolve inner class type of declaring class type, declared class name, and failure
         * handling mode. If the class cannot be found it depends on the given failure handling
         * mode,
         *
         * @param   <Type>  result class type.
         * @param   type    declaring class type.
         * @param   name    declared class name.
         *
         * @return  class type.
         */
        public static <Type> Class<Type> resolve(Class<?> type, String name) {
            return Classes.resolve(type, name, Failure.Mode.DEFAULT);
        }

        /**
         * Resolve inner class type of declaring class type, declared class name, and failure
         * handling mode. If the class cannot be found it depends on the given failure handling
         * mode,
         *
         * @param   <Type>  result class type.
         * @param   type    declaring class type.
         * @param   name    declared class name.
         * @param   mode    failure handling mode.
         *
         * @return  class type.
         */
        public static <Type> Class<Type> resolve(Class<?> type, String name, Failure.Mode mode) {
            return Classes.resolve(type.getClassLoader(), type.getName() + "$" + name, mode);
        }

        /**
         * Resolve class type using given class loader, declared class name, and default failure
         * mode.
         *
         * @param   <Type>  result class type.
         * @param   loader  class loader.
         * @param   name    declared class name.
         *
         * @return  class type.
         */
        public static <Type> Class<Type> resolve(ClassLoader loader, String name) {
            return Classes.resolve(loader, name, Failure.Mode.DEFAULT);
        }

        /**
         * Resolve class type using given class loader, declared class name, and failure handling
         * mode. If the class cannot be found it depends on the given failure handling mode,
         *
         * @param   <Type>  result class type.
         * @param   loader  class loader.
         * @param   name    declared class name.
         * @param   mode    failure handling mode.
         *
         * @return  class type.
         */
        @SuppressWarnings("unchecked")
        public static <Type> Class<Type> resolve(ClassLoader loader, String name, Failure.Mode mode) {
            try {
                return (Class<Type>) loader.loadClass(name);
            } catch (Exception except) {
                switch (mode) {
                    case DEFAULT:
                    case THROW_EXCEPTION:
                        throw Failure.create("[name=" + name + "]", except);

                    case RETURN_NULL:
                        return null;

                    default:
                        throw Failure.create(mode, except);
                }
            }
        }

        /**
         * Check whether given base class types are all assignable to given reference class types. A
         * reference class type of {@code null} is interpreted as any class. Primitive class types
         * are assumed to be assignable to their boxed class types. A {@code null} reference class
         * type list is assignable for any base class type list.
         *
         * @param   base  base class type list.
         * @param   ref   reference class type list.
         *
         * @return  whether given base class types are assignable to given reference class types.
         */
        protected static boolean assignable(Class<?>[] base, Class<?>[] ref) {
            if (ref == null) {
                return true;
            } else if (base.length != ref.length) {
                return false;
            }
            for (int index = 0; index < base.length; index++) {
                if (!Classes.assignable(base[index], ref[index])) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Check whether given base class type is assignable to given reference class type. A
         * reference class type of {@code null} is interpreted as any class. Primitive class types
         * are assumed to be assignable to their boxed class types.
         *
         * @param   base  base class type list.
         * @param   ref   reference class type list.
         *
         * @return  whether given base class type is assignable to given reference class type.
         */
        protected static boolean assignable(Class<?> base, Class<?> ref) {
            if (ref == null) {
                return true;
            } else if (base.equals(ref)) {
                return true;
            } else if (base.isAssignableFrom(ref)) {
                return true;
            } else if (base.isPrimitive()) {
                return BOX_MAP.get(base.getSimpleName()).equals(ref);
            } else if (ref.isPrimitive()) {
                return BOX_MAP.get(ref.getSimpleName()).equals(base);
            }
            return false;
        }

        /**
         * Resolve generic parameter bound class type for given parameterized given class using
         * given parameter index and given bound index.
         *
         * @param   <Type>  result class type.
         * @param   type    parameterized class type.
         * @param   param   index of parameter.
         * @param   bound   index of bound.
         *
         * @return  generic parameter bound class type
         */
        @SuppressWarnings("unchecked")
        protected static <Type> Class<Type> generic(Class<?> type, int param, int bound) {
            if (type.isArray()) {
                return Classes.generic(type.getComponentType(), param, bound);
            }
            TypeVariable<?>[] ptypes = type.getTypeParameters();
            if (ptypes.length == 0) {
                throw Failure.create(Failure.Type.ARGUMENT, //
                    "not parameterized type [" + type.getName() + "]");
            }
            if (param >= ptypes.length) {
                throw Failure.create(Failure.Type.ARGUMENT,
                    "invalid parameter index [type=" + type.getName() //
                    + ", param=" + param + ", bound=" + bound + "]");
            }
            TypeVariable<?> atype = ptypes[param];
            if (bound >= atype.getBounds().length) {
                throw Failure.create(Failure.Type.ARGUMENT,
                    "invalid parameter bound index [type=" + type.getName() //
                    + ", param=" + param + ", bound=" + bound + "]");
            }
            return (Class<Type>) atype.getBounds()[bound];
        }
    }

    /**
     * Object/Constructor specific access methods.
     */
    public static abstract class Objects {

        /**
         * Resolve declared constructor for given declaring class type using default failure
         * handling mode with given constructor argument types.
         *
         * @param   <Type>  declaring type.
         * @param   type    declaring class type.
         * @param   types   constructor argument types.
         *
         * @return  declared constructor.
         */
        public static <Type> Constructor<Type> resolve(Class<Type> type, Class<?>... types) {
            return Objects.resolve(type, Failure.Mode.DEFAULT, types);
        }

        /**
         * Resolve declared constructor for given declaring class type using given failure handling
         * mode with given constructor argument types.
         *
         * @param   <Type>  declaring type.
         * @param   type    declaring class type.
         * @param   mode    failure handling mode.
         * @param   types   constructor argument types.
         *
         * @return  declared constructor.
         */
        public static <Type> Constructor<Type> resolve(Class<Type> type, Failure.Mode mode, Class<?>... types) {
            try {
                return Base.accessible(type.getDeclaredConstructor(types));
            } catch (Exception except) {
                switch (mode) {
                    case DEFAULT:
                    case THROW_EXCEPTION:
                        throw Failure.create(Failure.Helper.message(type, types), except);

                    case RETURN_NULL:
                        return null;

                    default:
                        throw Failure.create(mode, except);
                }
            }
        }

        /**
         * Create new instance via given declared constructor using default failure handling mode
         * while utilizing constructor with given constructor argument types using given constructor
         * arguments.
         *
         * @param   <Type>   result object type.
         * @param   factory  declared constructor.
         * @param   args     constructor arguments.
         *
         * @return  instance of given class type.
         */
        public static <Type> Type create(Constructor<Type> factory, Object... args) {
            return Objects.create(factory, Failure.Mode.DEFAULT, args);
        }

        /**
         * Create new instance via given declared constructor using given failure handling mode
         * while utilizing constructor with given constructor argument types using given constructor
         * arguments.
         *
         * @param   <Type>   result object type.
         * @param   factory  declared constructor.
         * @param   mode     failure handling mode.
         * @param   args     constructor arguments.
         *
         * @return  instance of given class type.
         */
        public static <Type> Type create(Constructor<Type> factory, Failure.Mode mode, Object... args) {
            try {
                return factory.getDeclaringClass().cast(factory.newInstance(args));
            } catch (Exception except) {
                switch (mode) {
                    case DEFAULT:
                    case THROW_EXCEPTION:
                        throw Failure.create(Failure.Helper.message(factory.getDeclaringClass(),
                                factory.getParameterTypes(), args), except);

                    case RETURN_NULL:
                        return null;

                    default:
                        throw Failure.create(mode, except);
                }
            }
        }

        /**
         * Create new instance of given declared class type using default failure handling mode
         * while utilizing constructor with given constructor argument types using given constructor
         * arguments.
         *
         * @param   <Type>  result object type.
         * @param   type    declared class type.
         * @param   types   constructor argument types.
         * @param   args    constructor arguments.
         *
         * @return  instance of given class type.
         */
        public static <Type> Type create(Class<Type> type, Class<?>[] types, Object... args) {
            return Objects.create(type, Failure.Mode.DEFAULT, types, args);
        }

        /**
         * Create new instance of given declared class type using given failure handling mode while
         * utilizing constructor with given constructor argument types using given constructor
         * arguments.
         *
         * @param   <Type>  result object type.
         * @param   type    declared class type.
         * @param   mode    failure handling mode.
         * @param   types   constructor argument types.
         * @param   args    constructor arguments.
         *
         * @return  instance of given class type.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public static <Type> Type create(Class<Type> type, Failure.Mode mode, Class<?>[] types, Object... args) {
            if (type.isEnum()) {
                if (Arrays.equals(types, Base.STRING_TYPES)) {
                    return (Type) Enums.resolve((Class<Enum>) type, (String) args[0], mode);
                }
                throw Failure.create(Failure.Type.SUPPORT, Failure.Helper.message(type, types, args));
            }
            try {
                return Objects.create(Objects.resolve(type, mode, types), mode, args);
            } catch (Exception except) {
                throw Failure.create(Failure.Style.MERGED, Failure.Helper.message(type, types, args), except);
            }
        }

        /**
         * Create new instance of class type with given declared class name using default failure
         * handling mode while utilizing constructor with given constructor argument types using
         * given constructor arguments.
         *
         * @param   <Type>  result object type.
         * @param   name    declared class name.
         * @param   types   constructor argument types.
         * @param   args    constructor arguments.
         *
         * @return  instance of given class type.
         */
        public static <Type> Type create(String name, Class<?>[] types, Object... args) {
            return Objects.create(name, Failure.Mode.DEFAULT, types, args);
        }

        /**
         * Create new instance of class type with given declared class name using given failure
         * handling mode while utilizing constructor with given constructor argument types using
         * given constructor arguments.
         *
         * @param   <Type>  result object type.
         * @param   name    declared class name.
         * @param   mode    failure handling mode.
         * @param   types   constructor argument types.
         * @param   args    constructor arguments.
         *
         * @return  instance of given class type.
         */
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public static <Type> Type create(String name, Failure.Mode mode, Class<?>[] types, Object... args) {
            Class<Type> type = Classes.<Type>resolve(name, mode);
            if (type == null) {
                return null;
            } else if (type.isEnum()) {
                if (Arrays.equals(types, Base.STRING_TYPES)) {
                    return (Type) Enums.resolve((Class<Enum>) type, (String) args[0], mode);
                }
                throw Failure.create(Failure.Type.SUPPORT, Failure.Helper.message(type, types, args));
            }
            return Objects.create(type, mode, types, args);
        }

        /**
         * Inject given injection value on target object into target fields with given field name,
         * the type of the field is determined from by the injection value. If no field name is
         * given ({@code null}), all matching fields are injected. Note: if name as well as value
         * are {@code null} at once, all fields are reset to {@code null}.
         *
         * @param   <Type>  target object type.
         * @param   target  target object.
         * @param   name    field name (may be null).
         * @param   value   injection value (may be null).
         *
         * @return  target object for further injection.
         */
        public static <Type> Type inject(Type target, String name, Object value) {
            for (Field field
                : Fields.resolve(target.getClass(), name, //
                    (value != null) ? value.getClass() : null, Resolve.Type.SUPER)) {
                if ((name == null) || field.getName().equals(name)) {
                    Fields.set(target, field, value);
                }
            }
            return target;
        }

        /**
         * Copy given target object by using a constructor with the given argument list by injecting
         * the target object using given failure handling mode. If the object is an array the array
         * is copied as expected.
         *
         * @param   <Type>  target object type.
         * @param   target  target object.
         * @param   mode    failure handling mode.
         * @param   types   argument type list
         *
         * @return  copied target object.
         */
        @SuppressWarnings("unchecked")
        protected static <Type> Type copy(Type target, Failure.Mode mode, Class<?>... types) {
            if (target == null) {
                throw Failure.create(Failure.Type.ARGUMENT, "target must not be null");
            }
            Class<Type> type = (Class<Type>) target.getClass();
            if (type.isArray()) {
                Object[] array = (Object[]) target;
                return (Type) Arrays.copyOf(array, array.length);
            }
            return Objects.create(type, mode, types, target);
        }

        /**
         * Copy given target object by searching a copy constructor that allows creation of objects
         * based on instances of the same class type (including parent class types) or one of the
         * declared interface types using given failure handling mode.
         *
         * @param   <Type>  target object type.
         * @param   target  target object.
         * @param   mode    failure handling mode.
         *
         * @return  copied target object.
         */
        protected static <Type> Type copy(Type target, Failure.Mode mode) {
            if (target == null) {
                throw Failure.create(Failure.Type.ARGUMENT, "target must not be null");
            }
            Queue<Class<?>> list = new ArrayDeque<Class<?>>();
            list.add(target.getClass());
            while (!list.isEmpty()) {
                Class<?> atype = list.poll();
                Type result = Objects.copy(target, Failure.Mode.RETURN_NULL, atype);
                if (result != null) {
                    return result;
                }
                if ((atype.getSuperclass() != null) && (atype.getSuperclass() != Object.class)) {
                    list.add(atype.getSuperclass());
                }
                list.addAll(Arrays.asList(atype.getInterfaces()));
            }

            switch (mode) {
                case DEFAULT:
                case THROW_EXCEPTION:
                    throw Failure.create(target.getClass());

                case RETURN_NULL:
                    return null;

                default:
                    throw Failure.create(mode, null);
            }
        }

        /**
         * Clone given target object by calling clone on the target object with given failure
         * handling mode.
         *
         * @param   <Type>  target object type.
         * @param   target  target object.
         * @param   mode    failure handling mode.
         *
         * @return  cloned target object.
         */
        protected static <Type> Type clone(Type target, Failure.Mode mode) {
            if (target == null) {
                throw Failure.create(Failure.Type.ARGUMENT, "target must not be null");
            } else if (target instanceof Cloneable) {
                return Methods.invoke(target, "clone", Base.EMPTY_TYPES, Base.EMPTY_ARGS);
            }
            switch (mode) {
                case DEFAULT:
                case THROW_EXCEPTION:
                    throw Failure.create(target.getClass());

                case RETURN_NULL:
                    return null;

                default:
                    throw Failure.create(mode, null);
            }
        }
    }

    /**
     * Enumeration access helper that allows to create, insert, and delete enumeration values on
     * demand. Note: This implementation is based on some internals that might change with new Java
     * versions.
     */
    public static abstract class Enums {

        /**
         * Resolve enumeration value for given enumeration class type and enumeration value name
         * using given failure handling mode. If the given class type is no enumeration class type,
         * an not supported failure is thrown.
         *
         * @param   <Type>  enumeration type.
         * @param   type    enumeration class type.
         * @param   name    enumeration value name.
         * @param   mode    failure handling mode.
         *
         * @return  enumeration value.
         */
        protected static <Type extends Enum<Type>> Type resolve(Class<Type> type, String name, Failure.Mode mode) {
            try {
                Type value = Helper.resolve(Fields.<Type[]>get(type, Helper.resolve(type)), name);
                if (value != null) {
                    return value;
                }
            } catch (Exception except) {
                switch (mode) {
                    case DEFAULT:
                    case THROW_EXCEPTION:
                        throw Failure.create(Failure.Helper.message(type, name), except);

                    case RETURN_NULL:
                        return null;

                    default:
                        throw Failure.create(mode, except);
                }
            }
            switch (mode) {
                case DEFAULT:
                case THROW_EXCEPTION:
                    throw Failure.create(Failure.Type.ARGUMENT, "unknown enum value [" + name + "]");

                case RETURN_NULL:
                    return null;

                default:
                    throw Failure.create(mode, null);
            }
        }

        /**
         * Create enumeration value with given enumeration class type, given enumeration value name,
         * enumeration ordinal number, constructor argument types, and constructor arguments.
         *
         * @param   <Type>   enumeration type.
         * @param   type     enumeration class type.
         * @param   name     enumeration value name.
         * @param   ordinal  enumeration ordinal number.
         * @param   types    constructor argument types.
         * @param   args     constructor arguments.
         *
         * @return  enumeration value.
         */
        public static <Type extends Enum<Type>> Type create(Class<Type> type, String name, int ordinal,
                Class<?>[] types, Object... args) {
            Constructor<?> factory = Objects.resolve(type,
                    Helper.append(new Class[] { String.class, int.class }, types));
            return Helper.create(factory, Helper.args(name, ordinal, args));
        }

        /**
         * Create and insert enumeration value with given enumeration class type, given enumeration
         * value name, enumeration ordinal number, constructor argument types, and constructor
         * arguments.
         *
         * @param   <Type>   enumeration type.
         * @param   type     enumeration class type.
         * @param   name     enumeration value name.
         * @param   ordinal  enumeration ordinal number.
         * @param   types    constructor argument types.
         * @param   args     constructor arguments.
         *
         * @return  enumeration value.
         */
        public static <Type extends Enum<Type>> Type insert(Class<Type> type, String name, int ordinal,
                Class<?>[] types, Object... args) {
            Field field = Helper.resolve(type);
            @SuppressWarnings("unchecked")
            Type[] values = (Type[]) Fields.get(type, field);
            if (ordinal < 0) {
                Type value = Helper.resolve(values, name);
                if (value != null) {
                    return value;
                }
                ordinal = values.length;
            }
            Type value = Enums.create(type, name, ordinal, types, args);
            @SuppressWarnings("unchecked")
            Type[] update = Helper.merge(type, values, value);
            Helper.update(field, update);
            return value;
        }

        /**
         * Update enumeration values of given declared class type using given list of enumeration
         * values. Enumeration values with same ordinal will override previous found enumeration
         * values.
         *
         * @param   <Type>  enumeration type.
         * @param   type    enumeration class type.
         * @param   values  enumeration values.
         *
         * @return  list of updated enumeration values.
         */
        public static <Type extends Enum<Type>> Type[] update(Class<Type> type, Type... values) {
            return Helper.update(Helper.resolve(type), Helper.merge(type, null, values));
        }

        /**
         * Update enumeration values of given declared class type using given list of original
         * enumeration and additional enumeration values. All enumeration values are merged into a
         * common enumeration value list. Enumeration values with same ordinal will override
         * previous found enumeration values.
         *
         * @param   <Type>  enumeration type.
         * @param   type    enumeration class type.
         * @param   values  enumeration values (original, may be null or empty).
         * @param   others  enumeration values (additional, may be null or empty).
         *
         * @return  list of updated enumeration values.
         */
        public static <Type extends Enum<Type>> Type[] update(Class<Type> type, Type[] values, Type... others) {
            return Helper.update(Helper.resolve(type), Helper.merge(type, values, others));
        }

        /**
         * Private enumeration access helper.
         */
        private static final class Helper {

            /**
             * Field name of constructor accesssor.
             */
            private static final String FIELD_CONSTRUCTOR_ACCESSOR = "constructorAccessor";

            /**
             * Method name to acquire constructor accessor.
             */
            private static final String METHOD_ACQUIRE_CONSTRUCTOR_ACCESSOR = "acquireConstructorAccessor";

            /**
             * Method name to acquire new instance form constructor accessor.
             */
            private static final String METHOD_CONSTRUCTOR_ACCESSOR_NEW_INSTANCE = "newInstance";

            /**
             * Method name to acquire field accessor.
             */
            private static final String METHOD_ACQUIRE_FIELD_ACCESSOR = "acquireFieldAccessor";

            /**
             * Method name to update enumeration values field.
             */
            private static final String METHOD_FIELD_ACCESSOR_SET = "set";

            /**
             * Resolve enumeration values field for given enumeration class type.
             *
             * @param   type  enumeration class type.
             *
             * @return  enumeration values field.
             */
            protected static Field resolve(Class<?> type) {
                Field[] fields = type.getDeclaredFields();
                for (Field field : fields) {
                    if (field.getName().endsWith("$VALUES")) {
                        Fields.modify(Base.accessible(field), field.getModifiers() & ~Modifier.FINAL);
                        return field;
                    }
                }
                throw Failure.create(Failure.Type.ARGUMENT, "illegal enum type [" + type.getName() + "]");
            }

            /**
             * Resolve enumeration value with given enumeration value name from given enumeration
             * values array.
             *
             * @param   <Type>  enumeration type.
             * @param   values  enumeration values array.
             * @param   name    enumeration value name.
             *
             * @return  enumeration value.
             */
            protected static <Type extends Enum<Type>> Type resolve(Type[] values, String name) {
                for (Type value : values) {
                    if (value.name().equals(name)) {
                        return value;
                    }
                }
                return null;
            }

            /**
             * Create enumeration value using given enumeration constructor instance and given
             * enumeration constructor arguments.
             *
             * @param   <Type>   enumeration type.
             * @param   factory  enumeration constructor instance.
             * @param   args     enumeration constructor arguments.
             *
             * @return  enumeration value.
             */
            protected static <Type extends Enum<Type>> Type create(Constructor<?> factory, Object[] args) {
                Methods.invoke(factory, Helper.METHOD_ACQUIRE_CONSTRUCTOR_ACCESSOR, null);
                Object target = Fields.<Object>get(factory, Helper.FIELD_CONSTRUCTOR_ACCESSOR);
                return Methods.invoke(target, Helper.METHOD_CONSTRUCTOR_ACCESSOR_NEW_INSTANCE,
                        new Class[] { Object[].class }, new Object[] { args });
            }

            /**
             * Update enumeration values field with given enumerations values array and return
             * enumeration values array.
             *
             * @param   <Type>  enumeration type.
             * @param   field   enumeration values field.
             * @param   values  enumeration values array.
             *
             * @return  enumeration values array.
             */
            protected static <Type extends Enum<Type>> Type[] update(Field field, Type... values) {
                Object target = Methods.invoke(field, METHOD_ACQUIRE_FIELD_ACCESSOR, //
                        new Class[] { boolean.class }, new Object[] { false });
                Methods.invoke(target, METHOD_FIELD_ACCESSOR_SET, //
                    new Class[] { Object.class, Object.class }, new Object[] { null, values });
                return values.clone();
            }

            /**
             * Merge given enumeration values of given enumeration class type into a single ordered
             * list of enumeration values. If some enumeration values have the same ordinal number,
             * the last enumeration values overrides the previous instances.
             *
             * @param   <Type>  enumeration type.
             * @param   type    enumeration class type.
             * @param   values  first list of enumeration values.
             * @param   others  second list of enumeration values.
             *
             * @return  merged list of enumeration values.
             */
            @SuppressWarnings("unchecked")
            protected static <Type extends Enum<Type>> Type[] merge(Class<Type> type, Type[] values, Type... others) {
                if ((others != null) && (others.length > 0)) {
                    if ((values != null) && (values.length > 0)) {
                        values = Helper.order(type, values);
                        return Helper.insert(values, others);
                    }
                    return Helper.order(type, others);
                } else if ((values != null) && (values.length > 0)) {
                    return Helper.order(type, values);
                }
                return (Type[]) Array.newInstance(type, 0);
            }

            /**
             * Create and ordered list of enumeration values. If some enumeration values have the
             * same ordinal number, the last enumeration values overrides the previous instances.
             *
             * @param   <Type>  enumeration type.
             * @param   type    enumeration class type.
             * @param   values  list of enumeration values.
             *
             * @return  ordered list of enumeration values.
             */
            protected static <Type extends Enum<Type>> Type[] order(Class<Type> type, Type... values) {
                int max = -1;
                boolean ordered = true;
                for (int index = 0; index < values.length; index++) {
                    Type value = values[index];
                    int ordinal = value.ordinal();
                    if (index == ordinal) {
                        continue;
                    } else if (ordinal > max) {
                        max = ordinal;
                    }
                    ordered = false;
                }
                if (ordered) {
                    return values;
                }
                @SuppressWarnings("unchecked")
                Type[] order = (Type[]) Array.newInstance(type, max + 1);
                for (Type value : values) {
                    order[value.ordinal()] = value;
                }
                return order;
            }

            /**
             * Insert given enumeration values into ordered list of enumeration values overriding
             * previous enumeration values for same ordinal numbers.
             *
             * @param   <Type>  enumeration type.
             * @param   values  ordered list of enumeration values.
             * @param   others  list of enumeration values to insert.
             *
             * @return  combined list of enumeration values.
             */
            protected static <Type extends Enum<Type>> Type[] insert(Type[] values, Type... others) {
                int max = values.length - 1;
                for (Type other : others) {
                    if (other.ordinal() > max) {
                        max = other.ordinal();
                    }
                }
                if (max >= values.length) {
                    values = Arrays.copyOf(values, max + 1);
                }
                for (Type other : others) {
                    values[other.ordinal()] = other;
                }
                return values;
            }

            /**
             * Append given second array values to given first array values by creating a new
             * extended array. If the second array is {@code null} or empty, the first array is
             * returned without copying.
             *
             * @param   <Type>  array value type.
             * @param   values  first array values.
             * @param   others  second array values (may be null or empty).
             *
             * @return  merged array values.
             */
            protected static <Type> Type[] append(Type[] values, Type... others) {
                if ((others != null) && (others.length > 0)) {
                    values = Arrays.copyOf(values, values.length + others.length);
                    System.arraycopy(others, 0, values, values.length - others.length, others.length);
                }
                return values;
            }

            /**
             * Create enumeration constructor arguments using given enumeration value name,
             * enumeration value ordinal, and additional constructor arguments.
             *
             * @param   name     enumeration value name.
             * @param   ordinal  enumeration ordinal number.
             * @param   args     additional constructor arguments.
             *
             * @return  enumeration constructor arguments.
             */
            protected static Object[] args(String name, int ordinal, Object... args) {
                Object[] array = new Object[args.length + 2];
                System.arraycopy(args, 0, array, 2, args.length);
                array[1] = Integer.valueOf(ordinal);
                array[0] = name;
                return array;
            }
        }
    }

    /**
     * Bean access helper.
     */
    public static abstract class Beans {

        /**
         * Bean property resolution mode (used for field, getter, and setter resolution).
         */
        public static enum Mode {

            /**
             * Resolution mode for strict named and strict typed property.
             */
            KNOWN,

            /**
             * Auto resolution for strict named and lazy typed property.
             */
            NAMED,

            /**
             * Resolution mode for lazy named but strict typed property.
             */
            TYPED,

            /**
             * Auto resolution for lazy named and lazy typed property.
             */
            AUTO,
        }

        /**
         * Read target property value from given target object using given target property name.
         *
         * @param   <Type>  property value type.
         * @param   target  target object value.
         * @param   name    target property name.
         *
         * @return  property value.
         */
        protected static <Type> Type get(Object target, String name) {
            if (target == null) {
                throw Failure.create(Failure.Type.ARGUMENT, "target must not be null");
            } else if ((name == null) || name.isEmpty()) {
                throw Failure.create(Failure.Type.ARGUMENT, "name must not be null or empty [" + name + "]");
            }
            Context<?> context = Context.create(name);
            Method method = Beans.getter(target.getClass(), context.getType(), context.getName(), context.getMode());
            if (method != null) {
                return Methods.invoke(target, method, Base.EMPTY_ARGS);
            }
            Field field = Beans.field(target.getClass(), context.getType(), context.getName(), context.getMode());
            if (field != null) {
                return Fields.get(target, field);
            }
            throw Failure.create(Failure.Type.ARGUMENT,
                "invalid property " + Failure.Helper.message(context.getType(), context.getName()));
        }

        /**
         * Read target property value identified by given target property name from given target
         * object value. The target object value may be an array (accessible by {@link Array}),
         * {@link List}, {@link Set}, {@link Queue}, {@link Map} or any kind of object containing
         * properties. Elements are addressed either by index (arrays, list, sets) or by name
         * (objects, maps - as long as keys of maps can created using via string). It is allowed to
         * define multiple property names as dot ('.') separated list to access nested containers.
         *
         * @param   <Type>  property value type.
         * @param   target  target object value.
         * @param   name    target property name.
         *
         * @return  property value.
         */
        @SuppressWarnings("unchecked")
        public static <Type> Type read(Object target, String name) {
            if (target == null) {
                throw Failure.create(Failure.Type.ARGUMENT, "target must not be null");
            } else if ((name == null) || name.isEmpty()) {
                throw Failure.create(Failure.Type.ARGUMENT, "name must not be null or empty [" + name + "]");
            }
            try {
                Object actual = target;
                Matcher matcher = Base.PATH.matcher(name);
                while (matcher.find()) {
                    actual = Helper.read(actual, matcher.group(Base.PATH_BEAN));
                    if (matcher.group(Base.PATH_LIST) == null) {
                        continue;
                    }
                    for (String sname : Base.LIST.split(matcher.group(Base.PATH_LIST))) {
                        if (!sname.isEmpty()) {
                            actual = Helper.read(actual, sname);
                        }
                    }
                }
                return (Type) actual;
            } catch (RuntimeException except) {
                throw Failure.create(Failure.Helper.message(target, name), except);
            }
        }

        /**
         * Write target property value for given target object using given target property name and
         * return previous property value.
         *
         * @param   <Type>  property value type.
         * @param   target  target object value.
         * @param   name    target property name.
         * @param   value   target property value.
         *
         * @return  previous field value.
         */
        protected static <Type> Type set(Object target, String name, Type value) {
            if (target == null) {
                throw Failure.create(Failure.Type.ARGUMENT, "target must not be null");
            } else if ((name == null) || name.isEmpty()) {
                throw Failure.create(Failure.Type.ARGUMENT, "name must not be null or empty [" + name + "]");
            }
            Context<?> context = Context.create(name, (value != null) ? value.getClass() : null);
            Method method = Beans.getter(target.getClass(), context.getType(), context.getName(), context.getMode());
            if (method != null) {
                Type before = Methods.invoke(target, method, Base.EMPTY_ARGS);
                method = Beans.setter(target.getClass(), context.getType(), context.getName(), context.getMode());
                if (method != null) {
                    Methods.invoke(target, method, new Object[] { value });
                    return before;
                }
            }
            Field field = Beans.field(target.getClass(), context.getType(), context.getName(), context.getMode());
            if (field != null) {
                return Fields.set(target, field, value);
            }
            throw Failure.create(Failure.Type.ARGUMENT,
                "invalid property " + Failure.Helper.message(context.getType(), context.getName()));
        }

        /**
         * Write given target property value to property identified by given target property name on
         * given target object value and return previous property value. The target object value may
         * be an array (accessible by {@link Array}), {@link List}, {@link Set}, {@link Queue},
         * {@link Map} or any kind of object containing properties. Elements are addressed either by
         * index (arrays, list, sets) or by name (objects, maps - as long as keys of maps can
         * created using via string). It is allowed to define multiple property names as dot ('.')
         * separated list to access nested containers.
         *
         * @param   <Type>  property value type.
         * @param   target  target object value.
         * @param   name    target property name.
         * @param   value   target property value.
         *
         * @return  previous property value.
         */
        public static <Type> Type write(Object target, String name, Type value) {
            if (target == null) {
                throw Failure.create(Failure.Type.ARGUMENT, "target must not be null");
            } else if ((name == null) || name.isEmpty()) {
                throw Failure.create(Failure.Type.ARGUMENT, "name must not be null or empty [" + name + "]");
            }
            try {
                Object actual = target;
                Matcher matcher = Base.PATH.matcher(name);
                while (matcher.find() && !matcher.hitEnd()) {
                    actual = Helper.read(actual, matcher.group(Base.PATH_BEAN));
                    if (matcher.group(Base.PATH_LIST) == null) {
                        continue;
                    }
                    for (String sname : Base.LIST.split(matcher.group(Base.PATH_LIST))) {
                        if (!sname.isEmpty()) {
                            actual = AccessHelper.Beans.read(actual, sname);
                        }
                    }
                }
                if (matcher.group(Base.PATH_LIST) == null) {
                    return Helper.write(actual, matcher.group(Base.PATH_BEAN), value);
                }
                actual = Helper.read(actual, matcher.group(Base.PATH_BEAN));
                List<String> snames = Arrays.asList(Base.LIST.split(matcher.group(Base.PATH_LIST)));
                for (String sname : snames.subList(0, snames.size() - 1)) {
                    if (!sname.isEmpty()) {
                        actual = Helper.read(actual, sname);
                    }
                }
                return Helper.write(actual, snames.get(snames.size() - 1), value);
            } catch (RuntimeException except) {
                throw Failure.create(Failure.Helper.message(target, name, value), except);
            }
        }

        /**
         * Resolve field for given declaring class type, property class type, property field name,
         * and property resolution mode.
         *
         * @param   owner  declaring class type.
         * @param   type   property class type (may be {@code null}).
         * @param   name   property field name.
         * @param   mode   bean property resolution mode.
         *
         * @return  property field.
         */
        protected static Field field(Class<?> owner, Class<?> type, String name, Mode mode) {
            Field field = Fields.resolve(owner, name, Failure.Mode.RETURN_NULL);
            switch (mode) {
                case KNOWN:
                case TYPED:
                    if (type == null) {
                        throw Failure.create(Failure.Type.ARGUMENT, "type must not be null");
                    }
                    return ((field != null) && field.getType().equals(type)) ? field : null;

                case NAMED:
                case AUTO:
                    if (field == null) {
                        return null;
                    } else if ((type == null) || Classes.assignable(field.getType(), type)) {
                        return field;
                    }
                    return null;

                default:
                    throw Failure.create(Failure.Type.SUPPORT, "mode not supported [" + mode + "]");
            }
        }

        /**
         * Resolve getter method for given declaring class type, property class type, and property
         * name using given property resolution mode.
         *
         * @param   owner  declaring class type.
         * @param   type   property class type (may be {@code null}).
         * @param   name   property name.
         * @param   mode   bean property resolution mode.
         *
         * @return  getter method.
         */
        protected static Method getter(Class<?> owner, Class<?> type, String name, Mode mode) {
            switch (mode) {
                case KNOWN: {
                    if (type == null) {
                        throw Failure.create(Failure.Type.ARGUMENT, "type must not be null");
                    }
                    Method method = Methods.resolve(owner, name, Failure.Mode.RETURN_NULL);
                    return ((method != null) && method.getReturnType().equals(type)) ? method : null;
                }

                case NAMED: {
                    Method method = Methods.resolve(owner, name, Failure.Mode.RETURN_NULL);
                    return ((method != null) && Classes.assignable(method.getReturnType(), type)) ? method : null;
                }

                case TYPED:
                    if (type == null) {
                        throw Failure.create(Failure.Type.ARGUMENT, "type must not be null");
                    }
                    for (String fname : Beans.getter(type, name)) {
                        Method method = Methods.resolve(owner, fname, Failure.Mode.RETURN_NULL);
                        if ((method != null) && method.getReturnType().equals(type)) {
                            return method;
                        }
                    }
                    return null;

                case AUTO:
                    for (String fname : Beans.getter(type, name)) {
                        Method method = Methods.resolve(owner, fname, Failure.Mode.RETURN_NULL);
                        if ((method != null) && Classes.assignable(method.getReturnType(), type)) {
                            return method;
                        }
                    }
                    return null;

                default:
                    throw Failure.create(Failure.Type.SUPPORT, "mode not supported [" + mode + "]");
            }
        }

        /**
         * Create list of accepted getter names for given property name and given property class
         * type.
         *
         * @param   type  property class type.
         * @param   name  property name.
         *
         * @return  list of accepted getter names.
         */
        private static String[] getter(Class<?> type, String name) {
            String camel = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            if ((type == null) || (type == boolean.class) || (type == Boolean.class)) {
                return new String[] { name, "get" + camel, "is" + camel, "has" + camel };
            }
            return new String[] { name, "get" + camel };
        }

        /**
         * Resolve setter method for given declaring class type, property class type, and property
         * name using given property resolution mode.
         *
         * @param   owner  declaring class type.
         * @param   type   property class type.
         * @param   name   property name.
         * @param   mode   property resolution mode.
         *
         * @return  setter method.
         */
        protected static Method setter(Class<?> owner, Class<?> type, String name, Mode mode) {
            Class<?>[] types = (type != null) ? new Class<?>[] { type } : Base.ANY_TYPES;
            switch (mode) {
                case KNOWN:
                    if (type == null) {
                        throw Failure.create(Failure.Type.ARGUMENT, "type must not be null");
                    }
                    return Methods.resolve(owner, name, Failure.Mode.RETURN_NULL, types);

                case NAMED:
                    for (Method method : Methods.resolve(owner, name, types, void.class)) {
                        return method;
                    }
                    return null;

                case TYPED:
                    if (type == null) {
                        throw Failure.create(Failure.Type.ARGUMENT, "type must not be null");
                    }
                    for (String fname : Beans.setter(type, name)) {
                        Method method = Methods.resolve(owner, fname, Failure.Mode.RETURN_NULL, types);
                        if (method != null) {
                            return method;
                        }
                    }
                    return null;

                case AUTO:
                    for (String fname : Beans.setter(type, name)) {
                        for (Method method : Methods.resolve(owner, fname, types, void.class)) {
                            return method;
                        }
                    }
                    return null;

                default:
                    throw Failure.create(Failure.Type.SUPPORT, "mode not supported [" + mode + "]");
            }
        }

        /**
         * Create list of accepted setter names for given property name and given property class
         * type.
         *
         * @param   type  property class type.
         * @param   name  property name.
         *
         * @return  list of accepted setter names.
         */
        private static String[] setter(Class<?> type, String name) {
            String camel = Character.toUpperCase(name.charAt(0)) + name.substring(1);
            if ((type == null) || (type == boolean.class) || (type == Boolean.class)) {
                return new String[] { name, "set" + camel, "setIs" + camel, "setHas" + camel };
            }
            return new String[] { name, "set" + camel };
        }

        /**
         * Resolve property class type of given owner class type using given property name.
         *
         * @param   type  owner class type.
         * @param   name  property name.
         *
         * @return  property class type.
         */
        protected static Class<?> type(Class<?> type, String name) {
            try {
                if ((name != null) && !name.isEmpty()) {
                    return Helper.type(type, Base.LIST.split(name));
                }
                return type;
            } catch (RuntimeException except) {
                throw Failure.create(Failure.Helper.message(type, name), except);
            }
        }

        /**
         * Determine property class type using declared property field, declared getter method, and
         * declared setter method. If neither property field, nor declared getter method, nor
         * declared setter is available, null is returned.
         *
         * @param   <Type>  property type.
         * @param   field   declared property field.
         * @param   getter  declared getter method.
         * @param   setter  declared setter method.
         *
         * @return  property class type.
         */
        @SuppressWarnings("unchecked")
        protected static <Type> Class<Type> type(Field field, Method getter, Method setter) {
            if (field != null) {
                return (Class<Type>) field.getType();
            } else if (getter != null) {
                return (Class<Type>) getter.getReturnType();
            } else if (setter != null) {
                return (Class<Type>) setter.getParameterTypes()[0];
            }
            return null;
        }

        /**
         * Check consistency of property class type, declared property field, declared getter
         * method, and declared setter method and return bean property class type. If the bean
         * property class type is {@code null}, it is determined from property field, declared
         * getter method, or declared setter method.
         *
         * @param   <Type>  property type.
         * @param   type    bean property class type (may be null).
         * @param   field   declared property field.
         * @param   getter  declared getter method.
         * @param   setter  declared setter method.
         *
         * @return  property class type.
         */
        protected static <Type> Class<Type> type(Class<Type> type, Field field, Method getter, Method setter) {
            if (type == null) {
                type = Beans.type(field, getter, setter);
            }
            if ((field != null) && !Classes.assignable(type, field.getType()) /*(type != field.getType())*/) {
                throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS,
                    "incompatible types [type=" + type.getName() + ", field=" + field + "]");
            }
            if (getter != null) {
                if (getter.getParameterTypes().length != 0) {
                    throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS,
                        "invalid getter [" + getter + "]");
                } else if (!Classes.assignable(type, getter.getReturnType())) {
                    throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS,
                        "incompatible types [type=" + type.getName() + ", getter=" + getter + "]");
                }
            }
            if (setter != null) {
                if (setter.getParameterTypes().length != 1) {
                    throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS,
                        "invalid setter [" + setter + "]");
                } else if (!Classes.assignable(setter.getParameterTypes()[0], type)) {
                    throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS,
                        "incompatible types [type=" + type.getName() + ", setter=" + setter + "]");
                }
            }
            return type;
        }

        /**
         * Resolve property name from given getter/setter method.
         *
         * @param   method  getter/setter method.
         *
         * @return  property name.
         */
        protected static String name(Method method) {
            String name = method.getName();
            switch (method.getParameterTypes().length) {
                case 0: {
                    Class<?> type = method.getReturnType();
                    if (Beans.check(type, name, "is", true)) {
                        return Character.toLowerCase(name.charAt(2)) + name.substring(3);
                    } else if (Beans.check(type, name, "has", true)) {
                        return Character.toLowerCase(name.charAt(3)) + name.substring(4);
                    } else if (Beans.check(type, name, "get", false)) {
                        return Character.toLowerCase(name.charAt(3)) + name.substring(4);
                    }
                    return name;
                }

                case 1:
                    Class<?> type = method.getParameterTypes()[0];
                    if (Beans.check(type, name, "setIs", true)) {
                        return Character.toLowerCase(name.charAt(5)) + name.substring(6);
                    } else if (Beans.check(type, name, "setHas", true)) {
                        return Character.toLowerCase(name.charAt(6)) + name.substring(7);
                    } else if (Beans.check(type, name, "set", false)) {
                        return Character.toLowerCase(name.charAt(3)) + name.substring(4);
                    }
                    return name;

                default:
                    throw Failure.create(AccessHelper.Failure.Type.ARGUMENT,
                        "method is no getter/setter [" + method.getName() + "]");
            }
        }

        /**
         * Check whether given bean property prefix is present and valid for given distinct property
         * name and property class type. If the bean property prefix should only be valid for
         * boolean class types, the bool flag enables a check on the given property class type.
         *
         * @param   type    property class type.
         * @param   name    distinct property name.
         * @param   prefix  bean property prefix.
         * @param   bool    whether only allowed for boolean types.
         *
         * @return  given bean property prefix is present and valid.
         */
        private static boolean check(Class<?> type, String name, String prefix, boolean bool) {
            return (!bool || (type == boolean.class) || (type == Boolean.class)) && name.startsWith(prefix)
                && (name.length() > prefix.length()) && Character.isUpperCase(name.charAt(prefix.length()));
        }

        /**
         * Bean context providing bean context name as well as bean context class type.
         *
         * @param  <Type>  bean context type.
         */
        private static final class Context<Type> {

            /**
             * Bean property class type.
             */
            private final Class<Type> type;

            /**
             * Bean property resolution mode.
             */
            private final Mode mode;

            /**
             * Bean property name.
             */
            private final String name;

            /**
             * Create bean context with given bean property class type and bean property name.
             *
             * @param  type  bean property class type.
             * @param  mode  bean property resolution mode.
             * @param  name  bean property name.
             */
            private Context(Class<Type> type, Mode mode, String name) {
                this.type = type;
                this.mode = mode;
                this.name = name;
            }

            /**
             * Create bean context with given bean property name and default bean context class
             * type. The bean property name is parsed for a included bean context class name and
             * split to bean context class type and bean context name. Otherwise, a bean context
             * with bean property name and default bean context class type is returned.
             *
             * @param   <Type>  property value type.
             * @param   name    bean property name.
             * @param   type    default bean property class type.
             *
             * @return  bean context.
             */
            public static <Type> Context<Type> create(String name, Class<Type> type) {
                String[] names = Base.TYPE.split(name);
                if (names.length == 2) {
                    @SuppressWarnings("unchecked")
                    Class<Type> xtype = (Class<Type>) Classes.find(names[0]);
                    return new Context<Type>(xtype, Mode.TYPED, names[1]);
                }
                return new Context<Type>(type, Mode.AUTO, name);
            }

            /**
             * Create bean context with given bean property name. The bean property name is parsed
             * for a included bean context class name and split to bean context class type and bean
             * context name. Otherwise, a bean context with bean property name and {@code null} bean
             * context class type is returned.
             *
             * @param   <Type>  property value type.
             * @param   name    bean property name.
             *
             * @return  bean context.
             */
            public static <Type> Context<Type> create(String name) {
                return Context.create(name, null);
            }

            /**
             * Return decoded index value from bean context property name.
             *
             * @return  decoded index value from bean context property name.
             */
            public int getIndex() {
                return Integer.valueOf(Long.decode(this.name).intValue());
            }

            /**
             * Return decoded long value from bean context property name.
             *
             * @return  decoded long value from bean context property name.
             */
            public long getDecode() {
                return Long.decode(this.name).longValue();
            }

            /**
             * Return bean property class type.
             *
             * @return  bean property class type.
             */
            public Class<Type> getType() {
                return this.type;
            }

            /**
             * Return bean property resolution mode.
             *
             * @return  bean property resolution mode.
             */
            public Mode getMode() {
                return this.mode;
            }

            /**
             * Return bean property name.
             *
             * @return  bean property name.
             */
            public String getName() {
                return this.name;
            }
        }

        /**
         * Private bean access helper.
         */
        private static abstract class Helper {

            /**
             * Resolve property class type of given owner class type using given list of property
             * names.
             *
             * @param   type   owner class type.
             * @param   names  list of property names.
             *
             * @return  property class type.
             */
            protected static Class<?> type(Class<?> type, String... names) {
                for (String name : names) {
                    if (!name.isEmpty()) {
                        type = Helper.type(type, name);
                    }
                }
                return type;
            }

            /**
             * Resolve property class type of given owner class type using given property name.
             *
             * @param   type  owner class type.
             * @param   name  property name.
             *
             * @return  property class type.
             */
            private static Class<?> type(Class<?> type, String name) {
                if (type.isArray()) {
                    return type.getComponentType();
                }
                Context<?> context = Context.create(name);
                if (context.getType() != null) {
                    return context.getType();
                        /* Unnecessary until reification is available. */
                        /*
                        } else if (Collection.class.isAssignableFrom(type)) {
                            type = Classes.generic(type, 0, 0);
                            if (type != Object.class) {
                                return type;
                            }
                        } else if (Map.class.isAssignableFrom(type)) {
                            type = Classes.generic(type, 1, 0);
                            if (type != Object.class) {
                                return type;
                            }
                         */
                }
                Field field = Beans.field(type, null, context.getName(), Mode.AUTO);
                if (field != null) {
                    return field.getType();
                }
                Method method = Beans.getter(type, null, context.getName(), Mode.AUTO);
                if (method != null) {
                    return method.getReturnType();
                        /* Unnecessary until reification is available. */
                        /*
                           } else if (Iterable.class.isAssignableFrom(type)) {
                               type = Classes.generic(type, 0, 0);
                               if (type != Object.class) {
                                   return type;
                               }
                         */
                }
                throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ARGUMENT, //
                    "property type failure [type=" + type.getName() + ", name=" + name + "]");
            }

            /**
             * Resolve key class type from target map object. First the generic parameter of the map
             * is checked whether it provides a sufficient none-object class type. Otherwise, the
             * target is checked whether it is not empty and the class type of the first available
             * key is used. If no key is available string is used as default.
             *
             * @param   target  target map object.
             *
             * @return  map key class type.
             */
            private static Class<?> type(Map<?, ?> target) {
                /* Unnecessary until reification is available. */
                /*
                if (target != null) {
                    Class<?> type = Classes.generic(target.getClass(), 0, 0);
                    if (type != Object.class) {
                        return type;
                    }
                }
                 */
                Set<?> set = target.keySet();
                if (!set.isEmpty()) {
                    return set.iterator().next().getClass();
                }
                return String.class;
            }

            /**
             * Read target property value identified by given target property name from given target
             * object value. The target object value may be an array (accessible by {@link Array}),
             * {@link List}, {@link Set}, {@link Queue}, {@link Map} or any kind of object
             * containing properties. Elements are addressed either by index (arrays, list, sets) or
             * by name (objects, maps - as long as keys of maps can created using via string).
             *
             * @param   <Type>  property value type.
             * @param   target  target object value.
             * @param   name    target property name.
             *
             * @return  property value.
             */
            @SuppressWarnings("unchecked")
            protected static <Type> Type read(Object target, String name) {
                if (target == null) {
                    throw Failure.create(Failure.Type.ARGUMENT, "target must not be null");
                } else if (target.getClass().isArray()) {
                    return Helper.read((Object[]) target, name);
                } else if (target instanceof List<?>) {
                    return Helper.read((List<Type>) target, name);
                } else if (target instanceof Map<?, ?>) {
                    return Helper.read((Map<?, Type>) target, name);
                } else if (target instanceof Collection<?>) {
                    return Helper.read((Collection<Type>) target, name);
                } else if ((target instanceof Iterable<?>) && Base.INDEX.matcher(name).matches()) {
                    int index = Integer.valueOf(Long.decode(name).intValue());
                    return Helper.access((Iterable<Type>) target, index, false);
                }
                return Beans.get(target, name);
            }

            /**
             * Read target property value identified by given target property name from given target
             * object array.
             *
             * @param   <Type>  property value type.
             * @param   target  target object array.
             * @param   name    target property name.
             *
             * @return  property value.
             */
            @SuppressWarnings("unchecked")
            private static <Type> Type read(Object[] target, String name) {
                int index = Context.create(name).getIndex();
                if (index >= Array.getLength(target)) {
                    throw Failure.create(index, Array.getLength(target));
                }
                return (Type) Array.get(target, index);
            }

            /**
             * Read target property value identified by given target property name from given target
             * object list.
             *
             * @param   <Type>  property value type.
             * @param   target  target object list.
             * @param   name    target property name.
             *
             * @return  property value.
             */
            private static <Type> Type read(List<Type> target, String name) {
                int index = Context.create(name).getIndex();
                if (index < target.size()) {
                    return target.get(index);
                }
                throw Failure.create(index, target.size());
            }

            /**
             * Read target property value identified by given target property name from given target
             * object map.
             *
             * @param   <Type>  property value type.
             * @param   target  target object map.
             * @param   name    target property name.
             *
             * @return  property value.
             */
            private static <Type> Type read(Map<?, Type> target, String name) {
                Context<?> context = Context.create(name, Helper.type(target));
                if (context.getType() == String.class) {
                    return target.get(context.getName());
                } else if (Number.class.isAssignableFrom(context.getType())) {
                    if (context.getType() == Long.class) {
                        return target.get(context.getDecode());
                    } else if (context.getType() == Integer.class) {
                        return target.get(Integer.valueOf((int) context.getDecode()));
                    } else if (context.getType() == Short.class) {
                        return target.get(Short.valueOf((short) context.getDecode()));
                    } else if (context.getType() == Byte.class) {
                        return target.get(Byte.valueOf((byte) context.getDecode()));
                    } else if (context.getType() == Double.class) {
                        return target.get(Double.valueOf(context.getName()));
                    } else if (context.getType() == Float.class) {
                        return target.get(Float.valueOf(context.getName()));
                    } else if (context.getType() == BigInteger.class) {
                        return target.get(new BigInteger(context.getName()));
                    } else if (context.getType() == BigDecimal.class) {
                        return target.get(new BigDecimal(context.getName()));
                    }
                    throw Failure.create(context.getType());
                } else if (context.getType() == Boolean.class) {
                    return target.get(Boolean.valueOf(context.getName()));
                } else if (context.getType() == Character.class) {
                    return target.get(context.getName().charAt(0));
                } else if (context.getType() == Class.class) {
                    return target.get(Classes.find(context.getName()));
                }
                return target.get(Objects.create(context.getType(), //
                            Base.STRING_TYPES, context.getName()));
            }

            /**
             * Read target property value identified by given target property name from given target
             * object collection.
             *
             * @param   <Type>  property value type.
             * @param   target  target object collection.
             * @param   name    target property name.
             *
             * @return  property value.
             */
            private static <Type> Type read(Collection<Type> target, String name) {
                if ("*".equals(name)) {
                    return null;
                } else if (Base.INDEX.matcher(name).matches()) {
                    int index = Integer.valueOf(Long.decode(name).intValue());
                    if (index >= target.size()) {
                        throw Failure.create(index, target.size());
                    }
                    return Helper.access(target, index, false);
                }
                Type before = Helper.before(target, name, null);
                if (target.contains(before)) {
                    return before;
                }
                return null;
            }

            /**
             * Write given target property value to property identified by given target property
             * name on given target object value and return previous property value. The target
             * object value may be an array (accessible by {@link Array}), {@link List}, {@link Set},
             * {@link Queue}, {@link Map} or any kind of object containing properties. Elements are
             * addressed either by index (arrays, list, sets) or by name (objects, maps - as long as
             * keys of maps can created using via string).
             *
             * @param   <Type>  property value type.
             * @param   target  target object value.
             * @param   name    target property name.
             * @param   value   target property value.
             *
             * @return  previous property value.
             */
            @SuppressWarnings("unchecked")
            protected static <Type> Type write(Object target, String name, Type value) {
                if (target == null) {
                    throw Failure.create(Failure.Type.ARGUMENT, "target must not be null");
                } else if (target.getClass().isArray()) {
                    return Helper.write((Object[]) target, name, value);
                } else if (target instanceof List<?>) {
                    return Helper.write((List<Type>) target, name, value);
                } else if (target instanceof Map<?, ?>) {
                    return Helper.write((Map<?, Type>) target, name, value);
                } else if (target instanceof Collection<?>) {
                    return Helper.write((Collection<Type>) target, name, value);
                } else if ((target instanceof Iterable<?>) && Base.INDEX.matcher(name).matches()) {
                    int index = Integer.valueOf(Long.decode(name).intValue());
                    return Helper.access((Iterable<Type>) target, index, true);
                }
                return Beans.set(target, name, value);
            }

            /**
             * Write given target property value to property identified by given target property
             * name on given target object array and return previous property value.
             *
             * @param   <Type>  property value type.
             * @param   target  target object array.
             * @param   name    target property name.
             * @param   value   target property value.
             *
             * @return  previous property value.
             */
            @SuppressWarnings("unchecked")
            private static <Type> Type write(Object[] target, String name, Type value) {
                int index = Context.create(name).getIndex();
                if (index >= Array.getLength(target)) {
                    throw Failure.create(index, Array.getLength(target));
                }
                Type before = (Type) Array.get(target, index);
                Array.set(target, index, value);
                return before;
            }

            /**
             * Write given target property value to property identified by given target property
             * name on given target object list and return previous property value.
             *
             * @param   <Type>  property value type.
             * @param   target  target object list.
             * @param   name    target property name.
             * @param   value   target property value.
             *
             * @return  previous property value.
             */
            private static <Type> Type write(List<Type> target, String name, Type value) {
                if ("*".equals(name)) {
                    target.add(value);
                    return null;
                }
                int index = Context.create(name).getIndex();
                if (index < target.size()) {
                    Type before = target.get(index);
                    target.set(index, value);
                    return before;
                }
                throw Failure.create(index, target.size());
            }

            /**
             * Write given target property value to property identified by given target property
             * name on given target object map and return previous property value.
             *
             * @param   <Type>  property value type.
             * @param   target  target object map.
             * @param   name    target property name.
             * @param   value   target property value.
             *
             * @return  previous property value.
             */
            @SuppressWarnings("unchecked")
            private static <Type> Type write(Map<?, Type> target, String name, Type value) {
                Context<?> context = Context.create(name, Helper.type(target));
                if (context.getType() == String.class) {
                    return ((Map<String, Type>) target).put(context.getName(), value);
                } else if (Number.class.isAssignableFrom(context.getType())) {
                    if (context.getType() == Long.class) {
                        return ((Map<Long, Type>) target).put(context.getDecode(), value);
                    } else if (context.getType() == Integer.class) {
                        return ((Map<Integer, Type>) target).put(Integer.valueOf((int) context.getDecode()), value);
                    } else if (context.getType() == Short.class) {
                        return ((Map<Short, Type>) target).put(Short.valueOf((short) context.getDecode()), value);
                    } else if (context.getType() == Byte.class) {
                        return ((Map<Byte, Type>) target).put(Byte.valueOf((byte) context.getDecode()), value);
                    } else if (context.getType() == Double.class) {
                        return ((Map<Double, Type>) target).put(Double.valueOf(context.getName()), value);
                    } else if (context.getType() == Float.class) {
                        return ((Map<Float, Type>) target).put(Float.valueOf(context.getName()), value);
                    } else if (context.getType() == BigInteger.class) {
                        return ((Map<BigInteger, Type>) target).put(new BigInteger(context.getName()), value);
                    } else if (context.getType() == BigDecimal.class) {
                        return ((Map<BigDecimal, Type>) target).put(new BigDecimal(context.getName()), value);
                    }
                    throw Failure.create(context.getType());
                } else if (context.getType() == Boolean.class) {
                    return ((Map<Boolean, Type>) target).put(Boolean.valueOf(context.getName()), value);
                } else if (context.getType() == Character.class) {
                    return ((Map<Character, Type>) target).put(context.getName().charAt(0), value);
                } else if (context.getType() == Class.class) {
                    return ((Map<Class<?>, Type>) target).put(Classes.find(context.getName()), value);
                }
                return ((Map<Object, Type>) target).put(Objects.create(context.getType(), //
                            Base.STRING_TYPES, context.getName()), value);
            }

            /**
             * Write given target property value to property identified by given target property
             * name on given target object collection and return previous property value.
             *
             * @param   <Type>  property value type.
             * @param   target  target object collection.
             * @param   name    target property name.
             * @param   value   target property value.
             *
             * @return  previous property value.
             */
            private static <Type> Type write(Collection<Type> target, String name, Type value) {
                if ("*".equals(name)) {
                    target.add(value);
                    return null;
                } else if (Base.INDEX.matcher(name).matches()) {
                    int index = Integer.valueOf(Long.decode(name).intValue());
                    if (index >= target.size()) {
                        throw Failure.create(index, target.size());
                    }
                    return Helper.access(target, index, true);
                }
                Type before = Helper.before(target, name, value);
                if (target.remove(before)) {
                    if (value != null) {
                        target.add(value);
                    }
                    return before;
                }
                target.add(value);
                return null;
            }

            /**
             * Resolve previous target property value from target object collection using given
             * target property name and target property value to determine property type.
             *
             * @param   <Type>  property value type.
             * @param   target  target object collection.
             * @param   name    target property name.
             * @param   value   target property value.
             *
             * @return  previous target property value.
             */
            @SuppressWarnings("unchecked")
            private static <Type> Type before(Collection<Type> target, String name, Type value) {
                Class<Type> type = Classes.generic(target.getClass(), 0, 0);
                if (value != null) {
                    type = (Class<Type>) value.getClass();
                } else if (!target.isEmpty()) {
                    type = (Class<Type>) target.iterator().next().getClass();
                }
                Context<Type> context = Context.create(name, type);
                if (context.getType() == Object.class) {
                    throw Failure.create(Failure.Type.ARGUMENT, "could not determine type");
                }
                return Objects.create(context.getType(), Base.STRING_TYPES, context.getName());
            }

            /**
             * Resolve element value at given target index in given iterable target object. If
             * remove flag is given, the element value is remove from target object.
             *
             * @param   <Type>  property value type.
             * @param   target  iterable target object value.
             * @param   index   target index for element value.
             * @param   remove  whether to remove element value at index from target object value.
             *
             * @return  element value.
             */
            private static <Type> Type access(Iterable<Type> target, int index, boolean remove) {
                int count = 0;
                Iterator<Type> iter = target.iterator();
                while (iter.hasNext()) {
                    Type next = iter.next();
                    if (count++ >= index) {
                        if (remove) {
                            iter.remove();
                        }
                        return next;
                    }
                }
                throw Failure.create(index, count);
            }
        }
    }
}
