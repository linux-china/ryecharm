package insyncwithfoo.ryecharm.others.dependencygroups

import insyncwithfoo.ryecharm.PlatformTestCase
import org.junit.Test


internal class DependencyGroupInspectionTest : PlatformTestCase() {
    
    @Test
    fun `test non-existent group`() = doTest("nonExistentGroup")
    
    @Test
    fun `test non-normalized circular group`() = doTest("nonNormalizedCircularGroup")
    
    @Test
    fun `test non-normalized non-existent group`() = doTest("nonNormalizedNonExistentGroup")
    
    @Test
    fun `test invalid group name`() = doTest("invalidGroupName")
    
    @Test
    fun `test normalization`() = doTest("normalizedComparison")
    
    @Test
    fun `test invalid key`() = doTest("invalidKey")
    
    @Test
    fun `test duplicate groups`() = doTest("duplicateGroups")
    
    @Test
    fun `test circular group`() = doTest("circularGroup")
    
    private fun doTest(subdirectory: String) = fileBasedTest("$subdirectory/pyproject.toml") {
        fixture.enableInspections(DependencyGroupInspection())
        fixture.checkHighlighting()
    }
    
}
