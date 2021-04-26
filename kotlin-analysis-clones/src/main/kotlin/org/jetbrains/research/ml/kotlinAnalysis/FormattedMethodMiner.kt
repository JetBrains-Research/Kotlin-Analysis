package org.jetbrains.research.ml.kotlinAnalysis

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.application.ApplicationManager
import org.jetbrains.research.ml.kotlinAnalysis.psi.PsiProvider
import org.jetbrains.research.ml.kotlinAnalysis.util.getPrintWriter
import org.jetbrains.research.ml.kotlinAnalysis.util.getSubdirectories
import java.nio.file.Path

/**
 * Extracts all methods from projects in the dataset and saves methods in Clone Detection Tool
 * ([SourcererCC](https://github.com/JetBrains-Research/SourcererCC/)) format.
 */
class FormattedMethodMiner(outputDir: Path) {
    private val indexer = IndexBuilder(outputDir)
    private val methodDataWriter = getPrintWriter(outputDir, "method_data.txt")

    fun extractMethodsToCloneDetectionFormat(inputDir: Path) {
        getSubdirectories(inputDir).forEach { projectPath ->
            ApplicationManager.getApplication().runReadAction {
                ProjectUtil.openOrImport(projectPath, null, true).let { project ->
                    val projectIndex = indexer.indexProject(project)
                    println("Start processing project ${project.name} (index $projectIndex)")
                    val methods = PsiProvider.extractMethodsFromProject(project)
                    val methodsIndexed = methods.associateWith { method ->
                        indexer.indexMethod(method, projectIndex)
                    }
                    methodsIndexed.forEach { (method, methodIndex) ->
                        val methodFormatted = CloneDetectionAdapter.format(method, projectIndex, methodIndex)
                        methodDataWriter.println(methodFormatted)
                    }
                }
            }
        }
        close()
    }

    private fun close() {
        indexer.close()
        methodDataWriter.close()
    }
}
