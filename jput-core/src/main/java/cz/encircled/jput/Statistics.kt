package cz.encircled.jput


/**
 * @author Vlad on 27-May-17.
 */
object Statistics {

    fun getAverage(input: List<Long>): Double {
        var sum = 0.0
        for (a in input) {
            sum += a.toDouble()
        }
        return sum / input.size
    }

    fun getVariance(input: List<Long>): Double {
        val average = getAverage(input)
        var temp = 0.0
        for (a in input)
            temp += Math.pow(a - average, 2.0)
        return temp / input.size
    }

    fun round(value: Double): Long {
        return Math.round(value)
    }

    /**
     * @param input      **ordered** array
     * @param percentile target percentile
     * @return values below `percentile`
     */
    fun getPercentile(input: List<Long>, percentile: Double): List<Long> {
        JPutCommons.validatePercentile(percentile)

        return input.subList(0, Math.round(input.size * percentile).toInt())
    }

}
