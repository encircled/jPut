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
public @interface PercentileThreshold {

    /**
     * Target percentile (e.g. 70, 90, 95 etc)
     */
    int percentile();

    /**
     * Upper threshold in percents, i.e. allowed variation
     */
    int threshold() default 10;

}
