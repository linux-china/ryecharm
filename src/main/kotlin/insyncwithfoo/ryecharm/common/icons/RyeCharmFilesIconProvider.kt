package insyncwithfoo.ryecharm.common.icons

import com.intellij.ide.FileIconProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import insyncwithfoo.ryecharm.RuffIcons
import insyncwithfoo.ryecharm.RyeIcons
import insyncwithfoo.ryecharm.UVIcons
import insyncwithfoo.ryecharm.isRuffToml
import insyncwithfoo.ryecharm.isRyeConfigToml
import insyncwithfoo.ryecharm.isUVLock
import insyncwithfoo.ryecharm.isUVToml


/**
 * Provide customized icons for `.rye/config.toml`,
 * `uv.toml` and `ruff.toml`/`.ruff.toml`.
 * 
 * These icons are used in the *Project* tool window
 * and editor tabs, among others.
 */
internal class RyeCharmFilesIconProvider : FileIconProvider, DumbAware {
    
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?) =
        when {
            file.isRyeConfigToml -> RyeIcons.TINY_16
            file.isRuffToml -> RuffIcons.TINY_16
            file.isUVToml -> UVIcons.TINY_16
            file.isUVLock -> UVIcons.TINY_16_WHITE
            else -> null
        }
    
}
