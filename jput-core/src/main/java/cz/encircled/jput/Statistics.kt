package cz.encircled.jput

import kotlin.math.pow
import kotlin.math.roundToLong


/**
 * @author Vlad on 27-May-17.
 */
object Statistics {

    fun getAverage(input: List<Long>) = input.average()

    fun getVariance(input: List<Long>): Double {
        val average = getAverage(input)
        var temp = 0.0
        for (a in input)
            temp += (a - average).pow(2.0)
        return temp / input.size
    }

    fun round(value: Double) = value.roundToLong()

    /**
     * @param input      **ordered** array
     * @param rank target percentile
     * @return values below `percentile`
     */
    fun getPercentile(input: List<Long>, rank: Double): List<Long> {
        validatePercentile(rank)

        return input.subList(0, round(input.size * rank).toInt())
    }

    private fun validatePercentile(rank: Double) {
        check(!(rank <= 0.0 || rank > 1.0)) { "Wrong percentile [$rank], must be [0 < Q <= 1]" }
    }

}
