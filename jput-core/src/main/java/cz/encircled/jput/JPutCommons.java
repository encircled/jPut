package cz.encircled.jput;

/**
 * @author Vlad on 27-May-17.
 */
public class JPutCommons {

    public static void validatePercentile(double percentile) {
        if (percentile <= 0D || percentile > 1D) {
            throw new IllegalStateException("Wrong percentile [" + percentile + "], must be [0 < Q <= 1]");
        }
    }

}
