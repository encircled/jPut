package cz.encircled.jput

import cz.encircled.jput.context.context
import cz.encircled.jput.model.MethodConfiguration
import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.trend.TrendAnalyzer
import cz.encircled.jput.unit.PerformanceTest
import cz.encircled.jput.unit.UnitPerformanceAnalyzer
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
                val conf = MethodConfiguration.fromAnnotation(annotation)
                val execution = context.unitPerformanceAnalyzer.buildTestExecution(conf, frameworkMethod.method)

                val result = performExecution(conf, statement)
                performAnalysis(context.unitPerformanceAnalyzer, execution, conf, context.trendAnalyzer)
                context.unitPerformanceAnalyzer.addTestExecutions(execution, result)
            }
        } catch (e: AssumptionViolatedException) {
            eachNotifier.addFailedAssumption(e)
        } catch (e: Throwable) {
            eachNotifier.addFailure(e)
        } finally {
            eachNotifier.fireTestFinished()
        }
    }

    private fun performAnalysis(performanceAnalyzer: UnitPerformanceAnalyzer, execution: PerfTestExecution, conf: MethodConfiguration, trendAnalyzer: TrendAnalyzer) {
        val result = performanceAnalyzer.analyzeUnitTrend(execution, conf)
        if (result.isError) {
            throw AssertionFailedError("Unit performance test failed" + performanceAnalyzer.buildErrorMessage(result, conf))
        }

        if (conf.trendConfiguration != null) {
            // Assume that first has highest priority
            val sample = context.resultRecorders[0].getSample(execution, conf.trendConfiguration)

            if (sample.size >= conf.trendConfiguration.sampleSize) {
                val trendResult = trendAnalyzer.analyzeTestTrend(conf.trendConfiguration, execution, sample)
                if (trendResult.isError) {
                    throw AssertionFailedError("Trend performance test failed" + trendAnalyzer.buildErrorMessage(trendResult, conf))
                }
            }

            context.resultRecorders.forEach {
                it.appendTrendResult(execution)
            }
        }
    }

    private fun performExecution(conf: MethodConfiguration, statement: Statement): List<Long> {
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