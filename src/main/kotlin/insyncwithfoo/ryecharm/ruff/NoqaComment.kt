package insyncwithfoo.ryecharm.ruff

import com.intellij.psi.PsiComment


internal typealias RuleCode = String


/**
 * Due to some reason, IntelliJ IDEA's memory usage always peaks
 * when analyzing this file with injected regex fragments.
 * 
 * This allows bypassing the detection algorithm.
 */
private fun String.toRegexBypassingIDELanguageInjection() = this.toRegex()


// From:
// https://github.com/astral-sh/ruff/blob/5c548dcc04/crates/ruff_linter/src/noqa.rs#L180
internal val ruleCode = """[A-Z]+[0-9]+""".toRegexBypassingIDELanguageInjection()

// From:
// https://github.com/astral-sh/ruff/blob/5c548dcc04/crates/ruff_linter/src/noqa.rs#L56
//
// Ruff use Rust's `char.is_whitespace()` / `str.trim_end()`.
// They are replaced with `\h` here for simplicity.
internal val noqaComment = """(?x)
    (?<prefix>\#\h*(?i:noqa))
    (?:
        (?<colon>:\h*)
        (?<codeList>$ruleCode(?:(?<lastSeparator>[\h,]*+)$ruleCode)*+)
    )?
""".toRegexBypassingIDELanguageInjection()


// https://github.com/astral-sh/ruff/blob/5c548dcc04/crates/ruff_linter/src/noqa.rs#L436
// File-level comments actually uses `[A-Z]+[A-Za-z0-9]+` for codes,
// but this doesn't seem to be a good choice.
private val fileNoqaComment = """(?x)
    \#
	\h*(?:flake8|ruff)\h*:
	\h*(?i:noqa)\h*
	(?::\h*(?<codeList>$ruleCode(?:[\h,]\h*$ruleCode)*)|$)
""".toRegexBypassingIDELanguageInjection()


private data class RuleCodeFragment(
    val content: RuleCode,
    val range: IntRange
) {
    override fun toString() = content
}


private fun RuleCodeFragment(group: MatchResult, codeListAbsoluteOffset: Int): RuleCodeFragment {
    val start = group.range.first + codeListAbsoluteOffset
    val end = group.range.last + codeListAbsoluteOffset
    
    return RuleCodeFragment(group.value, start..end)
}


internal class NoqaComment private constructor(private val codes: List<RuleCodeFragment>) {
    
    fun findRuleCodeAtOffset(offset: Int): RuleCode? {
        return codes.find { offset in it.range }?.toString()
    }
    
    companion object {
        
        private fun fromMatchResult(match: MatchResult, elementOffset: Int): NoqaComment? {
            val delimitedCodeList = match.groups["codeList"] ?: return null
            
            val codeListOffsetRelativeToMatch = delimitedCodeList.range.first
            val matchOffsetRelativeToElement = match.range.first
            val codeListAbsoluteOffset =
                elementOffset + matchOffsetRelativeToElement + codeListOffsetRelativeToMatch
            
            val codes = ruleCode.findAll(delimitedCodeList.value)
                .map { RuleCodeFragment(it, codeListAbsoluteOffset) }
            
            return NoqaComment(codes.toList())
        }
        
        private fun parseFileLevelComment(text: String, elementOffset: Int) =
            fileNoqaComment.find(text)?.let { fromMatchResult(it, elementOffset) }
        
        private fun parseLineComment(text: String, elementOffset: Int): NoqaComment? =
            noqaComment.find(text)?.let { fromMatchResult(it, elementOffset) }
        
        fun parse(psiComment: PsiComment): NoqaComment? {
            val text = psiComment.text
            val elementOffset = psiComment.textOffset
            
            return parseFileLevelComment(text, elementOffset)
                ?: parseLineComment(text, elementOffset)
        }
        
    }
    
}
