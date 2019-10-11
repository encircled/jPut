package cz.encircled.jput.context

import cz.encircled.jput.model.PerfTestExecution
import cz.encircled.jput.model.SuiteConfiguration
import cz.encircled.jput.recorder.ElasticsearchClientWrapper
import cz.encircled.jput.recorder.ElasticsearchResultRecorder
import cz.encircled.jput.recorder.FileSystemResultRecorder
import cz.encircled.jput.recorder.ResultRecorder
import cz.encircled.jput.reporter.JPutConsoleReporter
import cz.encircled.jput.reporter.JPutReporter
import cz.encircled.jput.trend.SampleBasedTrendAnalyzer
import cz.encircled.jput.trend.TrendAnalyzer
import cz.encircled.jput.unit.TestExceptionsAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzerImpl
import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

lateinit var context: JPutContext

/**
 *
 * @author Vlad on 21-May-17.
 */
class JPutContext {

    private val log = LoggerFactory.getLogger(JPutContext::class.java)

    /**
     * Unique ID of current tests execution
     */
    val executionId: Long by lazy {
        System.currentTimeMillis()
    }

    /**
     * Global enabled/disabled switch
     */
    val isPerformanceTestEnabled
        get() = getProperty(PROP_ENABLED, true)

    var propertySources = mutableListOf(SystemPropertySource(), ClasspathFilePropertySource())

    /**
     * Test results recorders
     */
    val resultRecorders = mutableListOf<ResultRecorder>()

    /**
     * customTestId -> defaultTestId
     */
    val customTestIds = mutableMapOf<String, String>()

    // TODO remove after JPut is refactored
    val testExecutions: MutableMap<String, PerfTestExecution> = ConcurrentHashMap()

    lateinit var unitPerformanceAnalyzers: List<UnitPerformanceAnalyzer>
    lateinit var trendAnalyzer: TrendAnalyzer

    /**
     * Currently running class suite, should be set by [org.junit.runner.Runner]
     */
    var currentSuite: SuiteConfiguration? = null

    /**
     * Currently running method of a suite, should be set by [org.junit.runner.Runner]
     */
    var currentSuiteMethod: Method? = null

    var resultReporters = mutableListOf<JPutReporter>(JPutConsoleReporter())

    fun init() {
        unitPerformanceAnalyzers = listOf(UnitPerformanceAnalyzerImpl(), TestExceptionsAnalyzer())
        trendAnalyzer = SampleBasedTrendAnalyzer()

        getProperty(PROP_REPORTER_CLASS, "").let {
            if (it.isNotEmpty()) {
                resultReporters.add(Class.forName(it).getConstructor().newInstance() as JPutReporter)
            }
        }

        initECSRecorder()
        initFileSystemRecorder()
    }

    private fun initFileSystemRecorder() {
        if (getProperty(PROP_STORAGE_FILE_ENABLED, false)) {

            val default = System.getProperty("java.io.tmpdir") + "jput-test.data"
            val pathToFile = getProperty(PROP_PATH_TO_STORAGE_FILE, default)

            log.info("JPut is using Filesystem recorder: $pathToFile")
            resultRecorders.add(FileSystemResultRecorder(pathToFile))
        }
    }

    private fun initECSRecorder() {
        if (getProperty(PROP_ELASTIC_ENABLED, false)) {
            val host = getProperty<String>(PROP_ELASTIC_HOST)
            val port = getProperty(PROP_ELASTIC_PORT, 80)
            val scheme = getProperty(PROP_ELASTIC_SCHEME, "http")

            log.info("JPut is using Elasticsearch recorder: $host")

            val client = ElasticsearchClientWrapper(RestClient.builder(HttpHost(host, port, scheme)))
            val elastic = ElasticsearchResultRecorder(client)
            elastic.doCleanup()
            resultRecorders.add(elastic)
        }
    }

    fun destroy() {
        resultRecorders.forEach {
            try {
                it.flush()
            } catch (e: Exception) {
                log.warn("Failed to flush results", e)
            }
            try {
                it.destroy()
            } catch (e: Exception) {
                log.warn("Failed to close the recorder", e)
            }
        }
    }

    fun addPropertySource(source: PropertySource, index: Int = 0) {
        propertySources.add(index, source)
    }

    companion object {

        private const val PREFIX = "jput."

        /**
         * Enables/disables execution of performance tests
         */
        const val PROP_ENABLED = PREFIX + "enabled"

        /**
         * Fully classified class name of custom Result Recorder
         */
        const val PROP_REPORTER_CLASS = PREFIX + "reporter.class"

        /**
         * Boolean - enables/disables elasticsearch as a Result Recorder
         */
        const val PROP_ELASTIC_ENABLED = PREFIX + "storage.elastic.enabled"

        /**
         * Elasticsearch server host name
         */
        const val PROP_ELASTIC_HOST = PREFIX + "storage.elastic.host"

        /**
         * Elasticsearch server port
         */
        const val PROP_ELASTIC_PORT = PREFIX + "storage.elastic.port"

        /**
         * Network scheme, e.g. http/https
         */
        const val PROP_ELASTIC_SCHEME = PREFIX + "storage.elastic.scheme"

        /**
         * Elasticsearch index name to be used
         */
        const val PROP_ELASTIC_INDEX = PREFIX + "storage.elastic.index"

        /**
         * This property can be use to distinguish perf results from different environments or client machine.
         * For example when tested application is running on multiple servers each with different available resources
         * (CPU/RAM/DISC) which may affect the results. This property will be used during trend analysis to compare results from the same environment.
         */
        const val PROP_ELASTIC_ENV_IDENTIFIERS = PREFIX + "storage.elastic.env.identifiers"

        /**
         * Automatically delete data older than given days from Elasticsearch
         */
        const val PROP_ELASTIC_CLEANUP_DAYS = PREFIX + "storage.elastic.cleanup.remove.after.days"

        /**
         * Boolean - enables/disables file Result Recorder
         */
        const val PROP_STORAGE_FILE_ENABLED = PREFIX + "storage.file.enabled"

        /**
         * Absolute path to the file which will be used a a storeage
         */
        const val PROP_PATH_TO_STORAGE_FILE = PREFIX + "storage.file.path"

        /**
         * Custom parameters which will be passed to Result Recorders (for example to Kibana).
         * Might be used for example for tested application version, environment code, name of test runner etc. Format is `key1:value1,key2:value2`, i.e. value split by `:`, multiple values separated by `,`.
         */
        const val PROP_ENV_PARAMS = PREFIX + "env.custom.params"

        const val PROP_TEST_CONFIG = PREFIX + "config.test."

    }

}
