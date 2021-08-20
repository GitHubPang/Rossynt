package org.example.githubpang.rossynt.services

import com.intellij.openapi.util.text.StringUtil

internal object LineSeparatorUtil {
    fun convertLineSeparators(text: String?, newSeparator: String): String? = when (text) {
        null -> null
        else -> StringUtil.convertLineSeparators(text, newSeparator)
    }
}
