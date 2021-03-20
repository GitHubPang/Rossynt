package org.jetbrains.plugins.template.services

import com.intellij.openapi.Disposable
import org.example.githubpang.GlobalConstant
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MyApplicationService : Disposable {
    init {
        initBackend()
    }

    override fun dispose() {
        shutdownBackend()
    }

    private fun initBackend() {
        //pang do in background thread. Co-routine?
        //pang try catch?
        // Copy backend to temp path.
        val outPath = GlobalConstant.TEMP_PATH
        GlobalConstant.RESOURCE_BACKEND_FILE_NAME_ARRAY.forEach { fileName ->
            val inFile = GlobalConstant.RESOURCE_BACKEND_PATH + fileName
            val outFile = File(outPath.toFile(), fileName)
            javaClass.getResourceAsStream(inFile).use { inputStream: InputStream? ->
                if (inputStream == null) {
                    return@use//pang to handle open stream failed
                }

                FileOutputStream(outFile).use { outputStream: FileOutputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }

    private fun shutdownBackend() {
        try {
            val outPath = GlobalConstant.TEMP_PATH
            outPath.toFile().deleteRecursively()
        } catch (e: Exception) {
            //pang write log?
        }
    }
}
