package cz.encircled.jput

import cz.encircled.jput.io.TrendResultReader
import cz.encircled.jput.io.TrendResultWriter
import cz.encircled.jput.io.file.FileSystemResultReader
import cz.encircled.jput.io.file.FileSystemResultWriter
import cz.encircled.jput.model.CommonConstants
import cz.encircled.jput.trend.StandardSampleTrendAnalyzer
import cz.encircled.jput.trend.TrendAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzer
import cz.encircled.jput.unit.UnitPerformanceAnalyzerImpl

/**
 * @author Vlad on 21-May-17.
 */
class JPutContext private constructor() {
    val contextExecutionId: Long
    val unitPerformanceAnalyzer: UnitPerformanceAnalyzer
    val trendAnalyzer: TrendAnalyzer
    val trendResultReader: TrendResultReader
    val trendResultWriter: TrendResultWriter
    val isPerformanceTestEnabled: Boolean

    init {
        this.isPerformanceTestEnabled = getBoolPropertyOrDefault(CommonConstants.PROP_ENABLED, true)
        this.contextExecutionId = System.currentTimeMillis()
        this.unitPerformanceAnalyzer = UnitPerformanceAnalyzerImpl()
        this.trendAnalyzer = StandardSampleTrendAnalyzer()

        val pathToFile = getPropertyOrDefault(CommonConstants.PROP_PATH_TO_STORAGE_FILE, System.getProperty("java.io.tmpdir") + "jput-test.data")
        this.trendResultReader = FileSystemResultReader(pathToFile)
        this.trendResultWriter = FileSystemResultWriter(pathToFile)
    }

    private fun getProperty(key: String): String {
        return System.getProperty(key) ?: throw IllegalStateException("JPut property [$key] is mandatory")
    }

    private fun getPropertyOrDefault(key: String, defaultVal: String): String {
        return System.getProperty(key) ?: return defaultVal
    }

    private fun getBoolPropertyOrDefault(key: String, defaultVal: Boolean): Boolean {
        val value = System.getProperty(key) ?: return defaultVal
        return java.lang.Boolean.getBoolean(value)
    }

    companion object {

        val context = JPutContext()
    }

}
