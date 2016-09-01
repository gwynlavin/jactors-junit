package org.jactors.junit.builder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.jactors.junit.Expect;
import org.jactors.junit.builder.CollectionBuilder;
import org.jactors.junit.test.SimpleTest;
import org.jactors.junit.theory.ObjectTheory;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Suite;


/**
 * Collection builder test.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
    {
        CollectionBuilderTest.BuilderBehavior.class,
        CollectionBuilderTest.BuilderObjectTheory.class
    }
)
public class CollectionBuilderTest { // NOPMD: test suite!

    /**
     * Check collection builder behavior.
     */
    @FixMethodOrder(MethodSorters.JVM)
    public static final class BuilderBehavior extends SimpleTest {

        /**
         * Test creation with null value.
         */
        @Test(expected = IllegalArgumentException.class)
        @Expect(message = "cannot build null collection!")
        public void createWithNullValue() {
            CollectionBuilder.create((List<String>) null);
        }

        /**
         * Test iterator is working.
         */
        @Test
        public void iterIsWorking() {
            for (String value : CollectionBuilder.create(new ArrayList<String>()).insert("value")) {
                Assertions.assertThat(value).isEqualTo("value");
            }
        }

        /**
         * Test insert with single value.
         */
        @Test
        public void insertValue() {
            List<String> list = //
                CollectionBuilder.create(new ArrayList<String>()) //
                .insert("value").build();
            Assertions.assertThat(list).containsExactly("value");
        }

        /**
         * Test insert with array of values.
         */
        @Test
        public void insertArray() {
            List<String> list = //
                CollectionBuilder.create(new ArrayList<String>()) //
                .insert("value-1", "value-2").build();
            Assertions.assertThat(list).containsExactly("value-1", "value-2");
        }

        /**
         * Test insert with null array of values.
         */
        @Test(expected = NullPointerException.class)
        public void insertNullArray() {
            CollectionBuilder.create(new ArrayList<String>()).insert((String[]) null).build();
        }

        /**
         * Test insert with collection of values.
         */
        @Test
        public void insertList() {
            List<String> list = //
                CollectionBuilder.create(new ArrayList<String>()) //
                .insert(Arrays.asList("value-0", "value-1")).build();
            Assertions.assertThat(list).containsExactly("value-0", "value-1");
        }

        /**
         * Test insert with null collection of values.
         */
        @Test(expected = NullPointerException.class)
        public void insertNullList() {
            CollectionBuilder.create(new ArrayList<String>()).insert((List<String>) null).build();
        }

        /**
         * Test conditional insert with single value.
         */
        @Test
        public void condInsertValue() {
            List<String> list = //
                CollectionBuilder.create(new ArrayList<String>()) //
                .condins("value").condins("value").build();
            Assertions.assertThat(list).containsExactly("value");
        }

        /**
         * Test conditional insert with array of values.
         */
        @Test
        public void condInsertArray() {
            List<String> list = //
                CollectionBuilder.create(new ArrayList<String>()) //
                .insert("value-0").condins("value-0", "value-1").build();
            Assertions.assertThat(list).containsExactly("value-0", "value-1");
        }

        /**
         * Test conditional insert with null array of values.
         */
        @Test(expected = NullPointerException.class)
        public void condInsertNullArray() {
            CollectionBuilder.create(new ArrayList<String>()).condins((String[]) null).build();
        }

        /**
         * Test conditional insert with collection of values.
         */
        @Test
        public void condInsertList() {
            List<String> list = //
                CollectionBuilder.create(new ArrayList<String>()) //
                .condins(Arrays.asList("value-0", "value-0", "value-1")).build();
            Assertions.assertThat(list).containsExactly("value-0", "value-1");
        }

        /**
         * Test conditional insert with null collection of values.
         */
        @Test(expected = NullPointerException.class)
        public void condInsertNullList() {
            CollectionBuilder.create(new ArrayList<String>()).condins((List<String>) null).build();
        }

        /**
         * Test deletion with value.
         */
        @Test
        public void deleteValue() {
            List<String> list = //
                CollectionBuilder.create(new ArrayList<String>()) //
                .insert("value-0").insert("value-1") //
                .delete("value-x").delete("value-1").build();
            Assertions.assertThat(list).containsExactly("value-0");
        }

        /**
         * Test deletion with array of values.
         */
        @Test
        public void deleteArray() {
            List<String> list = //
                CollectionBuilder.create(new ArrayList<String>()) //
                .insert("value-0", "value-1").delete("value-x", "value-1").build();
            Assertions.assertThat(list).containsExactly("value-0");
        }

        /**
         * Test deletion with null array of values.
         */
        @Test
        public void deleteNullArray() {
            List<String> list = //
                CollectionBuilder.create(new ArrayList<String>()) //
                .insert("value-0").delete((String[]) null).build();
            Assertions.assertThat(list).containsExactly("value-0");
        }

        /**
         * Test deletion with list of values.
         */
        @Test
        public void deleteList() {
            List<String> list = //
                CollectionBuilder.create(new ArrayList<String>()) //
                .insert("value-0", "value-1") //
                .delete(Arrays.asList("value-1", "value-x")).build();
            Assertions.assertThat(list).containsExactly("value-0");
        }

        /**
         * Test deletion of map entries with null key collection.
         */
        @Test
        public void deleteNullKeyCollection() {
            List<String> list = //
                CollectionBuilder.create(new ArrayList<String>()) //
                .insert("value-0", "value-1") //
                .delete((List<String>) null).build();
            Assertions.assertThat(list).containsExactly("value-0", "value-1");
        }
    }

    /**
     * Check collection builder object theory.
     */
    @RunWith(Theories.class)
    public static final class BuilderObjectTheory extends ObjectTheory {

        /**
         * Default collection builder instance.
         */
        private static final CollectionBuilder<?, ?> DEFAULT = //
            CollectionBuilder.create(new ArrayList<String>());

        /**
         * List of collection builders.
         */
        @DataPoints
        public static final CollectionBuilder<?, ?>[] BUILDERS = new CollectionBuilder[] {
                DEFAULT, DEFAULT, CollectionBuilder.create(new ArrayList<String>())
            };
    }
}
