package org.jactors.junit;

import java.util.List;

import org.jactors.junit.test.ParameterTest;
import org.junit.runners.Parameterized;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

/**
 *
 */
public class HelperTest extends org.jactors.junit.test.HelperTest {

    /**
     * All helper types in package.
     *
     * @return return list of class types.
     */
    @Parameterized.Parameters(name="{index}: {0}")
    public static List<Object[]> data() {
        Class<?>[] array = new Reflections(new ConfigurationBuilder() //
                    .setUrls(ClasspathHelper.forPackage("org.jactors.junit")) //
                    .setScanners(new SubTypesScanner(false))) //
                .getSubTypesOf(Object.class).toArray(new Class<?>[0]);

        Builder builder = ParameterTest.builder();
        for (Class<?> type : array) {
            builder.add(type);
        }
        return builder.build();
    }
}
