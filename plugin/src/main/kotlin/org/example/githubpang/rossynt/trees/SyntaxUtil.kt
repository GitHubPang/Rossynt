package org.example.githubpang.rossynt.trees

import org.jetbrains.annotations.Contract

internal object SyntaxUtil {
    @Contract(pure = true)
    fun isSyntaxKindError(syntaxKind: String) = syntaxKind == "SkippedTokensTrivia"
}
