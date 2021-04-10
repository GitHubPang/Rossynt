package org.example.githubpang.rossynt.services

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import org.apache.commons.lang3.StringUtils
import java.util.regex.Pattern

internal class BackendRuntimeVersionFinder(dotNetPath: String) {
    var backendRuntimeVersion: BackendRuntimeVersion
        private set

    // ******************************************************************************** //

    init {
        val processOutput = ExecUtil.execAndGetOutput(GeneralCommandLine(dotNetPath, "--list-runtimes"))

        var highestBackendRuntimeVersion: BackendRuntimeVersion? = null
        processOutput.stdoutLines.forEach { stdoutLine ->
            val backendRuntimeVersion = extractVersionFromLine(stdoutLine) ?: return@forEach

            if (highestBackendRuntimeVersion == null || highestBackendRuntimeVersion!!.majorVersion < backendRuntimeVersion.majorVersion) {
                highestBackendRuntimeVersion = backendRuntimeVersion
            }
        }

        backendRuntimeVersion = highestBackendRuntimeVersion ?: throw IllegalStateException() //todo
    }

    companion object {
        fun extractVersionFromLine(line: String): BackendRuntimeVersion? {
            val pattern = Pattern.compile("^Microsoft.AspNetCore.App ([^ ]+) .*$")

            val matcher = pattern.matcher(line)
            if (!matcher.matches()) {
                return null
            }

            val version = matcher.group(1)
            val majorVersion = StringUtils.split(version, '.')[0].toInt()

            return BackendRuntimeVersion.values().find { majorVersion == it.majorVersion }
        }
    }
}
