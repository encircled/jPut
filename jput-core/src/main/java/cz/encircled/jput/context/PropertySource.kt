package cz.encircled.jput.context

import java.util.*

inline fun <reified T> getOptionalProperty(key: String): T? {
    val value = context.propertySources
            .mapNotNull { it.getProperty(key) }
            .firstOrNull()

    return if (value.isNullOrBlank()) return null
    else when (T::class) {
        Boolean::class -> value.toBoolean() as T
        Int::class -> value.toInt() as T
        Long::class -> value.toLong() as T
        else -> value as T
    }
}

inline fun <reified T> getProperty(key: String, defaultValue: T? = null): T =
        getOptionalProperty<T>(key) ?: (defaultValue ?: throw IllegalStateException("JPut property [$key] is mandatory"))

fun getCollectionProperty(key: String, defaultValue: List<String> = emptyList()): List<String> {
    val value = getProperty(key, "")

    return if (value.isEmpty()) defaultValue
    else value.split(",").map { it.trim() }
}

/**
 * Property sources for settings
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