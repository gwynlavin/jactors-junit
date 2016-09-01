package org.jactors.junit.theory;

import org.jactors.junit.Property;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;


/**
 * Bean theory implementation that allows to check validity of bean properties based on naming
 * conventions for getter, setter, and field names. Properties can also be defined by annotating
 * template fields in the inherited theory using {@link Property}.
 */
@Ignore
@RunWith(Theories.class)
public abstract class BeanTheory { // NOPMD: must be inherited!

    /**
     * Validate target bean object whether it behaves as valid bean object.
     *
     * @param   target  target bean object.
     *
     * @throws  Throwable  any exception that is not expected.
     */
    @Theory(nullsAccepted = false)
    public final void isBeanValid(Object target) throws Throwable {
        Assume.assumeNotNull(target);
        new Property.Checker().check(target, this);
    }
}
