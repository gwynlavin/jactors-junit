package org.jactors.junit.builder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * Collection builder that allows to insert and delete collection elements in a fluent way.
 *
 * @param   <Value>  type of value object.
 * @param   <Type>   type of set object.
 */
public class CollectionBuilder<Value, Type extends Collection<Value>> implements Iterable<Value> {

    /**
     * Delegate collection for modification.
     */
    protected final Type coll;

    /**
     * Create collection builder with given delegate collection for modification.
     *
     * @param  coll  delegate collection for modification.
     */
    protected CollectionBuilder(Type coll) {
        if (coll == null) {
            throw new IllegalArgumentException("cannot build null collection!");
        }
        this.coll = coll;
    }

    /**
     * Create collection builder with given delegate collection for modification.
     *
     * @param   <Value>  type of value object.
     * @param   <Type>   type of collection object.
     * @param   coll     delegate collection for modification.
     *
     * @return  collection builder with given delegate collection.
     */
    public static <Value, Type extends Collection<Value>> CollectionBuilder<Value, Type> create(Type coll) {
        return new CollectionBuilder<Value, Type>(coll);
    }

    /**
     * Insert collection element with given value element and return collection builder.
     *
     * @param   value  value element.
     *
     * @return  map builder
     */
    public CollectionBuilder<Value, Type> insert(Value value) {
        this.coll.add(value);
        return this;
    }

    /**
     * Insert collection element with given collection of value elements and return collection
     * builder.
     *
     * @param   values  array of value elements.
     *
     * @return  map builder
     */
    public CollectionBuilder<Value, Type> insert(Value... values) {
        this.coll.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Insert collection element with given collection of value elements and return collection
     * builder.
     *
     * @param   values  collection of value elements.
     *
     * @return  map builder
     */
    public CollectionBuilder<Value, Type> insert(Collection<Value> values) {
        this.coll.addAll(values);
        return this;
    }

    /**
     * Conditional insert if absent of collection element with given value element and return
     * collection builder.
     *
     * @param   value  value element.
     *
     * @return  map builder
     */
    public CollectionBuilder<Value, Type> condins(Value value) {
        if (!this.coll.contains(value)) {
            this.coll.add(value);
        }
        return this;
    }

    /**
     * Conditional insert if absent of collection element with given collection of value elements
     * and return collection builder.
     *
     * @param   values  array of value elements.
     *
     * @return  map builder
     */
    public CollectionBuilder<Value, Type> condins(Value... values) {
        for (Value value : values) {
            this.condins(value);
        }
        return this;
    }

    /**
     * Conditional insert if absent of collection element with given collection of value elements
     * and return collection builder.
     *
     * @param   values  collection of value elements.
     *
     * @return  map builder
     */
    public CollectionBuilder<Value, Type> condins(Collection<Value> values) {
        for (Value value : values) {
            this.condins(value);
        }
        return this;
    }

    /**
     * Delete given value element from collection and return map builder.
     *
     * @param   value  value element.
     *
     * @return  map builder.
     */
    public CollectionBuilder<Value, Type> delete(Value value) {
        this.coll.remove(value);
        return this;
    }

    /**
     * Delete given collection of value elements from collection and return map builder.
     *
     * @param   values  array of value elements.
     *
     * @return  map builder.
     */
    public CollectionBuilder<Value, Type> delete(Value... values) {
        if (values != null) {
            this.coll.removeAll(Arrays.asList(values));
        }
        return this;
    }

    /**
     * Delete given collection of value elements from collection and return map builder.
     *
     * @param   values  collection of value elements.
     *
     * @return  map builder.
     */
    public CollectionBuilder<Value, Type> delete(Collection<Value> values) {
        if (values != null) {
            this.coll.removeAll(values);
        }
        return this;
    }

    /**
     * Return modified delegate collection.
     *
     * @return  modified delegate collection.
     */
    public Type build() {
        return this.coll;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<Value> iterator() {
        return this.coll.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return this.coll.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (obj instanceof CollectionBuilder) {
            return this.coll.equals(((CollectionBuilder<?, ?>) obj).coll);
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return this.coll.toString();
    }
}
