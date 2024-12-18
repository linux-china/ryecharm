package insyncwithfoo.ryecharm

import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile


internal abstract class LanguageInjectionTestCase : PlatformTestCase() {
    
    private val injectedLanguageManager: InjectedLanguageManager
        get() = InjectedLanguageManager.getInstance(project)
    
    protected val hostFile: PsiFile
        get() = injectedLanguageManager.getTopLevelFile(file)
    
    private val PsiElement.injectedElements: List<PsiElement>
        get() = injectedLanguageManager.getInjectedPsiFiles(this)?.map { it.first } ?: emptyList()
    
    private val PsiElement.fragments: Sequence<PsiElement>
        get() = children.asSequence().flatMap { it.injectedElements.asSequence() + it.fragments }
    
    protected val fragments: List<PsiElement>
        get() = hostFile.fragments.toList()
    
    protected fun assertFileHasInjections() {
        assertNotEmpty(fragments)
    }
    
    protected fun assertFileDoesNotHaveInjections() {
        assertEmpty(fragments)
    }
    
}
