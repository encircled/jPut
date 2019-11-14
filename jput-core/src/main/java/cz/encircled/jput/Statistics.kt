package cz.encircled.jput

import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

fun List<Long>.deviation(): Double = sqrt(variance())

fun List<Long>.variance(): Double {
    return if (isEmpty()) 0.0
    else {
        val average = average()
        var temp = 0.0
        for (a in this) temp += (a - average).pow(2.0)
        temp / size
    }
}

fun round(value: Double) = value.roundToLong()

/**
 * @param rank target percentile
 * @return values below `percentile`
 */
fun List<Long>.percentile(rank: Double): List<Long> {
    validatePercentile(rank)

    return subList(0, round(size * rank).toInt())
}

private fun validatePercentile(rank: Double) {
    check(rank in 0.0..1.0) { "Wrong percentile [$rank], must be [0 < Q <= 1]" }
}
