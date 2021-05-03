package org.example.githubpang.rossynt.services

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class BackendProcessOutputParserResultTest internal constructor(private val urlScheme: String, private val serverPort: String, private val backendUrl: String) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Array<Any> {
            return arrayOf(
                arrayOf("https", "5001", "https://localhost:5001"),
                arrayOf("http", "5000", "http://localhost:5000"),
            )
        }
    }

    // ******************************************************************************** //

    @Test
    fun extractVersionFromLine() {
        val parseResult = BackendProcessOutputParser.ParseResult(urlScheme, serverPort)
        Assert.assertEquals(backendUrl, parseResult.backendUrl)
    }
}
