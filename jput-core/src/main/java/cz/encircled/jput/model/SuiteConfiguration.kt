package cz.encircled.jput.model

import cz.encircled.jput.annotation.PerformanceSuite

data class SuiteConfiguration(

        val clazz: Class<*>,

        val isParallel: Boolean = false

) {

    companion object {

        fun fromAnnotation(clazz: Class<*>, annotation: PerformanceSuite?): SuiteConfiguration {
            return if (annotation != null) SuiteConfiguration(clazz, annotation.parallel)
            else SuiteConfiguration(clazz)
        }

    }

}

