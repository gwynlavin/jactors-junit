package edu.umd.cs.findbugs.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to suppress findbugs warnings in order.
 */
@Retention(RetentionPolicy.CLASS)
@Target(
    {
        ElementType.TYPE,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.CONSTRUCTOR,
        ElementType.LOCAL_VARIABLE,
        ElementType.PACKAGE
    }
)
public @interface SuppressWarnings {

    /**
     * The set of warnings that are to be suppressed by the compiler in the annotated element.
     * Duplicate names are permitted. The second and successive occurrences of a name are ignored.
     * The presence of unrecognized warning names is not an error: Compilers must ignore any warning
     * names they do not recognize. They are, however, free to emit a warning if an annotation
     * contains an unrecognized warning name. Compiler vendors should document the warning names
     * they support in conjunction with this annotation type. They are encouraged to cooperate to
     * ensure that the same names work across multiple compilers.
     *
     * @return  findbugs warnings to suppress.
     */
    public String[] value() default {};

    /**
     * Justification for suppressing findbugs warnings.
     *
     * @return  justification for suppressing findbugs warnings.
     */
    public String justification() default "";
}
