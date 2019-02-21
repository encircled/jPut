package cz.encircled.jput

import cz.encircled.jput.model.PerformanceTestExecution
import java.util.function.Supplier
import java.util.stream.LongStream

/**
 * @author Vlad on 27-May-17.
 */
object Statistics {

    fun getAverage(vararg input: Long): Double {
        var sum = 0.0
        for (a in input) {
            sum += a.toDouble()
        }
        return sum / input.size
    }

    fun getVariance(vararg input: Long): Double {
        val average = getAverage(*input)
        var temp = 0.0
        for (a in input)
            temp += (a - average) * (a - average)
        return temp / input.size
    }

    fun getDeviation(vararg input: Long): Double {
        return Math.sqrt(getVariance(*input))
    }

    fun round(value: Double): Long {
        return Math.round(value)
    }

    /**
     * @param input      **ordered** array
     * @param percentile target percentile
     * @return values below `percentile`
     */
    fun getPercentile(input: LongArray, percentile: Double): LongArray {
        JPutCommons.validatePercentile(percentile)

        val upperBound = Math.round(input.size * percentile).toInt()

        val result = LongArray(upperBound)
        System.arraycopy(input, 0, result, 0, upperBound)

        return result
    }

    fun maxExecutionTime(execution: PerformanceTestExecution): Long {
        return execution.runs!!.max() ?: throw IllegalStateException()
    }

    fun averageExecutionTime(execution: PerformanceTestExecution): Long {
        return Math.round(LongStream.of(*execution.runs!!).average().orElseThrow<IllegalStateException>(Supplier<IllegalStateException> { IllegalStateException() }))
    }

}
