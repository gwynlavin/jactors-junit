package org.jactors.junit;

import org.jactors.junit.theory.HelperTheory;
import org.junit.experimental.theories.DataPoints;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 *
 */
public class HelperTest extends HelperTheory {

    /**
     * All helper types in package.
     */
    @DataPoints
    public static final Class<?>[] HELPERS = //
        new Reflections(new ConfigurationBuilder() //
            .setUrls(ClasspathHelper.forPackage("org.jactors.junit")) //
            .setScanners(new SubTypesScanner(false))) //
        .getSubTypesOf(Object.class).toArray(new Class<?>[0]);
}
