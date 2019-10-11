package cz.encircled.jput

import cz.encircled.jput.model.ExecutionRun

object JPutUtils {

    internal fun buildErrorMessage(repeat: ExecutionRun): String {
        val details = repeat.resultDetails
        var errorMsg = details.errorMessage ?: ""
        if (details.error != null && details.error.message != details.errorMessage) errorMsg += ". ${details.error.message}"

        return errorMsg
    }

}