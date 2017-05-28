package cz.encircled.jput.spring;

import cz.encircled.jput.JPutContext;
import cz.encircled.jput.model.MethodConfiguration;
import cz.encircled.jput.model.PerformanceTestRun;
import cz.encircled.jput.trend.TrendAnalyzer;
import cz.encircled.jput.unit.PerformanceTest;
import cz.encircled.jput.unit.UnitPerformanceAnalyzer;
import cz.encircled.jput.unit.UnitPerformanceResult;
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

/**
 * @author Vlad on 20-May-17.
 */
public class JPutSpringRunner extends SpringJUnit4ClassRunner {

    private UnitPerformanceAnalyzer unitAnalyzer = JPutContext.getContext().getUnitPerformanceAnalyzer();

    private TrendAnalyzer trendAnalyzer = JPutContext.getContext().getTrendAnalyzer();

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
        if (annotation == null || !JPutContext.getContext().isPerformanceTestEnabled()) {
            super.runChild(frameworkMethod, notifier);
            return;
        }

        if (isTestMethodIgnored(frameworkMethod)) {
            notifier.fireTestIgnored(description);
        } else {
            MethodConfiguration conf = MethodConfiguration.fromAnnotation(annotation);
            PerformanceTestRun run = unitAnalyzer.buildRun(conf, frameworkMethod.getMethod());

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
                    unitAnalyzer.addRun(run, System.currentTimeMillis() - start);
                }

                UnitPerformanceResult result = unitAnalyzer.analyzeUnitTrend(run, conf);
                if (result.isError()) {
                    throw new AssertionFailedError("unit performance test failed" + unitAnalyzer.buildErrorMessage(result, conf));
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

