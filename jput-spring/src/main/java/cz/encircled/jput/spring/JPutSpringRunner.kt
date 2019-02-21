package cz.encircled.jput.spring

import cz.encircled.jput.JPutContext
import cz.encircled.jput.model.MethodConfiguration
import cz.encircled.jput.model.MethodTrendConfiguration
import cz.encircled.jput.unit.PerformanceTest
import junit.framework.AssertionFailedError
import org.junit.AssumptionViolatedException
import org.junit.internal.runners.model.EachTestNotifier
import org.junit.internal.runners.statements.Fail
import org.junit.runner.notification.RunNotifier
import org.junit.runners.model.FrameworkMethod
import org.junit.runners.model.InitializationError
import org.junit.runners.model.Statement
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

/**
 * @author Vlad on 20-May-17.
 */
class JPutSpringRunner
/**
 * Construct a new `SpringJUnit4ClassRunner` and initialize a
 * [org.springframework.test.context.TestContextManager] to provide Spring testing functionality to
 * standard JUnit tests.
 *
 * @param clazz the test class to be run
 * @see .createTestContextManager
 */
@Throws(InitializationError::class)
constructor(clazz: Class<*>) : SpringJUnit4ClassRunner(clazz) {

    private val unitAnalyzer = JPutContext.context.unitPerformanceAnalyzer

    private val trendAnalyzer = JPutContext.context.trendAnalyzer

    private val trendResultReader = JPutContext.context.trendResultReader

    private val trendResultWriter = JPutContext.context.trendResultWriter

    override fun run(notifier: RunNotifier) {
        super.run(notifier)
    }

    override fun runChild(frameworkMethod: FrameworkMethod, notifier: RunNotifier) {
        val description = describeChild(frameworkMethod)


        if (isTestMethodIgnored(frameworkMethod)) {
            notifier.fireTestIgnored(description)
        } else {
            val annotation = frameworkMethod.getAnnotation(PerformanceTest::class.java)
            if (annotation == null || !JPutContext.context.isPerformanceTestEnabled) {
                super.runChild(frameworkMethod, notifier)
                return
            }

            val conf = MethodConfiguration.fromAnnotation(annotation)
            val trendAnnotation =
                    if (annotation.performanceTrend.isNotEmpty()) annotation.performanceTrend[0]
                    else null
            if (trendAnnotation != null) {
                conf.trendConfiguration = MethodTrendConfiguration.fromAnnotation(trendAnnotation)
            }

            val testRun = unitAnalyzer.buildTestExecution(conf, frameworkMethod.method)

            val statement =
                    try {
                        methodBlock(frameworkMethod)
                    } catch (ex: Throwable) {
                        Fail(ex)
                    }

            val eachNotifier = EachTestNotifier(notifier, description)
            eachNotifier.fireTestStarted()
            try {
                repeat(conf.warmUp) {
                    statement.evaluate()
                }

                repeat(conf.repeats) {
                    val start = System.nanoTime()
                    statement.evaluate()
                    unitAnalyzer.addTestRun(testRun, System.nanoTime() - start)
                }

                val result = unitAnalyzer.analyzeUnitTrend(testRun, conf)
                if (result.isError) {
                    throw AssertionFailedError("Unit performance test failed" + unitAnalyzer.buildErrorMessage(result, conf))
                }

                if (conf.trendConfiguration != null) {
                    val standardSampleSize = conf.trendConfiguration!!.standardSampleSize * conf.repeats
                    val standardSampleRuns = trendResultReader.getStandardSampleRuns(testRun, standardSampleSize)
                    if (standardSampleRuns != null && standardSampleRuns.size >= conf.trendConfiguration!!.standardSampleSize) {
                        val trendResult = trendAnalyzer.analyzeTestTrend(conf.trendConfiguration!!, testRun, *standardSampleRuns)
                        if (trendResult.isError) {
                            throw AssertionFailedError("Trend performance test failed" + trendAnalyzer.buildErrorMessage(trendResult, conf))
                        }
                    }
                    trendResultWriter.appendTrendResult(testRun)
                    trendResultWriter.flush()
                }
            } catch (e: AssumptionViolatedException) {
                eachNotifier.addFailedAssumption(e)
            } catch (e: Throwable) {
                eachNotifier.addFailure(e)
            } finally {
                eachNotifier.fireTestFinished()
            }
        }

    }

    override fun withAfters(frameworkMethod: FrameworkMethod, testInstance: Any, statement: Statement): Statement {
        trendResultWriter.flush()
        return super.withAfters(frameworkMethod, testInstance, statement)
    }

    override fun withAfterClasses(statement: Statement): Statement {
        trendResultWriter.flush()
        return super.withAfterClasses(statement)
    }
}

