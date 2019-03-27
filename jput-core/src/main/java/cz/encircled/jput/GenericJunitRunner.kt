package cz.encircled.jput

import cz.encircled.jput.context.context
import cz.encircled.jput.model.PerfTestConfiguration
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.unit.PerformanceTest
import junit.framework.AssertionFailedError
import org.junit.AssumptionViolatedException
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.runner.Description
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.Statement

interface GenericJunitRunner {

    fun executeTest(frameworkMethod: FrameworkMethod,
                    description: Description,
                    statement: Statement,
                    notifier: RunNotifier)

}

class GenericJunitRunnerImpl : GenericJunitRunner {

    override fun executeTest(frameworkMethod: FrameworkMethod,
                             description: Description,
                             statement: Statement,
                             notifier: RunNotifier) {
        val eachNotifier = EachTestNotifier(notifier, description)
        eachNotifier.fireTestStarted()
        try {
            val annotation = frameworkMethod.getAnnotation(PerformanceTest::class.java)
            if (annotation == null || !context.isPerformanceTestEnabled) {
                statement.evaluate()
            } else {
                val conf = PerfTestConfiguration.fromAnnotation(annotation)
                val execution = context.unitPerformanceAnalyzer.buildTestExecution(conf, frameworkMethod.method)

                val result = performExecution(conf, statement)

                execution.executionResult.addAll(result)
                performAnalysis(execution, conf)
                context.resultRecorders.forEach { it.flush() } // TODO
            }
        } catch (e: AssumptionViolatedException) {
            eachNotifier.addFailedAssumption(e)
        } catch (e: Throwable) {
            eachNotifier.addFailure(e)
        } finally {
            eachNotifier.fireTestFinished()
        }
    }

    private fun performAnalysis(execution: PerfTestExecution, conf: PerfTestConfiguration) {
        val unitAnalyzer = context.unitPerformanceAnalyzer
        val trendAnalyzer = context.trendAnalyzer
        val unitViolations = unitAnalyzer.analyzeUnitTrend(execution, conf)
        if (unitViolations.isError) {
            throw AssertionFailedError("Performance unit test failed.\n$unitViolations")
        }

        if (conf.trendConfiguration != null) {
            // Assume that first has highest priority
            val sample = context.resultRecorders[0].getSample(execution, conf.trendConfiguration)

            if (sample.size >= conf.trendConfiguration.sampleSize) {
                val trendViolations = trendAnalyzer.analyzeTestTrend(conf, execution, sample)
                if (trendViolations.isError) {
                    throw AssertionFailedError("Performance trend test failed.\n$trendViolations")
                }
            }

            context.resultRecorders.forEach {
                it.appendTrendResult(execution)
            }
        }
    }

    private fun performExecution(conf: PerfTestConfiguration, statement: Statement): List<Long> {
        repeat(conf.warmUp) {
            statement.evaluate()
        }

        return (1..conf.repeats).map {
            val start = System.nanoTime()
            statement.evaluate()
            (System.nanoTime() - start) / 1000000L
        }
    }

}