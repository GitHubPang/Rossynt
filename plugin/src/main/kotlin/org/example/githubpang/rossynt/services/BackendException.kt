package org.example.githubpang.rossynt.services

internal class BackendException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
