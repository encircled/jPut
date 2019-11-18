package cz.encircled.jput.context

import java.util.*

internal inline fun <reified T> getOptionalProperty(key: String): T? {
    val value = removeWhitespaces(context.propertySources
            .mapNotNull { it.getProperty(key) }
            .firstOrNull())

    return if (value.isNullOrBlank()) return null
    else when (T::class) {
        Boolean::class -> value.toBoolean() as T
        Int::class -> value.toInt() as T
        Long::class -> value.toLong() as T
        else -> value as T
    }
}

internal fun removeWhitespaces(str: String?): String? =
        (str as CharSequence?)?.filter { !it.isWhitespace() }?.toString()

internal inline fun <reified T> getProperty(key: String, defaultValue: T? = null): T =
        getOptionalProperty<T>(key) ?: (defaultValue ?: throw IllegalStateException("JPut property [$key] is mandatory"))

internal fun getCollectionProperty(key: String, defaultValue: List<String> = emptyList()): List<String> {
    val value = getProperty(key, "")

    return if (value.isEmpty()) defaultValue
    else value.split(",")
}

/**
 * Supports only Int to Long, should be re-designed if more types are needed in future
 */
internal fun getOptionalMapProperty(key: String): Map<Double, Long>? {
    return getOptionalProperty<String>(key)
            ?.split(",")
            ?.associate {
                it.split(":").let { s -> s[0].toInt().toPercentile() to s[1].toLong() }
            }
}

/**
 * Property source for JPut settings
 */
interface PropertySource {

    fun getProperty(key: String): String?

}

/**
 * Uses System.property and System.env as a sources
 */
class SystemPropertySource : PropertySource {

    override fun getProperty(key: String): String? = System.getProperty(key) ?: System.getenv()[key]

}

class ClasspathFilePropertySource : PropertySource {

    private val prop = Properties()

    init {
        this.javaClass.classLoader.getResourceAsStream("jput.properties").use {
            if (it != null) prop.load(it)
        }
    }

    override fun getProperty(key: String): String? = prop.getProperty(key)

}