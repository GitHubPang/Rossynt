package org.example.githubpang.rossynt.services

import java.io.File

internal object RossyntUtil {
    fun isCSFile(filePath: String) = File(filePath).extension.equals("cs", true)
}
