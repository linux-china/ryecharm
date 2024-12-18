package insyncwithfoo.ryecharm.ruff.onsave

import com.intellij.ide.actionsOnSave.impl.ActionsOnSaveFileDocumentManagerListener.ActionOnSave
import com.intellij.ide.actionsOnSave.impl.ActionsOnSaveFileDocumentManagerListener.DocumentUpdatingActionOnSave
import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.toNioPathOrNull
import com.intellij.psi.PsiFile
import insyncwithfoo.ryecharm.Command
import insyncwithfoo.ryecharm.CoroutineService
import insyncwithfoo.ryecharm.completedAbnormally
import insyncwithfoo.ryecharm.configurations.ruff.ruffConfigurations
import insyncwithfoo.ryecharm.fileDocumentManager
import insyncwithfoo.ryecharm.isSupportedByRuff
import insyncwithfoo.ryecharm.launch
import insyncwithfoo.ryecharm.message
import insyncwithfoo.ryecharm.paste
import insyncwithfoo.ryecharm.psiDocumentManager
import insyncwithfoo.ryecharm.ruff.commands.ruff
import insyncwithfoo.ryecharm.runInBackground
import insyncwithfoo.ryecharm.runWriteCommandAction
import kotlinx.coroutines.CoroutineScope
import java.nio.file.Path


// TODO: Use com.intellij.openapi.roots.ProjectFileIndex.isInProject
private operator fun Project.contains(file: VirtualFile) =
    basePath?.let { file.canonicalPath?.startsWith(it) } ?: false


/**
 * Fix and/or format files with Ruff on save.
 * 
 * This deliberately inherits from [ActionOnSave]
 * instead of [DocumentUpdatingActionOnSave]
 * because it isn't clear what should be done to:
 * 
 * * Retrieve the new content in suspending context under read action
 * * Write the new content back in suspending context under write action
 * * Do both of the above in a non-read-action BGT.
 */
internal class RuffOnSaveTasksRunner : ActionOnSave() {
    
    override fun isEnabledForProject(project: Project) =
        project.ruff != null && project.ruffConfigurations.run { formatOnSave || fixOnSave }
    
    override fun processDocuments(project: Project, documents: Array<Document>) {
        val configurations = project.ruffConfigurations
        val psiDocumentManager = project.psiDocumentManager
        
        val projectFilesOnly = configurations.runOnSaveProjectFilesOnly
        
        documents.forEach { document ->
            val file = psiDocumentManager.getPsiFile(document)
            val virtualFile = file?.virtualFile
            
            if (file == null || !file.isSupportedByRuff) {
                return@forEach
            }
            
            if (virtualFile == null || projectFilesOnly && virtualFile !in project) {
                return@forEach
            }
            
            project.process(document, file)
        }
    }
    
    private fun Project.process(document: Document, file: PsiFile) = launch<Coroutine> {
        val path = fileDocumentManager.getFile(document)?.toNioPathOrNull()
        val newText = fixAndFormat(document.text, path) ?: return@launch
        
        runWriteCommandAction(message("progresses.command.ruff.format")) {
            file.paste(newText)
            fileDocumentManager.saveDocument(document)
        }
    }
    
    private suspend fun Project.fixAndFormat(originalText: String, path: Path?): String? {
        val configurations = ruffConfigurations
        val ruff = ruff!!
        
        var newText = originalText
        
        if (configurations.fixOnSave) {
            val command = ruff.fixAll(newText, path)
            newText = getResultOrNull(command) ?: return null
        }
        
        if (configurations.formatOnSave) {
            val command = ruff.format(newText, path)
            newText = getResultOrNull(command) ?: return null
        }
        
        return newText
    }
    
    private suspend fun Project.getResultOrNull(command: Command): String? {
        val output = runInBackground(command)
        
        return output.stdout.takeUnless { output.completedAbnormally }
    }
    
    @Service(Service.Level.PROJECT)
    private class Coroutine(override val scope: CoroutineScope) : CoroutineService
    
}
