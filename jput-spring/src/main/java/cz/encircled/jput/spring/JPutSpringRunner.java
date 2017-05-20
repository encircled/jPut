package cz.encircled.jput.spring;

import cz.encircled.jput.MethodConfiguration;
import cz.encircled.jput.PerformanceTest;
import cz.encircled.jput.PerformanceTestRun;
import junit.framework.AssertionFailedError;
import org.junit.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;
import java.util.stream.LongStream;

/**
 * @author Vlad on 20-May-17.
 */
public class JPutSpringRunner extends SpringJUnit4ClassRunner {

    /**
     * Construct a new {@code SpringJUnit4ClassRunner} and initialize a
     * {@link org.springframework.test.context.TestContextManager} to provide Spring testing functionality to
     * standard JUnit tests.
     *
     * @param clazz the test class to be run
     * @see #createTestContextManager(Class)
     */
    public JPutSpringRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
    }

    @Override
    protected void runChild(FrameworkMethod frameworkMethod, RunNotifier notifier) {
        Description description = describeChild(frameworkMethod);
        PerformanceTest annotation = frameworkMethod.getAnnotation(PerformanceTest.class);
        if (annotation == null) {
            super.runChild(frameworkMethod, notifier);
            return;
        }
        MethodConfiguration conf = MethodConfiguration.fromAnnotation(annotation);
        PerformanceTestRun run = new PerformanceTestRun(conf);
        run.testMethod = frameworkMethod.getMethod();

        if (isTestMethodIgnored(frameworkMethod)) {
            notifier.fireTestIgnored(description);
        } else {
            Statement statement;
            try {
                statement = methodBlock(frameworkMethod);
            } catch (Throwable ex) {
                statement = new Fail(ex);
            }
            EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
            eachNotifier.fireTestStarted();
            try {
                for (int i = 1; i <= conf.warmUp; i++) {
                    statement.evaluate();
                }

                for (int i = 1; i <= conf.repeats; i++) {
                    long start = System.currentTimeMillis();
                    statement.evaluate();
                    run.addRun(System.currentTimeMillis() - start);
                }

                long runAvgTime = run.averageRunTime();
                if (conf.averageTimeLimit > 0 && runAvgTime > conf.averageTimeLimit) {
                    String assertMessage = String.format("\nLimit avg time = %d ms\nActual avg time = %d ms\n\n", conf.averageTimeLimit, runAvgTime);
                    throw new AssertionFailedError(assertMessage + "Performance test failed, average time is greater then limit: " + run);
                }
                long runMaxTime = run.maxRunTime();
                if (conf.maxTimeLimit > 0 && runMaxTime > conf.maxTimeLimit) {
                    String assertMessage = String.format("\nLimit max time = %d ms\nActual max time = %d ms\n\n", conf.maxTimeLimit, runMaxTime);
                    throw new AssertionFailedError(assertMessage + "Performance test failed, max time is greater then limit: " + run);
                }
                for (Map.Entry<Long, Long> percentile : conf.percentiles.entrySet()) {
                    long matchingCount = LongStream.of(run.runs).filter(time -> time <= percentile.getValue()).count();
                    int matchingPercents = Math.round(matchingCount * 100 / conf.repeats);
                    if (matchingPercents < percentile.getKey()) {
                        String assertMessage = "\nMax time = " + percentile.getValue() + "ms \nexpected percentile = " + percentile.getKey() +
                                "%\nActual percentile = " + matchingPercents + "%\n\n";
                        throw new AssertionFailedError(assertMessage + "Performance test failed, max time is greater then limit: " + run);
                    }
                }
            } catch (AssumptionViolatedException e) {
                eachNotifier.addFailedAssumption(e);
            } catch (Throwable e) {
                eachNotifier.addFailure(e);
            } finally {
                eachNotifier.fireTestFinished();
            }
        }

    }


}

