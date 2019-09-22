package cz.encircled.jput.reporter

import cz.encircled.jput.model.PerfTestExecution
import io.qameta.allure.Allure
import io.qameta.allure.AllureLifecycle
import io.qameta.allure.model.Stage
import io.qameta.allure.model.Status
import io.qameta.allure.model.StepResult
import io.qameta.allure.model.TestResult
import io.qameta.allure.util.ResultsUtils

open class JPutAllureReporter : JPutReporter {

    private val lifecycle: AllureLifecycle = Allure.getLifecycle()

    override fun beforeClass(clazz: Class<*>) {
        val id = getTestCaseId(clazz)

        val result = TestResult()
                .withUuid(id)
                .withName(id)
                .withFullName(getTestCaseId(clazz))
                .withStage(Stage.SCHEDULED)
                .withLabels(ResultsUtils.createStoryLabel(id), ResultsUtils.createHostLabel(), ResultsUtils.createThreadLabel())
                .withDescription(id)
                .withHistoryId(id)

        lifecycle.scheduleTestCase(result)
        lifecycle.startTestCase(id)
    }

    override fun afterClass(clazz: Class<*>) {
        val id = getTestCaseId(clazz)

        lifecycle.updateTestCase(id) { getTestCaseStatus(it) }
        lifecycle.stopTestCase(id)
        lifecycle.writeTestCase(id)
    }

    override fun beforeTest(execution: PerfTestExecution) {
        lifecycle.startStep(execution.conf.testId, StepResult().withName(execution.conf.testId))
    }

    override fun afterTest(execution: PerfTestExecution) {
        lifecycle.updateStep(execution.conf.testId) {
            if (execution.violations.isNotEmpty()) {
                it.withStatus(Status.FAILED)
                Allure.addAttachment("Validation error", execution.violations.joinToString { v ->
                    v.messageProducer.invoke(execution)
                })
            } else {
                it.withStatus(Status.PASSED)
            }
            Allure.addAttachment("Execution times", "avg ${execution.executionAvg}ms, max ${execution.executionMax}ms")
        }
        lifecycle.stopStep(execution.conf.testId)
    }

    fun getTestCaseStatus(testResult: TestResult) {
        val isFailed = testResult.steps.any { it.status != Status.PASSED }

        if (isFailed) testResult.withStatus(Status.FAILED)
        else testResult.withStatus(Status.PASSED)
    }

    fun getTestCaseId(clazz: Class<*>): String = clazz.simpleName

}