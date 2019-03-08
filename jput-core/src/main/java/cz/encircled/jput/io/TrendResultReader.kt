package cz.encircled.jput.io

import cz.encircled.jput.model.PerformanceTestExecution

/**
 * @author Vlad on 21-May-17.
 */
interface TrendResultReader {

    fun getReferenceExecutions(newExecution: PerformanceTestExecution, referenceExecutionsCount: Int): List<Long>

}
