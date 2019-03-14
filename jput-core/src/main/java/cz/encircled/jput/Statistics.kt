package cz.encircled.jput

import cz.encircled.jput.model.PerfTestExecution

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

    fun getStandardDeviation(input: List<Long>): Double {
        val average = getAverage(input)
        var temp = 0.0
        for (a in input)
            temp += (a - average) * (a - average)
        return temp / input.size
    }

    fun getVariance(input: List<Long>): Double {
        return Math.sqrt(getStandardDeviation(input))
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

    fun maxExecutionTime(execution: PerfTestExecution): Long {
        return execution.executionResult.max() ?: throw IllegalStateException()
    }

    fun averageExecutionTime(execution: PerfTestExecution): Long {
        val avg = execution.executionResult.average()
        return if (avg == Double.NaN) throw IllegalStateException() else Math.round(avg)
    }

}
