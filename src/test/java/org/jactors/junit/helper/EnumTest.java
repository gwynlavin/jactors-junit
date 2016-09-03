package org.jactors.junit.helper;

import org.hamcrest.CoreMatchers;
import org.jactors.junit.Expect;
import org.jactors.junit.theory.EnumTheory;
import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 * Simple enumeration theory test for classes in package.
 */
public class EnumTest extends EnumTheory {

    /**
     * Unknown enum types for testing.
     */
    public static interface Unknown {

        /**
         * Unknown enumeration element name.
         */
        public static final String NAME_UNKNOWN = "UNKNOWN";

        /**
         * Unknown matcher.
         */
        public static final Expect.Matcher MATCHER_UNKNOWN = //
            AccessHelper.Enums.insert(Expect.Matcher.class, NAME_UNKNOWN, -1, null);

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
     * Ensure that all enums are extended.
     */
    public static void ensure() {
        Assert.assertThat(Unknown.MATCHER_UNKNOWN, CoreMatchers.is(CoreMatchers.notNullValue()));
        Assert.assertThat(Unknown.MODE_FAILURE_UNKNOWN, CoreMatchers.is(CoreMatchers.notNullValue()));
        Assert.assertThat(Unknown.MODE_BEAN_UNKNOWN, CoreMatchers.is(CoreMatchers.notNullValue()));
        Assert.assertThat(Unknown.TYPE_RESOLVE_UNKNOWN, CoreMatchers.is(CoreMatchers.notNullValue()));
    }

    /**
     * All enumeration types in package.
     */
    @DataPoints
    public static final Class<?>[] ENUMS = //
        new Reflections(new ConfigurationBuilder() //
            .setUrls(ClasspathHelper.forPackage("org.jactors.junit")) //
            .setScanners(new SubTypesScanner())) //
        .getSubTypesOf(Enum.class).toArray(new Class<?>[0]);
}
