package org.example.githubpang.rossynt.events

import kotlinx.coroutines.*

internal class TextEventThrottler {
    private var job: Job? = null
    private var lastText: String? = null
    private var callback: ITextEventThrottlerCallback? = null

    // ******************************************************************************** //

    fun setCallback(callback: ITextEventThrottlerCallback?) {
        this.callback = callback
    }

    fun reset() {
        job?.cancel()
        job = null
    }

    fun queueEvent(text: String) {
        lastText = text

        if (job != null) {
            return
        }
        job = GlobalScope.launch(Dispatchers.IO) {
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
                job = null

                callback?.onTextEvent(lastText)
            }
        }
    }
}
