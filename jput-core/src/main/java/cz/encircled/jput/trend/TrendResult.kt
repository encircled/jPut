package cz.encircled.jput.trend

/**
 * @author Vlad on 27-May-17.
 */
data class TrendResult(var isAverageMet: Boolean = true, var runAverageTime: Long = 0,
                       var standardAverage: Long = 0, var deviation: Long = 0, var notMetPercentiles: IntArray? = null) {

    val isError: Boolean
        get() = !isAverageMet || notMetPercentiles != null && notMetPercentiles!!.isNotEmpty()

}
