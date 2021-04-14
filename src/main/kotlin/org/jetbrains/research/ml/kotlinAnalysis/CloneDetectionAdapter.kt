package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.psi.KtNamedFunction
import java.math.BigInteger
import java.security.MessageDigest

/**
 * Adapter from Kotlin function to SourcererCC format.
 */
object CloneDetectionAdapter {

    fun format(function: KtNamedFunction, projectId: Int, methodId: Int): String {
        PsiProvider.deleteComments(function)
        val tokens: List<String> = tokenize(function.text)
        val totalTokens = tokens.size
        val tokensCounter: Map<String, Int> = tokens.groupingBy { it }
            .eachCount()
        val uniqueTokens = tokensCounter.keys.size
        val hash: String = md5Hash(function.text)

        val statsResult = listOf(
            projectId.toString(),
            methodId.toString(),
            totalTokens.toString(),
            uniqueTokens.toString(),
            hash
        ).joinToString(separator = ",", postfix = "@#@")

        val tokensResult =
            tokensCounter.entries.joinToString(separator = ",") { entry -> "${entry.key}@@::@@${entry.value}" }

        return statsResult + tokensResult
    }

    private fun tokenize(functionText: String): List<String> {
        val delimiters = ";.[]()~!-+&*/%<>^|?{}=#,\"\\:$'`@ \n".toCharArray()
        return functionText.split(*delimiters).filter { it.isNotEmpty() }
    }

    private fun md5Hash(input: String): String {
        // https://stackoverflow.com/questions/64171624/what-is-the-best-way-to-generate-an-md5-hash-in-kotlin
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray()))
            .toString(16)
            .padStart(32, '0')
    }
}
