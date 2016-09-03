package org.jactors.junit.theory;

import java.io.Serializable;
import java.util.Comparator;

import org.junit.FixMethodOrder;
import org.junit.experimental.theories.DataPoints;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Suite;


/**
 * Compare and object theory test.
 */
@FixMethodOrder(MethodSorters.JVM)
@RunWith(Suite.class)
@Suite.SuiteClasses(
    {
        CompareTheoryTest.ComparableTheory.class,
        CompareTheoryTest.ComparatorTheory.class
    }
)
public class CompareTheoryTest {

    /**
     * Singleton comparator instance.
     */
    protected static final Compares COMPARATOR = new Compares();

    /**
     * Custom comparable.
     */
    private static final class Compare implements Comparable<Compare>, Serializable {

        /**
         * Serial version unique identifier.
         */
        private static final long serialVersionUID = -12926686946566247L;

        /**
         * Comparable name.
         */
        protected final String name;

        /**
         * Create custom comparable with given name.
         *
         * @param  name  comparable name.
         */
        public Compare(String name) {
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        public int compareTo(Compare that) {
            return COMPARATOR.compare(this, that);
        }

        /**
         * {@inheritDoc}
         */
        public int hashCode() {
            return (this.name == null) ? 0 : this.name.hashCode();
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
            Compare other = (Compare) obj;
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
            return "Compare[name=" + this.name + "]";
        }
    }

    /**
     * Custom comparator.
     */
    protected static final class Compares implements Comparator<Compare> {

        /**
         * {@inheritDoc}
         */
        public int compare(Compare first, Compare second) {
            if ((first.name == null) && (second.name == null)) {
                return 0;
            } else if (first.name == null) {
                return -1;
            } else if (second.name == null) {
                return 1;
            }
            return first.name.compareTo(second.name);
        }
    }

    /**
     * Comparable theory test.
     */
    public static class ComparableTheory extends CompareTheory<Compare> {

        /**
         * Compared data points.
         */
        @DataPoints
        public static final Compare[] COMPARE = new Compare[] {
                new Compare(null), new Compare("first"), new Compare("second"), new Compare("first")
            };
    }

    /**
     * Comparator theory test.
     */
    public static class ComparatorTheory extends CompareTheory<Compare> {

        /**
         * Compared data points.
         */
        @DataPoints
        public static final Compare[] COMPARE = new Compare[] {
                new Compare(null), new Compare("first"), new Compare("second"), new Compare("first")
            };

        /**
         * Constructor to check comparator.
         */
        public ComparatorTheory() {
            super(COMPARATOR);
        }
    }
}
