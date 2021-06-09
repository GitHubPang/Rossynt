package org.example.githubpang.rossynt.services

internal interface IRossyntService {
    fun onCurrentFilePathUpdated(filePath: String?)
    fun onFindNodeAtCaretResult(nodeId: String?)
}
