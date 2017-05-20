package cz.encircled.jput;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Vlad on 20-May-17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PerformanceTest {

    int warmUp() default 0;

    int repeats() default 1;

    long maxTimeLimit() default 0L;

    long averageTimeLimit() default 0L;

    long[] percentiles() default {};

}
