package cz.encircled.jput.io

import cz.encircled.jput.model.PerformanceTestExecution

/**
 * @author Vlad on 21-May-17.
 */
interface TrendResultWriter {

    fun appendTrendResult(execution: PerformanceTestExecution)

    fun flush()

}
