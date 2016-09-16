package org.jactors.junit.helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Bean access helper.
 */
public abstract class BeanHelper {

    /**
     * Inject given injection value on target object into target fields with given field name. If no
     * field name is given, all matching fields are injected.
     *
     * @param  target  target object.
     * @param  name    field name.
     * @param  value   injection value.
     */
    public static void inject(Object target, String name, Object value) {
        AccessHelper.Objects.inject(target, name, value);
    }

    /**
     * Create bean property definition using given property field.
     *
     * @param   <Type>  property type.
     * @param   field   property field.
     *
     * @return  bean property definition.
     */
    @SuppressWarnings("unchecked")
    public static <Type> Property<Type> create(Field field) {
        return new Property<Type>((Class<Type>) field.getType(), //
                field.getName(), Property.AUTO, Property.AUTO);
    }

    /**
     * Create bean property definition using given property getter/setter method.
     *
     * @param   <Type>  property type.
     * @param   method  property getter/setter method.
     * @param   mode    failure handling mode.
     *
     * @return  bean property definition.
     */
    @SuppressWarnings("unchecked")
    public static <Type> Property<Type> create(Method method, AccessHelper.Failure.Mode mode) {
        switch (method.getParameterTypes().length) {
            case 0:
                return new Property<Type>((Class<Type>) method.getReturnType(), //
                        AccessHelper.Beans.name(method), Property.AUTO, Property.AUTO);

            case 1:
                return new Property<Type>((Class<Type>) method.getParameterTypes()[0], //
                        AccessHelper.Beans.name(method), Property.AUTO, Property.AUTO);

            default:
                switch (mode) {
                    case DEFAULT:
                    case THROW_EXCEPTION:
                        throw AccessHelper.Failure.create(AccessHelper.Failure.Type.METHOD, //
                            "method is no getter/setter [" + method.getName() + "]");

                    case RETURN_NULL:
                        return null;

                    default:
                        throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ARGUMENT, //
                            "mode not supported [" + mode + "]");
                }
        }
    }

    /**
     * Create bean property definition using given property class type and property name.
     *
     * @param   <Type>  property type.
     * @param   type    property class type.
     * @param   name    property name.
     *
     * @return  bean property definition.
     */
    public static <Type> Property<Type> create(Class<Type> type, String name) {
        if ((name == null) || name.isEmpty()) {
            throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS,
                "name must not be null or empty [" + name + "]");
        }
        return new Property<Type>(type, name, Property.AUTO, Property.AUTO);
    }

    /**
     * Create bean property definition using given property class type, property field name,
     * property getter name, and property setter name.
     *
     * @param   <Type>  property type.
     * @param   type    property class type.
     * @param   field   property field name (may be null).
     * @param   getter  property getter name (may be null).
     * @param   setter  property setter name (may be null).
     *
     * @return  bean property definition.
     */
    public static <Type> Property<Type> create(Class<Type> type, //
            String field, String getter, String setter) {
        if (type == null) {
            throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS, "type must not be null");
        } else if (((field == null) || field.isEmpty())
                && ((getter == null) || getter.isEmpty() || (setter == null) || setter.isEmpty())) {
            throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS,
                "incomplete property [field=" + field + ", getter=" + getter + ", setter=" + setter + "]");
        }
        return new Property<Type>(type, field, getter, setter);
    }

    /**
     * Create bean property accessor for given property class type and bean property definition.
     *
     * @param   <Type>    property type.
     * @param   type      property class type.
     * @param   property  property definition.
     *
     * @return  bean property accessor.
     */
    public static <Type> BeanHelper.Accessor<Type> create(Class<?> type, Property<Type> property) {
        try {
            return Accessor.Helper.create(type, property);
        } catch (RuntimeException except) {
            throw AccessHelper.Failure.create("creation failure [owner=" + type.getName() //
                + ", property=" + property + "]", except);
        }
    }

    /**
     * Common property and accessor descriptor.
     *
     * @param  <Fields>   property field type.
     * @param  <Methods>  property method type.
     */
    private static class Descriptor<Fields, Methods> {

        /**
         * Property field name (may be virtual).
         */
        protected final Fields field;

        /**
         * Property getter method name.
         */
        protected final Methods getter;

        /**
         * Property setter method name.
         */
        protected final Methods setter;

        /**
         * Common property and accessor descriptor with given field descriptor, getter descriptor,
         * and setter descriptor.
         *
         * @param  field   field description.
         * @param  getter  getter description.
         * @param  setter  setter description.
         */
        protected Descriptor(Fields field, Methods getter, Methods setter) {
            this.field = field;
            this.getter = getter;
            this.setter = setter;
        }

        /**
         * Return property field descriptor.
         *
         * @return  property field descriptor.
         */
        public Fields field() {
            return this.field;
        }

        /**
         * Return property getter method descriptor.
         *
         * @return  property getter method descriptor.
         */
        public Methods getter() {
            return this.getter;
        }

        /**
         * Return property setter method descriptor.
         *
         * @return  property setter method descriptor.
         */
        public Methods setter() {
            return this.setter;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((this.field == null) ? 0 : this.field.hashCode());
            result = (prime * result) + ((this.getter == null) ? 0 : this.getter.hashCode());
            result = (prime * result) + ((this.setter == null) ? 0 : this.setter.hashCode());
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
            Descriptor<?, ?> other = (Descriptor<?, ?>) obj;
            if (this.field == null) {
                if (other.field != null) {
                    return false;
                }
            } else if (!this.field.equals(other.field)) {
                return false;
            }
            if (this.getter == null) {
                if (other.getter != null) {
                    return false;
                }
            } else if (!this.getter.equals(other.getter)) {
                return false;
            }
            if (this.setter == null) {
                if (other.setter != null) {
                    return false;
                }
            } else if (!this.setter.equals(other.setter)) {
                return false;
            }
            return true;
        }
    }

    /**
     * Property definition.
     *
     * @param  <Type>  property type.
     */
    public static final class Property<Type> extends Descriptor<String, String> {

        /**
         * Constant for auto naming.
         */
        public static final String AUTO = "*";

        /**
         * Property class type definition.
         */
        private final Class<Type> type;

        /**
         * Create property definition for given declaring class type, given property field name,
         * property getter name, and property setter name.
         *
         * @param  type    declaring class type.
         * @param  field   property field name.
         * @param  getter  property getter name.
         * @param  setter  property setter name.
         */
        protected Property(Class<Type> type, String field, String getter, String setter) {
            super(field, getter, setter);
            if (type == null) {
                throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS, "type must not be null");
            }
            this.type = type;
        }

        /**
         * Return property class type definition.
         *
         * @return  property class type definition.
         */
        public Class<Type> type() {
            return this.type;
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            final int prime = 31;
            return (prime * super.hashCode()) + this.type.hashCode();
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            Property<?> other = (Property<?>) obj;
            return (this.type == other.type);
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "Property[type=" + this.type.getName() + ", field=" + this.field //
                + ", setter=" + this.setter + ", getter=" + this.getter + "]";
        }
    }

    /**
     * Bean property accessor.
     *
     * @param  <Type>  property type.
     */
    public static final class Accessor<Type> extends Descriptor<Field, Method> {

        /**
         * Parent bean property accessor;
         */
        private final Accessor<?> parent;

        /**
         * Bean property definition.
         */
        private final Property<Type> property;

        /**
         * Bean sub-property name ({@code null} if not available).
         */
        private final String name;

        /**
         * Create bean property accessor for given parent bean accessor, bean property class type,
         * declared field, declared getter method, declared setter method, and bean sub-property
         * name.
         *
         * @param  parent  parent bean accessor.
         * @param  type    bean property class type.
         * @param  field   declared property field.
         * @param  getter  declared getter method.
         * @param  setter  declared setter method.
         * @param  name    bean sub-property name.
         */
        protected Accessor(Accessor<?> parent, Class<Type> type, //
                Field field, Method getter, Method setter, String name) {
            super(field, getter, setter);
            if (type == null) {
                throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS, "type must not be null");
            }
            AccessHelper.Beans.type(((name == null) || name.isEmpty()) ? type : null, field, getter, setter);
            this.property = Helper.create(type, parent, field, getter, setter, name);
            this.parent = parent;
            this.name = name;
        }

        /**
         * Return property definition.
         *
         * @return  property definition.
         */
        public Property<Type> property() {
            return this.property;
        }

        /**
         * Read property value from given target object.
         *
         * @param   target  target object.
         *
         * @return  property value.
         *
         * @throws  Throwable  whatever exception getter throws.
         */
        public Type get(Object target) throws Throwable {
            try {
                return this.read(target);
            } catch (AccessHelper.Failure failure) {
                throw failure.getTarget(Throwable.class);
            }
        }

        /**
         * Read property value from given target object.
         *
         * @param   target  target object.
         *
         * @return  property value.
         */
        public Type read(Object target) {
            if (this.parent != null) {
                target = this.parent.read(target);
            }
            return this.read(target, this.name);
        }

        /**
         * Read property value from given target object using given bean sub-property name.
         *
         * @param   target  target object.
         * @param   name    bean sub-property name.
         *
         * @return  property value.
         */
        private Type read(Object target, String name) {
            if ((name != null) && !name.isEmpty()) {
                return AccessHelper.Beans.read(this.read(target, null), name);
            } else if (this.getter != null) {
                return AccessHelper.Methods.invoke(target, this.getter);
            } else if (this.field != null) {
                return AccessHelper.Fields.get(target, this.field);
            }
            throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS, //
                "access without getter [field=" + this.field + ", getter=" + this.getter + "]");
        }

        /**
         * Write given property value to given target object and return previous property value.
         *
         * @param   target  target object.
         * @param   value   property value.
         *
         * @return  previous property value.
         *
         * @throws  Throwable  whatever exception setter throws.
         */
        public Type set(Object target, Type value) throws Throwable {
            try {
                return this.write(target, value);
            } catch (AccessHelper.Failure failure) {
                throw failure.getTarget(Throwable.class);
            }
        }

        /**
         * Write given property value to given target object and return previous property value.
         *
         * @param   target  target object.
         * @param   value   property value.
         *
         * @return  previous property value.
         */
        public Type write(Object target, Type value) {
            if (this.parent != null) {
                target = this.parent.read(target);
            }
            return this.write(target, this.name, value);
        }

        /**
         * Write given property value for given target object using given bean sub-property name and
         * return previous property value.
         *
         * @param   target  target object.
         * @param   name    bean sub-property name.
         * @param   value   property value.
         *
         * @return  previous property value.
         */
        private Type write(Object target, String name, Type value) {
            if ((name != null) && !name.isEmpty()) {
                return AccessHelper.Beans.write(this.read(target, null), name, value);
            } else if (this.setter != null) {
                Type before = this.read(target, this.name);
                AccessHelper.Methods.invoke(target, this.setter, value);
                return before;
            } else if (this.field != null) {
                return AccessHelper.Fields.set(target, this.field, value);
            }
            throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS, //
                "access without setter [field=" + this.field + ", setter=" + this.setter + "]");
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = (prime * result) + ((this.parent == null) ? 0 : this.parent.hashCode());
            result = (prime * result) + ((this.name == null) ? 0 : this.name.hashCode());
            return result;
        }

        /**
         * {@inheritDoc}
         */
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (!super.equals(obj)) {
                return false;
            }
            Accessor<?> other = (Accessor<?>) obj;
            if (this.parent == null) {
                if (other.parent != null) {
                    return false;
                }
            } else if (!this.parent.equals(other.parent)) {
                return false;
            }
            if (this.name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!this.name.equals(other.name)) {
                return false;
            }
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public String toString() {
            return "Accessor[property=" + this.property //
                + ", parent=" + ((this.parent != this) ? this.parent : "this") //
                + ", field=" + ((this.field != null) ? this.field.getName() : null) //
                + ", getter=" + ((this.getter != null) ? this.getter : null) //
                + ", setter=" + ((this.setter != null) ? this.setter : null) //
                + ", key=" + this.name + "]";
        }

        /**
         * Property helper.
         */
        protected static abstract class Helper {

            /**
             * Group index of bean path.
             */
            public static final int PATH_BEAN = 1;

            /**
             * Group index of list path.
             */
            public static final int PATH_LIST = 3;

            /**
             * Pattern for matching property path.
             */
            private static final Pattern PATH = Pattern.compile("([^.,\\(\\)\\[\\]]+)"
                    + "((?<!\\\\)\\[(.*?)?(?<!\\\\)\\](?!\\[))*+(\\.|,)?");

            /*
            public static void main(String[] args) {
                for (String name : new String[] { "value", "*", "base.value", "base[x].value[x,y]" }) {
                    int count = 0;
                    Matcher match = PATH.matcher(name);
                    while (match.find()) {
                        for (int index = 0; index < match.groupCount(); index++) {
                            String value = match.group(index);
                            System.out.println("index-" + count + "." + index + ": " + value);
                        }
                        System.out.println("count-" + count + ": " + match.group(PATH_BEAN) + " => "
                            + ((match.group(PATH_LIST) != null) ? match.group(PATH_LIST) : ""));
                        count++;
                    }
                }
            }
             */

            /**
             * Create bean property accessor for given declaring class type and bean property
             * definition.
             *
             * @param   <Type>    property type.
             * @param   type      declaring class type.
             * @param   property  property definition.
             *
             * @return  bean property accessor.
             */
            @edu.umd.cs.findbugs.annotations.SuppressWarnings("NS_DANGEROUS_NON_SHORT_CIRCUIT")
            public static <Type> Accessor<Type> create(Class<?> type, Property<Type> property) {
                Class<?> owner = type;
                Accessor<Type> accessor = null;
                Matcher fmatcher = PATH.matcher((property.field() != null) ? property.field() : "");
                Matcher gmatcher = PATH.matcher((property.getter() != null) ? property.getter() : "");
                Matcher smatcher = PATH.matcher((property.setter() != null) ? property.setter() : "");
                while (fmatcher.find() | gmatcher.find() | smatcher.find()) {
                    accessor = Helper.create(owner, accessor, property, fmatcher, gmatcher, smatcher);
                    owner = AccessHelper.Beans.type(accessor.property.type(), accessor.name);
                }
                return accessor;
            }

            private static <Type> Accessor<Type> create(Class<?> owner, Accessor<?> accessor, Property<Type> property, //
                    Matcher fmatcher, Matcher gmatcher, Matcher smatcher) {
                String name = Helper.name(property, fmatcher, gmatcher, smatcher);
                Class<Type> type = (Helper.last(fmatcher, gmatcher, smatcher)) ? property.type() : null;
                Field field = AccessHelper.Beans.field(owner, type, Helper.name(fmatcher),
                        AccessHelper.Beans.Mode.AUTO);
                Method getter = AccessHelper.Beans.getter(owner, type, Helper.name(gmatcher, fmatcher),
                        AccessHelper.Beans.Mode.AUTO);
                Method setter = AccessHelper.Beans.setter(owner, type, Helper.name(smatcher, fmatcher),
                        AccessHelper.Beans.Mode.AUTO);
                return new Accessor<Type>(accessor,
                        (type != null) ? type : AccessHelper.Beans.<Type>type(field, getter, setter), //
                        field, getter, setter, name);
            }

            private static boolean last(Matcher... matchers) {
                for (Matcher matcher : matchers) {
                    if (!matcher.hitEnd()) {
                        return false;
                    }
                }
                return true;
            }

            private static String name(Matcher... matchers) {
                for (Matcher matcher : matchers) {
                    try {
                        String name = matcher.group(PATH_BEAN);
                        if (!name.isEmpty() && !name.equals(Property.AUTO)) {
                            return name;
                        }
                    } catch (IllegalStateException except) {
                        // ignore and continue search!
                    }
                }
                return null;
            }

            private static <Type> String name(Property<Type> property, Matcher... matchers) {
                String name = Helper.name("", matchers);
                for (Matcher matcher : matchers) {
                    if (!Helper.name(name, matcher).equals(name)) {
                        throw AccessHelper.Failure.create(AccessHelper.Failure.Type.ACCESS,
                            "inconsistent sub-property [name=" + name + ", field=" + Helper.name("", matcher) //
                            + ", property=" + property + "]");
                    }
                }
                return (!name.isEmpty()) ? name : null;
            }

            private static String name(String name, Matcher... matchers) {
                for (Matcher matcher : matchers) {
                    try {
                        String sname = matcher.group(PATH_LIST);
                        if ((sname != null) && !sname.isEmpty()) {
                            return sname;
                        }
                    } catch (IllegalStateException except) {
                        // ignore and continue search!
                    }
                }
                return name;
            }

            /**
             * Create property definition using given property class type, parent property accessor,
             * declared property field, declared getter method, declared setter method, and
             * sub-property name.
             *
             * @param   <Type>  property type.
             * @param   type    property class type.
             * @param   parent  parent property accessor.
             * @param   field   declared property field.
             * @param   getter  declared getter method.
             * @param   setter  declared setter method.
             * @param   name    sub-property name.
             *
             * @return  property definition.
             */
            protected static <Type> Property<Type> create(Class<Type> type, Accessor<?> parent, Field field,
                    Method getter, Method setter, String name) {
                return new Property<Type>(type, //
                        Helper.name(parent, (field != null) ? field.getName() : null, name),
                        Helper.name(parent, (getter != null) ? AccessHelper.Beans.name(getter) : null, name),
                        Helper.name(parent, (setter != null) ? AccessHelper.Beans.name(setter) : null, name));
            }

            private static String name(Accessor<?> parent, String name, String key) {
                if ((parent != null) && (name != null)) {
                    Property<?> property = parent.property();
                    if (property.getter() != null) {
                        return property.getter() + "." + Helper.name(name, key);
                    } else if (property.field() != null) {
                        return property.field() + "." + Helper.name(name, key);
                    }
                }
                return Helper.name(name, key);
            }

            private static String name(String name, String key) {
                if (name != null) {
                    if (key != null) {
                        return name + "[" + key + "]";
                    }
                    return name;
                }
                return null;
            }
        }
    }
}
