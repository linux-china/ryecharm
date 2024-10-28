package insyncwithfoo.ryecharm.others.dependencygroups

import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.psi.PsiElement
import com.jetbrains.python.icons.PythonIcons
import insyncwithfoo.ryecharm.keyValuePair
import insyncwithfoo.ryecharm.message
import org.toml.lang.psi.TomlKey
import org.toml.lang.psi.TomlTable
import org.toml.lang.psi.ext.name


internal class DependencyGroupInstaller : RunLineMarkerContributor() {
    
    override fun getInfo(element: PsiElement): Info? {
        val key = element as? TomlKey ?: return null
        val keyValuePair = key.keyValuePair ?: return null
        val table = keyValuePair.parent as? TomlTable ?: return null
        
        if (table.header.key?.text != "dependency-groups") {
            return null
        }
        
        val groupName = key.name ?: return null
        
        val icon = PythonIcons.Python.PythonPackages
        val action = InstallDependencyGroup(element.project, groupName)
        
        return Info(icon, arrayOf(action)) { message("lineMarkers.dependencyGroups.install", groupName) }
    }
    
}