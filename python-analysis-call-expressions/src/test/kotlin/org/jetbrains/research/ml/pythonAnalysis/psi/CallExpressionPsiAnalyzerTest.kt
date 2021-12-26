package org.jetbrains.research.ml.pythonAnalysis.psi

import com.jetbrains.python.actions.PyQualifiedNameProvider
import com.jetbrains.python.psi.PyCallExpression
import com.jetbrains.python.psi.PyDecorator
import com.jetbrains.python.psi.resolve.PyResolveContext
import com.jetbrains.python.psi.types.TypeEvalContext
import org.jetbrains.research.ml.kotlinAnalysis.psi.extentions.extractElementsOfType
import org.jetbrains.research.ml.pythonAnalysis.CallExpressionAnalyzer
import org.jetbrains.research.ml.pythonAnalysis.CallExpressionAnalyzerContext
import org.jetbrains.research.pluginUtilities.util.Extension
import org.jetbrains.research.pluginUtilities.util.ParametrizedBaseWithPythonSdkTest
import org.jetbrains.research.pluginUtilities.util.getPsiFile
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File

@RunWith(Parameterized::class)
class CallExpressionPsiAnalyzerTest : ParametrizedBaseWithPythonSdkTest(
    getResourcesRootPath(
        ::CallExpressionPsiAnalyzerTest,
        resourcesRootName = "callExpressionPsiAnalyzerTestData"
    )
) {
    @JvmField
    @Parameterized.Parameter(0)
    var inFile: File? = null

    @JvmField
    @Parameterized.Parameter(1)
    var outFile: File? = null

    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{index}: ({0}, {1})")
        fun getTestData() =
            getInAndOutArray(
                ::CallExpressionPsiAnalyzerTest,
                resourcesRootName = "callExpressionPsiAnalyzerTestData",
                inExtension = Extension.PY,
                outExtension = Extension.TXT,
            )
    }

    @Test
    fun testCallExpressionFqNamesInFile() {
        val typeEvalContext = TypeEvalContext.deepCodeInsight(myFixture.project)
        val pyResolveContext = PyResolveContext.defaultContext(typeEvalContext)
        val fqNamesProvider = PyQualifiedNameProvider()

        val analyzerContext = CallExpressionAnalyzerContext(pyResolveContext, fqNamesProvider)

        val inPsiFile = getPsiFile(inFile!!, myFixture)

        val callExpressions =
            inPsiFile.extractElementsOfType(PyCallExpression::class.java).filter { it !is PyDecorator }

        val actualCallExpressionFqNames =
            callExpressions.mapNotNull { CallExpressionAnalyzer.analyze(it, analyzerContext) }.sorted()

        val expectedCallExpressionFqNames = outFile!!.readLines().sorted()

        assertEquals(actualCallExpressionFqNames, expectedCallExpressionFqNames)
    }
}