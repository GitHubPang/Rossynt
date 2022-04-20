package org.example.githubpang.rossynt.events

import com.intellij.openapi.Disposable
import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext

internal class TextEventThrottler : Disposable {
    private val scope = CoroutineScope(EmptyCoroutineContext + Job())
    private var job: Job? = null
    private var lastText: String? = null
    private var callback: ITextEventThrottlerCallback? = null

    // ******************************************************************************** //

    fun setCallback(callback: ITextEventThrottlerCallback?) {
        this.callback = callback
    }

    override fun dispose() {
        reset()
        scope.cancel()
    }

    fun reset() {
        job?.cancel()
        job = null
    }

    fun queueEvent(text: String) {
        lastText = text

        job?.cancel()
        job = scope.launch(Dispatchers.IO) {
            try {
                delay(1000)
            } catch (e: CancellationException) {
                return@launch
            }

            launch(Dispatchers.Main) innerLaunch@{
                if (job == null) {
                    return@innerLaunch
                }

                val lastText = lastText ?: return@innerLaunch
                this@TextEventThrottler.lastText = null

                job?.cancel()
                job = null

                callback?.onTextEvent(lastText)
            }
        }
    }
}
