package org.jetbrains.research.ml.kotlinAnalysis

import org.jetbrains.kotlin.psi.KtCallExpression

/**
 * Controller for stack of blocks in build.gradle.kts file, which controls the blocks [GradleBlock] order according to
 * psi tree path order.
 */
object KtsGradleBlockContextController :
    PsiContextControllerImpl<GradleBlockContext, KtCallExpression>
        (KtCallExpression::class.java) {

    private fun getGradleBlock(psiElement: KtCallExpression): GradleBlock? {
        return psiElement.calleeExpression?.text?.let { GradleBlock.fromSimpleName(it) }
    }

    override fun openContext(psiElement: KtCallExpression, context: GradleBlockContext) {
        getGradleBlock(psiElement)?.let { context.blocksStack.add(it) }
    }

    override fun closeContext(psiElement: KtCallExpression, context: GradleBlockContext) {
        getGradleBlock(psiElement)?.let {
            assert(context.blocksStack.isNotEmpty()) { "Can not close context due to empty context stack" }
            assert(context.blocksStack.last() == it) { "Can not close context due to invalid context stack" }
            context.blocksStack.removeLast()
        }
    }
}

/**
 * Analyser for gradle dependency which parse [GradleDependency] form [KtCallExpression] inside
 * [GradleBlock.DEPENDENCIES] block.
 */
object KtsGradleDependencyAnalyzer :
    PsiAnalyzerWithContextImpl<GradleBlockContext, KtCallExpression, GradleDependency?>
        (KtCallExpression::class.java) {

    override fun analyze(psiElement: KtCallExpression, context: GradleBlockContext): GradleDependency? {
        return if (context.blocksStack.contains(GradleBlock.DEPENDENCIES)) {
            val dependency = GradleFileUtil.parseGradleDependencyFromString(psiElement.text)
            if (dependency == null && !psiElement.text.startsWith("dependencies")) {
                println("can not parse ${psiElement.text}")
            }
            dependency
        } else {
            null
        }
    }
}

/**
 * In Kotlin file dependency is stored inside
 * [dependency][KtCallExpression] block ->
 * [implementation|api|compile|...][KtCallExpression] block ->
 * as its arguments
 */
object KtsGradleDependenciesAnalyzer :
    PsiMainAnalyzerWithContext<GradleBlockContext, GradleDependency?, List<GradleDependency>>(
        listOf(KtsGradleBlockContextController),
        listOf(KtsGradleDependencyAnalyzer),
        GradleDependenciesAggregator
    )