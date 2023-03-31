package uk.ac.ox.oxfish.parameters;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FreeParameter {
    double hardMaximum() default Integer.MAX_VALUE; // a more reasonable default than Double.MAX_VALUE?

    double hardMinimum() default 0;

    double maximum() default Double.NaN;

    double minimum() default Double.NaN;
}
