package org.jactors.junit.builder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.MapEntry;
import org.jactors.junit.Expect;
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
 * Map builder test.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
    {
        MapBuilderTest.BuilderBehavior.class,
        MapBuilderTest.BuilderObjectTheory.class
    }
)
public class MapBuilderTest {

    /**
     * Check map builder behavior.
     */
    @FixMethodOrder(MethodSorters.JVM)
    public static final class BuilderBehavior extends SimpleTest {

        /**
         * Test creation with null value.
         */
        @Test(expected = IllegalArgumentException.class)
        @Expect(message = "cannot build null map!")
        public void createWithNullValue() {
            MapBuilder.create((Map<String, String>) null);
        }

        /**
         * Test iterator is working.
         */
        @Test
        public void iterIsWorking() {
            for (Map.Entry<String, String> entry
                : MapBuilder.create(new HashMap<String, String>()).insert("key", "value")) {
                Assertions.assertThat(entry.getKey()).isEqualTo("key");
                Assertions.assertThat(entry.getValue()).isEqualTo("value");
            }
        }

        /**
         * Test insert with single key-value pair.
         */
        @Test
        public void insertKeyValue() {
            Map<String, String> map = //
                MapBuilder.create(new HashMap<String, String>()).insert("key", "value").build();
            Assertions.assertThat(map).containsExactly(MapEntry.entry("key", "value"));
        }

        /**
         * Test insert of all elements from map.
         */
        @Test
        public void insertMap() {
            Map<String, String> base = //
                MapBuilder.create(new HashMap<String, String>()) //
                .insert("key-0", "value-0").insert("key-1", "value-1").build();
            Map<String, String> map = //
                MapBuilder.create(new HashMap<String, String>()).insert(base).build();
            Assertions.assertThat(map).containsExactly(MapEntry.entry("key-0", "value-0"),
                MapEntry.entry("key-1", "value-1"));
        }

        /**
         * Test insert of all elements from map.
         */
        @Test(expected = NullPointerException.class)
        public void insertNullMap() {
            MapBuilder.create(new HashMap<String, String>()).insert(null).build();
        }

        /**
         * Test insert key collection with same value pairs.
         */
        @Test
        public void insertKeyCollection() {
            List<String> keys = Arrays.asList("key-0", "key-1");
            Map<String, String> map = //
                MapBuilder.create(new HashMap<String, String>()).insert(keys, "value").build();
            Assertions.assertThat(map).containsExactly(MapEntry.entry("key-0", "value"),
                MapEntry.entry("key-1", "value"));
        }

        /**
         * Test insert null key collection with same value pairs.
         */
        @Test(expected = NullPointerException.class)
        public void insertNullKeyCollection() {
            MapBuilder.create(new HashMap<String, String>()).insert((List<String>) null, "value").build();
        }

        /**
         * Test conditional insert with single key-value pair.
         */
        @Test
        public void condInsertKeyValue() {
            Map<String, String> map = //
                MapBuilder.create(new HashMap<String, String>()) //
                .condins("key", "value-0").condins("key", "value-1").build();
            Assertions.assertThat(map).containsExactly(MapEntry.entry("key", "value-0"));
        }

        /**
         * Test conditional insert of all elements from map.
         */
        @Test
        public void condInsertMap() {
            Map<String, String> base = //
                MapBuilder.create(new HashMap<String, String>()) //
                .insert("key-0", "value-x").insert("key-1", "value-x").build();
            Map<String, String> map =
                MapBuilder.create(new HashMap<String, String>()) //
                .insert("key-0", "value-0").condins(base).build();
            Assertions.assertThat(map).containsExactly(MapEntry.entry("key-0", "value-0"),
                MapEntry.entry("key-1", "value-x"));
        }

        /**
         * Test conditional insert of all elements from map.
         */
        @Test(expected = NullPointerException.class)
        public void condInsertNullMap() {
            MapBuilder.create(new HashMap<String, String>()).condins(null).build();
        }

        /**
         * Test conditional insert key collection with same value pairs.
         */
        @Test
        public void condInsertKeyCollection() {
            List<String> keys = Arrays.asList("key-0", "key-1");
            Map<String, String> map = //
                MapBuilder.create(new HashMap<String, String>()) //
                .insert("key-0", "value-0").condins(keys, "value-x").build();
            Assertions.assertThat(map).containsExactly(MapEntry.entry("key-0", "value-0"),
                MapEntry.entry("key-1", "value-x"));
        }

        /**
         * Test conditional insert null key collection with same value pairs.
         */
        @Test(expected = NullPointerException.class)
        public void condInsertNullKeyCollection() {
            MapBuilder.create(new HashMap<String, String>()).condins((List<String>) null, "value").build();
        }

        /**
         * Test deletion of map entry with key.
         */
        @Test
        public void deleteKey() {
            Map<String, String> map = //
                MapBuilder.create(new HashMap<String, String>()) //
                .insert("key-0", "value-0").delete("key-0").delete("key-1").build();
            Assertions.assertThat(map).isEmpty();
        }

        /**
         * Test deletion of map entry with key-value pair.
         */
        @Test
        public void deleteKeyValue() {
            Map<String, String> map = //
                MapBuilder.create(new HashMap<String, String>()) //
                .insert("key-0", "value-0").insert("key-1", "value-1") //
                .delete("key-0", "value-x").delete("key-1", "value-1").build();
            Assertions.assertThat(map).containsExactly(MapEntry.entry("key-0", "value-0"));
        }

        /**
         * Test deletion of map entries given by reference map.
         */
        @Test
        public void deleteMap() {
            Map<String, String> base = //
                MapBuilder.create(new HashMap<String, String>()) //
                .insert("key-0", "value-x").insert("key-1", "value-1").build();
            Map<String, String> map = //
                MapBuilder.create(new HashMap<String, String>()) //
                .insert("key-0", "value-0").insert("key-1", "value-1").delete(base).build();
            Assertions.assertThat(map).containsExactly(MapEntry.entry("key-0", "value-0"));
        }

        /**
         * Test deletion of map entries with null reference map.
         */
        @Test
        public void deleteNullMap() {
            Map<String, String> map = //
                MapBuilder.create(new HashMap<String, String>()) //
                .insert("key-0", "value-0").delete((Map<String, String>) null).build();
            Assertions.assertThat(map).containsExactly(MapEntry.entry("key-0", "value-0"));
        }

        /**
         * Test deletion of map entries with key collection.
         */
        @Test
        public void deleteKeyCollection() {
            List<String> keys = Arrays.asList("key-1", "key-x");
            Map<String, String> map = //
                MapBuilder.create(new HashMap<String, String>()) //
                .insert("key-0", "value-0").insert("key-1", "value-1").delete(keys).build();
            Assertions.assertThat(map).containsExactly(MapEntry.entry("key-0", "value-0"));
        }

        /**
         * Test deletion of map entries with null key collection.
         */
        @Test
        public void deleteNullKeyCollection() {
            Map<String, String> map = //
                MapBuilder.create(new HashMap<String, String>()) //
                .insert("key-0", "value-0").delete((List<String>) null).build();
            Assertions.assertThat(map).containsExactly(MapEntry.entry("key-0", "value-0"));
        }
    }

    /**
     * Check map builder object theory.
     */
    @RunWith(Theories.class)
    public static final class BuilderObjectTheory extends ObjectTheory {

        /**
         * Default map builder instance.
         */
        private static final MapBuilder<?, ?, ?> DEFAULT = //
            MapBuilder.create(new HashMap<String, String>());

        /**
         * Map builders.
         */
        @DataPoints
        public static final MapBuilder<?, ?, ?>[] BUILDERS = new MapBuilder[] {
                DEFAULT, DEFAULT, MapBuilder.create(new HashMap<String, String>())
            };
    }
}
