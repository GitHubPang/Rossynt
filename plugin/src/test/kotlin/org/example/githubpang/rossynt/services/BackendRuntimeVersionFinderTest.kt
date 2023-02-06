package org.example.githubpang.rossynt.services

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class BackendRuntimeVersionFinderTest internal constructor(private val line: String, private val backendRuntimeVersion: BackendRuntimeVersion?) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Array<Any> {
            return arrayOf(
                arrayOf("""Microsoft.AspNetCore.App 3.1.9 [C:\Users\User\.dotnet\shared\Microsoft.AspNetCore.App]""", null),
                arrayOf("""Microsoft.AspNetCore.App 5.0.1 [C:\Users\User\.dotnet\shared\Microsoft.AspNetCore.App]""", BackendRuntimeVersion.DOT_NET_5),
                arrayOf("""Microsoft.AspNetCore.App 6.0.1 [C:\Program Files\dotnet\shared\Microsoft.AspNetCore.App]""", BackendRuntimeVersion.DOT_NET_6),
                arrayOf("""Microsoft.AspNetCore.App 7.0.1 [/usr/local/share/dotnet/shared/Microsoft.AspNetCore.App]""", BackendRuntimeVersion.DOT_NET_7),
                arrayOf("""Microsoft.NETCore.App 3.1.9 [C:\Users\User\.dotnet\shared\Microsoft.NETCore.App]""", null),
                arrayOf("""Microsoft.NETCore.App 5.0.1 [C:\Users\User\.dotnet\shared\Microsoft.NETCore.App]""", null),
                arrayOf("""Microsoft.NETCore.App 6.0.1 [C:\Program Files\dotnet\shared\Microsoft.NETCore.App]""", null),
                arrayOf("""Microsoft.NETCore.App 7.0.1 [/usr/local/share/dotnet/shared/Microsoft.NETCore.App]""", null),
                arrayOf("""Microsoft.WindowsDesktop.App 3.1.9 [C:\Users\User\.dotnet\shared\Microsoft.WindowsDesktop.App]""", null),
                arrayOf("""Microsoft.WindowsDesktop.App 5.0.1 [C:\Users\User\.dotnet\shared\Microsoft.WindowsDesktop.App]""", null),
                arrayOf("""Microsoft.WindowsDesktop.App 6.0.1 [C:\Program Files\dotnet\shared\Microsoft.WindowsDesktop.App]""", null),
            )
        }
    }

    // ******************************************************************************** //

    @Test
    fun extractVersionFromLine() {
        Assert.assertEquals(backendRuntimeVersion, BackendRuntimeVersionFinder.extractVersionFromLine(line))
    }
}
