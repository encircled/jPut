package cz.encircled.jput

import kotlin.math.pow
import kotlin.math.roundToInt
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

/**
 * List must be ordered
 *
 * @param rank target percentile
 * @return values below `percentile`
 */
fun List<Long>.percentile(rank: Double): List<Long> {
    validatePercentile(rank)
    for (i in 1 until size) {
        check(get(i) >= get(i - 1)) { "List must be ordered!" }
    }

    return subList(0, (size * rank).roundToInt())
}

private fun validatePercentile(rank: Double) {
    check(rank in 0.0..1.0) { "Wrong percentile [$rank], must be [0 < Q <= 1]" }
}
