package org.jactors.junit.theory;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

/**
 * Simple enumeration theory that checks the following rule that should hold for any enumeration
 * type:
 *
 * <ul>
 * <li>Whether the enumeration ordinals are correctly defined.</li>
 * <li>Whether names are correctly resolved to enumeration values.</li>
 * <li>Whether string representation of enumeration is sensible (currently not null).</li>
 * </ul>
 *
 * <p>This theory simplifies automated code coverage in combination with reflection utilities, that
 * allows to enumerate all enumeration classes.</p>
 */
@Ignore
@RunWith(Theories.class)
public class EnumTheory {

    /**
     * Check whether ordinal order of given enumeration class type is correct.
     *
     * @param  <Type>  enumeration type.
     * @param  type    enumeration class type.
     */
    @Theory(nullsAccepted = false)
    public final <Type extends Enum<Type>> void checkOrdinalOrder(Class<Type> type) {
        Type[] values = type.getEnumConstants();
        for (int ordinal = 0; ordinal < values.length; ordinal++) {
            int actual = values[ordinal].ordinal();
            Assert.assertThat(actual, CoreMatchers.equalTo(ordinal));
        }
    }

    /**
     * Check whether name to value resolution of given enumeration class type is correct.
     *
     * @param  <Type>  enumeration type.
     * @param  type    enumeration class type.
     */
    @Theory(nullsAccepted = false)
    public final <Type extends Enum<Type>> void checkNameResolution(Class<Type> type) {
        for (Type value : type.getEnumConstants()) {
            Type actual = Enum.valueOf(type, value.name());
            Assert.assertThat(actual, CoreMatchers.equalTo(value));
        }
    }

    /**
     * Check whether to string method of given enumeration class type is correct and sensible.
     *
     * @param  <Type>  enumeration type.
     * @param  type    enumeration class type.
     */
    @Theory(nullsAccepted = false)
    public final <Type extends Enum<Type>> void checkToStringNotNull(Class<Type> type) {
        for (Type value : type.getEnumConstants()) {
            Assert.assertThat(value.toString(), CoreMatchers.notNullValue());
        }
    }
}
