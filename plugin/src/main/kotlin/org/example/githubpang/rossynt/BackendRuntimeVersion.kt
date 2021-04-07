package org.example.githubpang.rossynt

internal enum class BackendRuntimeVersion(val majorVersion: Int, val directoryName: String) {
    DOT_NET_CORE_2(2, "netcoreapp2.1"),
    DOT_NET_CORE_3(3, "netcoreapp3.1"),
    DOT_NET_5(5, "net5.0")
}
