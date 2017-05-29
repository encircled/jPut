package cz.encircled.jput.trend;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Vlad on 27-May-17.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface PerformanceTrend {

    /**
     * Static average time threshold.
     * <p>
     *  Performance trend test will fail if average execution time is greater than average time of standard sample plus given threshold
     * </p>
     */
    double averageTimeThreshold() default -1;

    /**
     * true - use statistic variance of base sample as an average time threshold
     * <p>
     *  Performance trend test will fail if average execution time is greater than average time of standard sample plus its variance
     * </p>
     */
    boolean averageTimeVarianceThreshold() default false;

    PercentileThreshold[] percentiles() default {};

}
