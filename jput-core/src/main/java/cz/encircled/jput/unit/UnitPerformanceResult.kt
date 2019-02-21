package cz.encircled.jput.unit

/**
 * @author Vlad on 28-May-17.
 */
data class UnitPerformanceResult(var isAverageLimitMet: Boolean = true, var isMaxLimitMet: Boolean = true,
                                 var runAverageTime: Long = 0, var runMaxTime: Long = 0) {

    val isError: Boolean
        get() = !isAverageLimitMet || !isMaxLimitMet

}
