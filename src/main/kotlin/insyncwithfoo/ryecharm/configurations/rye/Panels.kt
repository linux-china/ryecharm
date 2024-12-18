package insyncwithfoo.ryecharm.configurations.rye

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.Row
import com.intellij.ui.dsl.builder.panel
import insyncwithfoo.ryecharm.bindText
import insyncwithfoo.ryecharm.configurations.AdaptivePanel
import insyncwithfoo.ryecharm.configurations.Overrides
import insyncwithfoo.ryecharm.configurations.PanelBasedConfigurable
import insyncwithfoo.ryecharm.configurations.projectAndOverrides
import insyncwithfoo.ryecharm.emptyText
import insyncwithfoo.ryecharm.makeFlexible
import insyncwithfoo.ryecharm.message
import insyncwithfoo.ryecharm.rye.commands.Rye
import insyncwithfoo.ryecharm.rye.commands.detectExecutable
import insyncwithfoo.ryecharm.singleFileTextField


private class RyePanel(state: RyeConfigurations, overrides: Overrides?, project: Project?) :
    AdaptivePanel<RyeConfigurations>(state, overrides, project)


private fun Row.executableInput(block: Cell<TextFieldWithBrowseButton>.() -> Unit) =
    singleFileTextField().makeFlexible().apply(block)


private fun RyePanel.makeComponent() = panel {
    
    row(message("configurations.rye.executable.label")) {
        executableInput {
            val detectedExecutable = Rye.detectExecutable()?.toString()
            
            bindText(state::executable) { detectedExecutable.orEmpty() }
            emptyText = detectedExecutable ?: message("configurations.rye.executable.placeholder")
        }
        overrideCheckbox(state::executable)
    }
    
    timeoutGroup(state.timeouts, RyeTimeouts.entries)
    
}


internal fun PanelBasedConfigurable<RyeConfigurations>.createPanel(state: RyeConfigurations): DialogPanel {
    val (project, overrides) = projectAndOverrides
    return RyePanel(state, overrides, project).makeComponent()
}
