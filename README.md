# Java Performance Unit Testing

jPuts allows using junit tests for continuous performance testing. Time accuracy is in milliseconds.  

## Use cases

### Unit performance testing

The main goal of unit performance testing is to ensure that target code meets performance requirements. JPUT supports following metrics:
 - average execution time  
 - max execution time
 - percentile max execution time

### Trend performance testing

JPUT measures and saves tests execution statistics which allows to verify that performance of target method is not below previous results. Trend performance testing supported the same metric:  
 - average execution time  
 - max execution time
 - percentile max execution time

## Configuration

JUnit test method can be marked with `@PerformanceTest` in order to enable performance testing. Performance test configuration:

- *warmUp* - default 0 - count of warm up test executions, which wont be counted. Recommended value is > 0
- *repeats* - milliseconds, default 1 ms - count of test executions, which are counted
- *maxTimeLimit* - milliseconds, default 0 ms - maximal execution time limit. Test is failed, if at least one execution time is greater than specified value. 
Max time limit check is skipped if specified value is < 1   
- *averageTimeLimit* - milliseconds, default 0 ms - average execution time limit. Test is failed, if average execution time of all repeats is greater than specified value.
Average time limit check is skipped if specified value is < 1

#### Examples
```java
@Test
@PerformanceTest(warmUp = 10, repeats = 20, maxTimeLimit = 100)
public void myUnitTest() {
    
}
```

## Spring integration

`@RunWith(JPutSpringRunner.class)` allows running spring unit tests with jPuts