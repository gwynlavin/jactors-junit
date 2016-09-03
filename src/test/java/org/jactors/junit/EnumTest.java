package org.jactors.junit;

import org.jactors.junit.theory.EnumTheory;
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
     * All enumeration types in package.
     */
    @DataPoints
    public static final Class<?>[] ENUMS = //
        new Reflections(new ConfigurationBuilder() //
            .setUrls(ClasspathHelper.forPackage("org.jactors.junit")) //
            .setScanners(new SubTypesScanner())) //
        .getSubTypesOf(Enum.class).toArray(new Class<?>[0]);
}
