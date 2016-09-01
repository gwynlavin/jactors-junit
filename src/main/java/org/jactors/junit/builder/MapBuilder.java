package org.jactors.junit.builder;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Map builder that allows to insert and delete of map elements in a fluent way.
 *
 * @param   <Key>    type of key object.
 * @param   <Value>  type of value object.
 * @param   <Type>   type of map object.
 */
public class MapBuilder<Key, Value, Type extends Map<Key, Value>> implements Iterable<Map.Entry<Key, Value>> {

    /**
     * Delegate map for modification.
     */
    protected final Type map;

    /**
     * Create map builder with given delegate map for modification.
     *
     * @param  map  delegate map for modification.
     */
    protected MapBuilder(Type map) {
        if (map == null) {
            throw new IllegalArgumentException("cannot build null map!");
        }
        this.map = map;
    }

    /**
     * Create map builder with given delegate map for modification.
     *
     * @param   <Key>    type of key object.
     * @param   <Value>  type of value object.
     * @param   <Type>   type of map object.
     * @param   map      delegate map for modification.
     *
     * @return  map builder with given delegate map.
     */
    public static <Key, Value, Type extends Map<Key, Value>> MapBuilder<Key, Value, Type> create(Type map) {
        return new MapBuilder<Key, Value, Type>(map);
    }

    /**
     * Insert map element with given key and given value and return map builder.
     *
     * @param   key    key of element.
     * @param   value  value of element.
     *
     * @return  map builder
     */
    public MapBuilder<Key, Value, Type> insert(Key key, Value value) {
        this.map.put(key, value);
        return this;
    }

    /**
     * Insert all map elements of given reference map and return map builder.
     *
     * @param   map  reference map.
     *
     * @return  map builder.
     */
    public MapBuilder<Key, Value, Type> insert(Map<Key, Value> map) {
        this.map.putAll(map);
        return this;
    }

    /**
     * Insert map elements with given list of keys and given value and return map builder.
     *
     * @param   keys   list of key for element.
     * @param   value  value of element.
     *
     * @return  map builder
     */
    public MapBuilder<Key, Value, Type> insert(Collection<Key> keys, Value value) {
        for (Key key : keys) {
            this.map.put(key, value);
        }
        return this;
    }

    /**
     * Conditional insert if absent of map elements with key and given value and return map builder.
     * If the given key is present the original element is kept.
     *
     * @param   key    key of element.
     * @param   value  value of element.
     *
     * @return  map builder
     */
    public MapBuilder<Key, Value, Type> condins(Key key, Value value) {
        if (!this.map.containsKey(key)) {
            this.map.put(key, value);
        }
        return this;
    }

    /**
     * Conditional insert if absent of all map elements of given reference map and return map
     * builder.
     *
     * @param   map  reference map.
     *
     * @return  map builder.
     */
    public MapBuilder<Key, Value, Type> condins(Map<Key, Value> map) {
        for (Map.Entry<Key, Value> entry : map.entrySet()) {
            this.condins(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Conditional insert if absent of map elements with given list of keys and given value and
     * return map builder.
     *
     * @param   keys   list of key for element.
     * @param   value  value of element.
     *
     * @return  map builder
     */
    public MapBuilder<Key, Value, Type> condins(Collection<Key> keys, Value value) {
        for (Key key : keys) {
            if (!this.map.containsKey(key)) {
                this.map.put(key, value);
            }
        }
        return this;
    }

    /**
     * Delete map element with given key and return map builder.
     *
     * @param   key  key of element.
     *
     * @return  map builder.
     */
    public MapBuilder<Key, Value, Type> delete(Key key) {
        this.map.remove(key);
        return this;
    }

    /**
     * Delete map element with given key and value, where key and value determine the map element,
     * and return map builder.
     *
     * @param   key    key of element.
     * @param   value  value of element.
     *
     * @return  map builder.
     */
    public MapBuilder<Key, Value, Type> delete(Key key, Value value) {
        if (value.equals(this.map.get(key))) {
            this.map.remove(key);
        }
        return this;
    }

    /**
     * Delete all map elements of given reference map, where key and value of the reference map
     * determine the map elements, and return map builder.
     *
     * @param   map  list of element.
     *
     * @return  map builder.
     */
    public MapBuilder<Key, Value, Type> delete(Map<Key, Value> map) {
        if (map != null) {
            for (Map.Entry<Key, Value> entry : map.entrySet()) {
                this.delete(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    /**
     * Delete map elements with given list of keys and return map builder.
     *
     * @param   keys  list of keys of element.
     *
     * @return  map builder.
     */
    public MapBuilder<Key, Value, Type> delete(Collection<Key> keys) {
        if (keys != null) {
            for (Key key : keys) {
                this.map.remove(key);
            }
        }
        return this;
    }

    /**
     * Return modified delegate map.
     *
     * @return  modified delegate map.
     */
    public Type build() {
        return this.map;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<Entry<Key, Value>> iterator() {
        return this.map.entrySet().iterator();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return this.map.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof MapBuilder) {
            return this.map.equals(((MapBuilder<?, ?, ?>) obj).map);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return this.map.toString();
    }
}
