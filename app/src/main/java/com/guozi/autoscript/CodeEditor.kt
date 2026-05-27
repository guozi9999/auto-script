package com.guozi.autoscript

import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText

/**
 * 代码编辑器
 * 支持语法高亮的代码编辑器
 */
class CodeEditor @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {
    
    companion object {
        // 颜色定义
        private val KEYWORD_COLOR = Color.parseColor("#FF79C6")    // 关键字 - 粉色
        private val STRING_COLOR = Color.parseColor("#F1FA8C")     // 字符串 - 黄色
        private val NUMBER_COLOR = Color.parseColor("#BD93F9")     // 数字 - 紫色
        private val COMMENT_COLOR = Color.parseColor("#6272A4")    // 注释 - 灰色
        private val FUNCTION_COLOR = Color.parseColor("#50FA7B")   // 函数 - 绿色
        private val OPERATOR_COLOR = Color.parseColor("#FF79C6")   // 运算符 - 粉色
        
        // 关键字列表
        private val KEYWORDS = setOf(
            "var", "let", "const", "function", "return", "if", "else", "for", "while",
            "do", "switch", "case", "break", "continue", "new", "this", "try", "catch",
            "finally", "throw", "typeof", "instanceof", "in", "of", "true", "false",
            "null", "undefined", "NaN", "Infinity"
        )
        
        // 内置函数列表
        private val BUILTIN_FUNCTIONS = setOf(
            "click", "longClick", "swipe", "input", "back", "home", "recent",
            "sleep", "log", "toast", "clickText", "clickId", "setTextById",
            "findText", "findId", "findImage", "findAndClick", "findAllImages",
            "getColor", "findColor", "colorMatch",
            "readFile", "writeFile", "appendFile", "fileExists", "deleteFile", "listFiles",
            "httpGet", "httpPost", "setTimeout", "setInterval", "clearTimeout", "clearInterval"
        )
    }
    
    private var isHighlighting = false
    
    init {
        // 设置等宽字体
        typeface = android.graphics.Typeface.MONOSPACE
        
        // 监听文本变化
        addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                if (!isHighlighting && s != null) {
                    highlightCode(s)
                }
            }
        })
    }
    
    /**
     * 高亮代码
     */
    private fun highlightCode(editable: android.text.Editable) {
        isHighlighting = true
        
        val text = editable.toString()
        val spannable = SpannableStringBuilder(text)
        
        // 清除旧的样式
        val spans = editable.getSpans(0, editable.length, ForegroundColorSpan::class.java)
        for (span in spans) {
            editable.removeSpan(span)
        }
        
        // 高亮注释
        highlightRegex(spannable, """//.*$""", COMMENT_COLOR, RegexOption.MULTILINE)
        highlightRegex(spannable, """/\*[\s\S]*?\*/""", COMMENT_COLOR)
        
        // 高亮字符串
        highlightRegex(spannable, """"[^"]*""""", STRING_COLOR)
        highlightRegex(spannable, """'[^']*'""", STRING_COLOR)
        highlightRegex(spannable, """`[^`]*`""", STRING_COLOR)
        
        // 高亮数字
        highlightRegex(spannable, """\b\d+\.?\d*\b""", NUMBER_COLOR)
        
        // 高亮关键字
        for (keyword in KEYWORDS) {
            highlightWord(spannable, keyword, KEYWORD_COLOR)
        }
        
        // 高亮内置函数
        for (func in BUILTIN_FUNCTIONS) {
            highlightWord(spannable, func, FUNCTION_COLOR)
        }
        
        // 应用样式
        editable.replace(0, editable.length, spannable)
        
        isHighlighting = false
    }
    
    /**
     * 高亮正则匹配
     */
    private fun highlightRegex(spannable: SpannableStringBuilder, pattern: String, color: Int, option: RegexOption? = null) {
        val regex = if (option != null) Regex(pattern, option) else Regex(pattern)
        regex.findAll(spannable).forEach { match ->
            spannable.setSpan(
                ForegroundColorSpan(color),
                match.range.first,
                match.range.last + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
    
    /**
     * 高亮单词
     */
    private fun highlightWord(spannable: SpannableStringBuilder, word: String, color: Int) {
        val regex = Regex("""\b$word\b""")
        regex.findAll(spannable).forEach { match ->
            spannable.setSpan(
                ForegroundColorSpan(color),
                match.range.first,
                match.range.last + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
    
    /**
     * 获取当前行号
     */
    fun getCurrentLine(): Int {
        val cursorPos = selectionStart
        if (cursorPos < 0) return 0
        return text?.substring(0, cursorPos)?.count { it == '\n' } ?: 0
    }
    
    /**
     * 获取总行数
     */
    fun getLineCount(): Int {
        return text?.toString()?.lines()?.size ?: 0
    }
    
    /**
     * 获取指定行的内容
     */
    fun getLine(lineNumber: Int): String? {
        val lines = text?.toString()?.lines()
        return if (lineNumber in (lines?.indices ?: 0 until 0)) {
            lines?.get(lineNumber)
        } else {
            null
        }
    }
    
    /**
     * 设置指定行的内容
     */
    fun setLine(lineNumber: Int, content: String) {
        val lines = text?.toString()?.lines()?.toMutableList() ?: return
        if (lineNumber in lines.indices) {
            lines[lineNumber] = content
            setText(lines.joinToString("\n"))
        }
    }
    
    /**
     * 插入文本到当前光标位置
     */
    fun insertText(text: String) {
        val start = selectionStart
        if (start >= 0) {
            this.text?.insert(start, text)
        }
    }
    
    /**
     * 注释/取消注释当前行
     */
    fun toggleComment() {
        val currentLine = getCurrentLine()
        val line = getLine(currentLine) ?: return
        
        if (line.trimStart().startsWith("//")) {
            // 取消注释
            val newLine = line.replaceFirst("//", "").replaceFirst(" ", "")
            setLine(currentLine, newLine)
        } else {
            // 添加注释
            setLine(currentLine, "// $line")
        }
    }
    
    /**
     * 自动缩进
     */
    fun autoIndent() {
        val currentLine = getCurrentLine()
        val line = getLine(currentLine) ?: return
        
        // 获取上一行的缩进
        val prevLine = if (currentLine > 0) getLine(currentLine - 1) else null
        val prevIndent = prevLine?.takeWhile { it == ' ' }?.length ?: 0
        
        // 检查是否需要增加缩进
        val trimmedLine = line.trimStart()
        val newIndent = if (trimmedLine.endsWith("{") || trimmedLine.endsWith("(")) {
            prevIndent + 2
        } else {
            prevIndent
        }
        
        // 应用缩进
        val indent = " ".repeat(newIndent)
        setLine(currentLine, indent + trimmedLine)
    }
}
