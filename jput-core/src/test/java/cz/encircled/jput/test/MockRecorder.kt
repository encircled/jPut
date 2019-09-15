package cz.encircled.jput.test

import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.recorder.ResultRecorder

/**
 * @author Vlad on 15-Sep-19.
 */
class MockRecorder : ResultRecorder {

    val executions = mutableListOf<PerfTestExecution>()

    var mockSample = listOf<Long>()

    override fun getSample(execution: PerfTestExecution): List<Long> = mockSample

    override fun appendTrendResult(execution: PerfTestExecution) {
        executions.add(execution)
    }

    override fun flush() {
    }
}