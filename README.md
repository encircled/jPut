[![Build Status](https://travis-ci.org/encircled/jPut.svg?branch=master)](https://travis-ci.org/encircled/jPut)
[![codecov](https://codecov.io/gh/encircled/jPut/branch/master/graph/badge.svg)](https://codecov.io/gh/encircled/jPut)

# Java Performance Unit Testing

jPuts allows using junit tests for continuous performance testing.

## Use cases

### Unit performance testing

The main goal of the unit performance testing is to ensure, that the target piece of code meets the performance requirements. 
For unit testing, jPut may use different metrics like average/max/percentile execution time. You can configure those metrics for each test, violating those constraints will cause failing the unit test.

### Performance trend testing

JPut supports persisting the execution statistics, which allows to observe the trends how performance changes in time, or compare performance between different version of an application. 

Also, it can be used for auto verifying, that performance of your application did not degrade after a particular change in the code. JPut can be configured to fail the build if performance resulsts degraded too much comparing to previous executions.

JPut might be used together with Elasticsearch and Kibana for visualizing the performance trends.

## Performance tests configuration


## Unit test configuration

A JUnit test method must be marked with `@PerformanceTest` in order to enable the performance testing. Performance unit test configuration is following:

#### Execution parameters

- *warmUp* - default 0 - count of warm up test executions, which are not counted for asserting. Recommended value is > 0
- *repeats* - count of test executions, which are counted for asserting and trend analysis
- *maxTimeLimit* - in milliseconds, default 0 ms - max execution time limit. Test is considered failed, if elapsed time of at least one execution is greater than specified value. Max time limit check is skipped, if specified value is < 1   
- *averageTimeLimit* - in milliseconds, default 0 ms - average execution time limit. Test is considered failed, if average time of all executions is greater than specified value. Average time limit check is skipped, if specified value is < 1
- *useStandardDeviationAsThreshold* can be used together with *averageTimeThreshold*, result threshold will be the sum of both

#### Error handling parameters

- *continueOnException* - default 'true' - if true, all errors thrown by the test will be handled, preventing the unit test to fail. Result will be enriched with statusCode=500 and corresponding error message.  
- *maxAllowedExceptionsCount* - default Long.MAX_VALUE - if count of errored tests is greater than this, the unit test will be marked as failed. 

#### Trend analysis parameters

- *trends* - configuration of trends testing:  
    * *sampleSize* - sample size which is used for trend analysis, i.e. only specified count of previous test executions will be used for analysis
    * *sampleSelectionStrategy* - defines the way, how the sample (a subset of results) should be chosen from the all available previous results
    * *averageTimeThreshold* - static average time threshold. Performance trend test will fail if average execution time is greater than sample average time plus given threshold
    * *useStandardDeviationAsThreshold* - if true - use the sample standard deviation as an average time threshold. Performance trend test will fail if average execution time is greater than sample average time plus threshold

#### Returning error result

In order to pass an error message from the test, unit test should return an instance of `cz.encircled.jput.model.RunResult`, like this:
```java
@Test
@PerformanceTest(...)
public void myUnitTest() {
   ...
   return new RunResult(500, "Unexpected server error")
}

```

Such run will be marked as failed and an error will be passed to result recorders (like Kibana)


## Global JPut configuration

- *jput.enabled* - boolean - enables/disables execution of performance tests
- *jput.reporter.class* - fully classified class name of custom Result Recorder
- *jput.env.custom.params* - custom parameters which will be passed to Result Recorders (for example to Kibana). Might be used for example for tested application version, environment code, name of test runner etc. Format is `key1:value1,key2:value2`, i.e. value split by `:`, multiple values separated by `,` 

#### ELK

- *jput.storage.elastic.enabled* - boolean - enables/disables elasticsearch as a Result Recorder
- *jput.storage.elastic.host* - elasticsearch server host name
- *jput.storage.elastic.port* - elasticsearch server port. Default is 80
- *jput.storage.elastic.scheme* - network scheme, e.g. http/https. Default is http
- *jput.storage.elastic.index* - elasticsearch index name to be used
- *jput.storage.elastic.env.identifiers* - this property can be use to distinguish perf results from different environments or client machine. For example when tested application is running on multiple servers each with different available resources (CPU/RAM/DISC) which may affect the results. This property will be used during trend analysis to compare results from the same environment 
- *jput.storage.elastic.cleanup.remove.after.days* - Int - Automatically delete data older than given days from Elasticsearch 

#### Filesystem

- *jput.storage.file.enabled* - boolean - enables/disables file Result Recorder
- *jput.storage.file.path* - absolute path to the file which will be used a a storeage

### Examples

Unit performance test with: 2 warm up executions, 20 repeats and 100ms max time validation

```java
@Test
@PerformanceTest(warmUp = 2, repeats = 20, maxTimeLimit = 100)
public void myUnitTest() {

}

```
Additionally trend performance test with, which validates, that performance of the method is still within the expected boundaries.
In this particular case boundaries are: average time within sample average time + it's variance 

```java
@Test
@PerformanceTest(warmUp = 2, repeats = 20, maxTimeLimit = 100, 
    trends = PerformanceTrend(useStandardDeviationAsThreshold = true))
public void myUnitAndTrendTest() {
    
}
```

## Tests result reporter

JPut provides support Allure reports. In order to enable it set property `jput.reporter.classes=cz.encircled.jput.reporter.JPutAllureReporter`.
You can have a custom reporter by implementing an interface `JPutReporter` and setting property `jput.reporter.classes=my.custom.Reporter`.

## Spring integration

Spring module provides JUnit 4 runner `@RunWith(JPutSpringRunner.class)`

## JPut vs JMeter

- Create huge load from just one machine with Reactive test executor
- Simple integration with existing CI pipelines thanks to using junit
- Test implementation flexibility. You can use any jvm language and you are not limited in how to prepare test data / execute test / assert results. All in the code.
- Re-use already existing code for your test data generation and validation
- Out of the box support for performance trend analysis
