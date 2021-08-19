package org.example.githubpang.rossynt.services

import com.intellij.openapi.util.text.StringUtil
import java.io.File

internal object RossyntUtil {
    fun isCSFile(filePath: String) = File(filePath).extension.equals("cs", true)

    fun convertLineSeparators(text: String?, newSeparator: String): String? = when (text) {
        null -> null
        else -> StringUtil.convertLineSeparators(text, newSeparator)
    }
}
