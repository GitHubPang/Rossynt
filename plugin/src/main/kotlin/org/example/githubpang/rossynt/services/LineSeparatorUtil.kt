package org.example.githubpang.rossynt.services

import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.annotations.Contract

internal object LineSeparatorUtil {
    @Contract(pure = true)
    fun convertLineSeparators(text: String?, newSeparator: String): String? = when (text) {
        null -> null
        else -> StringUtil.convertLineSeparators(text, newSeparator)
    }

    @Contract(pure = true)
    fun convertOffset(offset: Int, text: String?, oldSeparator: String, newSeparator: String): Int {
        return text?.substring(0, offset)?.replace(oldSeparator, newSeparator)?.length ?: offset
    }
}
