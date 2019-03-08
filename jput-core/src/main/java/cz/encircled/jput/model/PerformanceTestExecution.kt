package cz.encircled.jput.model


/**
 * @author Vlad on 20-May-17.
 */
data class PerformanceTestExecution(var executionId: Long = 0, var testClass: String? = null, var testMethod: String? = null,
                                    var runs: MutableList<Long> = mutableListOf())