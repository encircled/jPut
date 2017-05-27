package cz.encircled.jput.trend;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Vlad on 27-May-17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PerformanceTrendTest {

    int warmUp() default 0;

    int repeats() default 1;

    double averageTimeTreshold() default -1;

    PercentileThreshold[] percentiles() default {};

}
