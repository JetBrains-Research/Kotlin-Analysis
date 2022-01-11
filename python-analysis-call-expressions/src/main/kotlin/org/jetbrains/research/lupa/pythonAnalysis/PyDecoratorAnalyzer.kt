package org.jetbrains.research.lupa.pythonAnalysis

import com.jetbrains.python.psi.impl.PyDecoratorImpl

/**
 * Analyzer for [decorator][PyDecoratorImpl].
 * Analysis consists of fully qualified name extraction.
 */
object PyDecoratorAnalyzer : CallExpressionAnalyzer<PyDecoratorImpl>(PyDecoratorImpl::class.java)
