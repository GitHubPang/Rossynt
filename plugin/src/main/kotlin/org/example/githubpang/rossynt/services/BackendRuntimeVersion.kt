package org.example.githubpang.rossynt.services

internal enum class BackendRuntimeVersion(val majorVersion: Int, val directoryName: String) {
    DOT_NET_5(5, "net5.0"),
    DOT_NET_6(6, "net6.0"),
    DOT_NET_7(7, "net7.0"),
}
