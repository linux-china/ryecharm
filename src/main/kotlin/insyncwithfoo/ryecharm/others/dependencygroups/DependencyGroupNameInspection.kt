package insyncwithfoo.ryecharm.others.dependencygroups

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import insyncwithfoo.ryecharm.isPyprojectToml
import insyncwithfoo.ryecharm.isString
import insyncwithfoo.ryecharm.keyValuePair
import insyncwithfoo.ryecharm.message
import insyncwithfoo.ryecharm.stringContent
import insyncwithfoo.ryecharm.table
import org.toml.lang.psi.TomlArray
import org.toml.lang.psi.TomlInlineTable
import org.toml.lang.psi.TomlLiteral
import org.toml.lang.psi.TomlVisitor
import org.toml.lang.psi.ext.name


private class DependencyGroupNameVisitor(private val holder: ProblemsHolder) : TomlVisitor() {
    
    override fun visitLiteral(element: TomlLiteral) {
        if (element.containingFile.virtualFile?.isPyprojectToml != true) {
            return
        }
        
        val string = element.takeIf { it.isString } ?: return
        val propertyPair = string.keyValuePair?.takeIf { it.key.name == "include-group" } ?: return
        
        val inlineTable = propertyPair.parent as? TomlInlineTable ?: return
        val array = inlineTable.parent as? TomlArray ?: return
        val arrayKey = array.keyValuePair?.key ?: return
        
        val dependencyGroupsTable = arrayKey.table?.takeIf { it.isDependencyGroups }
        val registeredGroupNames = dependencyGroupsTable?.groupNames ?: emptyList()
        val groupName = string.stringContent ?: return
        val normalizedGroupName = groupName.normalize()
        
        when {
            !groupName.isValid -> reportInvalidGroupName(element, groupName)
            normalizedGroupName !in registeredGroupNames -> reportUnknownGroup(element, groupName)
        }
    }
    
    private fun reportInvalidGroupName(element: PsiElement, groupName: String) {
        val message = message("inspections.dependencyGroupNames.message.invalid", groupName)
        val problemHighlightType = ProblemHighlightType.POSSIBLE_PROBLEM
        
        holder.registerProblem(element, message, problemHighlightType)
    }
    
    private fun reportUnknownGroup(element: PsiElement, groupName: String) {
        val message = message("inspections.dependencyGroupNames.message.unknown", groupName)
        val problemHighlightType = ProblemHighlightType.LIKE_UNKNOWN_SYMBOL
        
        holder.registerProblem(element, message, problemHighlightType)
    }
    
}


internal class DependencyGroupNameInspection : LocalInspectionTool(), DumbAware {
    
    override fun getShortName() = SHORT_NAME
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
        DependencyGroupNameVisitor(holder)
    
    companion object {
        const val SHORT_NAME = "insyncwithfoo.ryecharm.others.dependencygroups.DependencyGroupNameInspection"
    }
    
}