package cz.encircled.jput

/**
 * @author Vlad on 27-May-17.
 */
object JPutCommons {

    fun validatePercentile(percentile: Double) {
        if (percentile <= 0.0 || percentile > 1.0) {
            throw IllegalStateException("Wrong percentile [$percentile], must be [0 < Q <= 1]")
        }
    }

}
