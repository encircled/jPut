# Java Performance Unit Testing

jPuts allows using junit tests for continuous performance testing.

## Use cases

### Unit performance testing

The main goal of the unit performance testing is to ensure, that the target piece of code meets the performance requirements. For unit testing, jPut uses average and max execution time metrics.

### Performance trend testing

JPut supports persisting the execution statistics, which allows to trace the changes in performance in time. It can be used for asserting that performance of your application did not degrade after a particular change in the code. Also, it might be used together with Elasticsearch and Kibana for visualazing those performance trends.

## Configuration

A JUnit test method can be marked with `@PerformanceTest` in order to enable the performance testing. Performance unit test configuration is following:

- *warmUp* - default 0 - count of warm up test executions, which are not counted for asserting. Recommended value is > 0
- *repeats* - count of test executions, which are counted for asserting and trend analysis
- *maxTimeLimit* - in milliseconds, default 0 ms - max execution time limit. Test is considered failed, if elapsed time of at least one execution is greater than specified value. Max time limit check is skipped, if specified value is < 1   
- *averageTimeLimit* - in milliseconds, default 0 ms - average execution time limit. Test is considered failed, if average time of all executions is greater than specified value. Average time limit check is skipped, if specified value is < 1

#### Examples

Unit performance test with: 2 warm up executions, 20 repeats and 100ms max time validation

```java
@Test
@PerformanceTest(warmUp = 2, repeats = 20, maxTimeLimit = 100)
public void myUnitTest() {
    
}
```

## Spring integration

`@RunWith(JPutSpringRunner.class)` allows running spring unit tests with jPuts
