package org.example.githubpang.rossynt.services

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class BackendProcessOutputParserTest internal constructor(private val line: String, private val urlScheme: String?, private val serverPort: String?) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Array<Any> {
            return arrayOf(
                // .NET Core 2.1
                arrayOf("""Hosting environment: Development""", null, null),
                arrayOf("""Content root path: C:\Rossynt\backend\RossyntBackend""", null, null),
                arrayOf("""Now listening on: https://localhost:5001""", "https", "5001"),
                arrayOf("""Now listening on: http://localhost:5000""", "http", "5000"),
                arrayOf("""Application started. Press Ctrl+C to shut down.""", null, null),

                // .NET Core 3.1
                arrayOf("""info: Microsoft.Hosting.Lifetime[0]""", null, null),
                arrayOf("""      Now listening on: https://localhost:5001""", "https", "5001"),
                arrayOf("""info: Microsoft.Hosting.Lifetime[0]""", null, null),
                arrayOf("""      Now listening on: http://localhost:5000""", "http", "5000"),
                arrayOf("""info: Microsoft.Hosting.Lifetime[0]""", null, null),
                arrayOf("""      Application started. Press Ctrl+C to shut down.""", null, null),
                arrayOf("""info: Microsoft.Hosting.Lifetime[0]""", null, null),
                arrayOf("""      Hosting environment: Development""", null, null),
                arrayOf("""info: Microsoft.Hosting.Lifetime[0]""", null, null),
                arrayOf("""      Content root path: C:\Rossynt\backend\RossyntBackend""", null, null),

                // .NET 5.0
                arrayOf("""info: Microsoft.Hosting.Lifetime[0] Now listening on: https://localhost:5001""", "https", "5001"),
                arrayOf("""info: Microsoft.Hosting.Lifetime[0] Now listening on: http://localhost:5000""", "http", "5000"),
                arrayOf("""info: Microsoft.Hosting.Lifetime[0] Application started. Press Ctrl+C to shut down.""", null, null),
                arrayOf("""info: Microsoft.Hosting.Lifetime[0] Hosting environment: Development""", null, null),
                arrayOf("""info: Microsoft.Hosting.Lifetime[0] Content root path: C:\Rossynt\backend\RossyntBackend""", null, null),
            )
        }
    }

    // ******************************************************************************** //

    @Test
    fun extractVersionFromLine() {
        val parseResult = BackendProcessOutputParser.parseText(line)
        Assert.assertNotEquals(urlScheme, parseResult?.urlScheme)
        Assert.assertEquals(serverPort, parseResult?.serverPort)
    }
}
