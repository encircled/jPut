package cz.encircled.jput

/**
 * @author Vlad on 27-May-17.
 */
object JPutCommons {

    fun validatePercentile(rank: Double) {
        check(!(rank <= 0.0 || rank > 1.0)) { "Wrong percentile [$rank], must be [0 < Q <= 1]" }
    }

}
