package org.jactors.junit.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * Abstract parameterized test.
 */
@FixMethodOrder(MethodSorters.JVM)
@RunWith(org.junit.runners.Parameterized.class)
public abstract class ParameterTest extends SimpleTest { // NOPMD: should be extended!

    /**
     * Create parameterized test without test result object.
     */
    public ParameterTest() {
        // Nothing to do.
    }

    /**
     * Create parameterized test with given test result object.
     *
     * @param  result  test result object.
     */
    public ParameterTest(Object result) {
        this.rule.expect(result);
    }

    /**
     * Create parameterized test builder.
     *
     * @return  parameterized test builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Simple test case parameter builder.
     */
    public static final class Builder implements Iterable<Object[]> {

        /**
         * Delegate list for test case parameters.
         */
        private final List<Object[]> list = new ArrayList<Object[]>();

        /**
         * Default builder constructor.
         */
        protected Builder() {
            // NOPMD:
        }

        /**
         * Add test case parameter set with given test objects.
         *
         * @param   test  test objects.
         *
         * @return  test case parameter builder for further setup.
         */
        public Builder add(Object... test) {
            this.list.add(test);
            return this;
        }

        /**
         * Add all test case parameter sets from given parameter builder.
         *
         * @param   builder  test case parameter builder.
         *
         * @return  test case parameter builder for further setup.
         */
        public Builder add(Builder builder) {
            this.list.addAll(builder.list);
            return this;
        }

        /**
         * Remove last element from given parameter builder.
         *
         * @return  test case parameter builder for further setup.
         */
        public Builder remove() {
            this.list.remove(this.list.size() - 1);
            return this;
        }

        /**
         * Clear list of test case parameters from given parameter builder.
         *
         * @return  test case parameter builder for further setup.
         */
        public Builder clear() {
            this.list.clear();
            return this;
        }

        /**
         * Return test case parameters.
         *
         * @return  test case parameters.
         */
        public List<Object[]> build() {
            return this.list;
        }

        /**
         * {@inheritDoc}
         */
        public Iterator<Object[]> iterator() {
            return this.list.iterator();
        }
    }
}
