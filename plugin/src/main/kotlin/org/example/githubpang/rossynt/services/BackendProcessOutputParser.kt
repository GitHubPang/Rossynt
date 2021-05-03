package org.example.githubpang.rossynt.services

import java.util.regex.Pattern

internal class BackendProcessOutputParser {
    internal data class ParseResult(val urlScheme: String, val serverPort: String) {
        val backendUrl: String = "$urlScheme://localhost:$serverPort"
    }

    // ******************************************************************************** //

    companion object {
        fun parseText(text: String): ParseResult? {
            val pattern = Pattern.compile("(http[s]?)://.+?:([0-9]+)")
            val matcher = pattern.matcher(text)

            if (!matcher.find()) {
                return null
            }

            val urlScheme = matcher.group(1)
            val serverPort = matcher.group(2)
            return ParseResult(urlScheme, serverPort)
        }
    }
}
