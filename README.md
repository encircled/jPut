[![Build Status](https://travis-ci.org/encircled/jPut.svg?branch=master)](https://travis-ci.org/encircled/jPut)
[![codecov](https://codecov.io/gh/encircled/jPut/branch/master/graph/badge.svg)](https://codecov.io/gh/encircled/jPut)

# Java Performance Unit Testing

JPut allows implementing continuous and load performance testing completely as a code (any JVM language) using JUnit tests. 

* [Use cases](#use-cases)
* [Quick start](#quick-start)
* [Performance tests configuration](#performance-test-configuration)
* [Property based configuration](#property-based-configuration)
* [Storage types](#storage-types)
* [Property sources](#property-sources)
* [Global config parameters](#global-jput-configuration)
* [Spring integration](#spring-integration)
* [Why not XYZ?](#why-not-xyz)

## Use cases

#### Unit performance testing

The main goal of the unit performance testing is to ensure, that the target piece of code meets the performance requirements. 
For unit testing, jPut may use different metrics like average/max/percentile execution time. You can configure those metrics for each test, violating those constraints will cause failing the unit test.

#### Performance trend testing

JPut supports persisting the execution statistics, which allows to observe the trends how performance changes in time, or compare performance between different version of an application. 

Also, it can be used for auto verifying, that performance of your application did not degrade after a particular change in the code. JPut can be configured to fail the build if performance resulsts degraded too much comparing to previous executions.

JPut might be used together with Elasticsearch and Kibana for visualizing the performance trends.

#### Load testing

You may simulate a big load using JPut as well. In this case, it is recommended to use Reactive test executor, which allows to create a bigger load using the same amount of hardware resources comparing to standard blocking implementation.  


## Quick start

JPut uses JUnit tests, so the class and functions/methods structure is very similar. Examples here are written in java, but you can use any JVM language! 

The test class must use `JPutJUnit4Runner` JUnit runner, like:

```java
import org.junit.runner.RunWith;

@RunWith(JPutJUnit4Runner.class)
class MyPerformanceTests {

    // ...

}
``` 

Behaviour of common JUnit methods like `BeforeEach`, `BeforeClass` etc is preserved. `BeforeEach` and `AfterEach` is executed exactly once, even if a perf test has multiple `repeats` or `warmUps`. 

The performance test itself must be additionally annotated with `@PerformanceTest`, which contains test configuration parameters (unless configured via properties as described [here](#property-based-configuration)).
Complete set of parameters will be described in a moment. Here are a few examples.

The performance test with 2 warm up executions, 20 repeats and 100ms max time validation looks like

```java
@Test
@PerformanceTest(warmUp = 2, repeats = 20, maxTimeLimit = 100)
public void myUnitTest() {
    // Do the job...
}
```

The next test contains configuration for trend analysis as well, which compares execution results with previous executions.

 According to the following config, JPut will compare the execution result with 30 previous executions and will validate that the average execution time is within (standard deviation + 20)

```java
@Test
@PerformanceTest(warmUp = 2, repeats = 20, maxTimeLimit = 100, 
    trends = PerformanceTrend(sampleSize = 30, averageTimeThreshold = 20, useStandardDeviationAsThreshold = true))
public void myUnitAndTrendTest() {
    // Do the job...
}
```

Trend analysis requires a configured data storage as described [here](#storage-types)

## Performance test configuration

This section describes all possible test configuration parameters, which can be set via `@PerformanceTest` or properties.

#### Execution parameters

- *warmUp* - default 0 - count of warm up test executions, which are not counted for asserting. Recommended value is > 0
- *repeats* - count of test executions, which are counted for asserting and trend analysis
- *maxTimeLimit* - in milliseconds, default 0 ms - max execution time limit. Test is considered failed, if elapsed time of at least one execution is greater than specified value. Max time limit check is skipped, if specified value is < 1   
- *averageTimeLimit* - in milliseconds, default 0 ms - average execution time limit. Test is considered failed, if average time of all executions is greater than specified value. Average time limit check is skipped, if specified value is < 1
- *rampUp* - ramp-up in milliseconds. If parallel is 100, and the ramp-up period is 100000 (100 seconds), then JPut will take 100 seconds to get all 100 threads running, i.e. 1 second delay after each new thread
- *percentiles* - upper limits for test execution time in milliseconds within defined percentiles. Defining multiple percentiles allows to have multiple validation constraints, for instance [200ms for 75%] and [500ms for 95%]. If configured via property, is must look like `75=200, 95=500`

#### Trend analysis parameters

- *trends* - configuration of trends testing:  
    * *sampleSize* - sample size which is used for trend analysis, i.e. only specified count of previous test executions will be used for analysis
    * *sampleSelectionStrategy* - defines the way, how the sample (a subset of results) should be chosen from the all available previous results
    * *averageTimeThreshold* - static average time threshold. Performance trend test will fail if average execution time is greater than sample average time plus given threshold
    * *useStandardDeviationAsThreshold* - if true - use the sample standard deviation as an average time threshold. Performance trend test will fail if average execution time is greater than sample average time plus threshold. can be used together with *averageTimeThreshold*, result threshold will be the sum of both

#### Error handling parameters

- *continueOnException* - default 'true' - if true, all errors thrown by the test will be handled, preventing the unit test to fail. Result will be enriched with statusCode=500 and corresponding error message.  
- *maxAllowedExceptionsCount* - default Long.MAX_VALUE - if count of errored tests is greater than this, the unit test will be marked as failed. 


### Property based configuration

Performance tests configuration can be located in properties instead of annotations. It allows to have configuration dynamic, for example dependant on profile/environment.
Each configuration attribute present in `PerformanceTest` can be instead defined as
`jput.config.test.${testId}.${parameter}=${value}`, where:
- `testId` is test id (if set in `PerformanceTest`) or `HoldingClassName#methodName` by default
- `parameter` is the name of a configuration parameter, for example `repeats`
- `value` is the value of a configuration parameter

Collection data types must be delimited by `,` like `value1, value2`, 
key->value data types must be delimited by `,` and `=`, like: `key1=value1, key2=value2`  
 
 See [Property source](#property-sources) which describes how to setup properties for JPut.  


## Storage types

As a storage for test results, JPut supports Elasticsearch or Filesystem. The main advantage of Elasticsearch is that it can be used with Kibana for visualizing the data.
At least one storage is mandatory for trend analysis.

Configuration of both is described [here](#global-jput-configuration)

## Global JPut configuration

See [Property source](#property-sources) which describes how to setup properties for JPut.

- *jput.enabled* - boolean - enables/disables execution of performance tests
- *jput.reporter.class* - fully classified class name of custom Result Recorder
- *jput.env.custom.params* - custom parameters which will be passed to Result Recorders (for example to Kibana). Might be used for example for tested application version, environment code, name of test runner etc. Format is `key1:value1,key2:value2`, i.e. value split by `:`, multiple values separated by `,` 

#### ELK

- *jput.storage.elastic.enabled* - boolean - enables/disables elasticsearch as a Result Recorder
- *jput.storage.elastic.host* - elasticsearch server host name
- *jput.storage.elastic.port* - elasticsearch server port. Default is 80
- *jput.storage.elastic.scheme* - network scheme, e.g. http/https. Default is http
- *jput.storage.elastic.index* - elasticsearch index name to be used
- *jput.storage.elastic.env.identifiers* - this property can be use to distinguish perf results from different environments or client machine. For example when tested application is running on multiple servers each with different available resources (CPU/RAM/DISC) which may affect the results. 
This property will be used during trend analysis to compare results from the same environment. Format is: list of property names, separated by comma. Property values must be provided using *jput.env.custom.params* property.  
- *jput.storage.elastic.cleanup.remove.after.days* - Int - Automatically delete data older than given days from Elasticsearch 

#### Filesystem

- *jput.storage.file.enabled* - boolean - enables/disables file Result Recorder
- *jput.storage.file.path* - absolute path to the file which will be used a a storeage


## Property sources

By default, JPut will lookup for properties in environment variables, java system variables and in classpath file `jput.properties` if present.
It is possible to add a custom property source using `JPutContext.context.addPropertySource` method. 

## Tests result reporter

JPut provides support Allure reports. In order to enable it set property `jput.reporter.classes=cz.encircled.jput.reporter.JPutAllureReporter`.
You can have a custom reporter by implementing an interface `JPutReporter` and setting property `jput.reporter.classes=my.custom.Reporter`.



## Test execution result and errors

By default, JPut catches all the thrown exception and marks the test as failed. Test which ended without an exception is considered as successful.

It is possible to explicitly specify the test result by returning an instance of `cz.encircled.jput.model.RunResult`, like this:

```java
@Test
@PerformanceTest(...)
public RunResult myUnitTest() {
   // ...
   return new RunResult(403, "Unexpected access error")
}
```

Such run will be marked as failed and an error will be passed to the result recorders (like Kibana)


## Spring integration

Spring module provides JUnit 4 runner `@RunWith(JPutSpringRunner.class)`. 
It allows to use any Spring features in the tests and will automatically attach the Spring as a property source for JPut.

## Why not XYZ?

### JPut vs JMeter

- Create huge load from just one machine with Reactive test executor
- Simple integration with existing CI pipelines thanks to using junit
- Test implementation flexibility. You can use any jvm language and you are not limited in how to prepare test data / execute test / assert results. All in the code.
- Re-use already existing code for your test data generation and validation
- Out of the box support for performance trend analysis

### JPut vs Gatling

- You have even more flexibility in how you write the performance tests
- JPut supports tests written in any JVM language
- Create huge load from just one machine with Reactive test executor
- Out of the box support for performance trend analysis