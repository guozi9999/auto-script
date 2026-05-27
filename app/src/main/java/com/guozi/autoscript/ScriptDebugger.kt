package com.guozi.autoscript

import android.util.Log

/**
 * 脚本调试器
 * 提供断点、单步执行、变量监视等功能
 */
class ScriptDebugger {
    
    companion object {
        private const val TAG = "ScriptDebugger"
    }
    
    // 调试状态
    var isDebugging = false
        private set
    
    // 断点列表
    private val breakpoints = mutableSetOf<Int>()
    
    // 当前执行行
    var currentLine = 0
        private set
    
    // 变量监视列表
    private val watchExpressions = mutableListOf<String>()
    
    // 变量值缓存
    private val variableValues = mutableMapOf<String, String>()
    
    // 调试回调
    var onBreakpointHit: ((line: Int, variables: Map<String, String>) -> Unit)? = null
    var onStepComplete: ((line: Int, variables: Map<String, String>) -> Unit)? = null
    var onDebugComplete: (() -> Unit)? = null
    
    // 单步执行信号
    private var stepSignal = Object()
    private var shouldStep = false
    private var shouldContinue = false
    
    /**
     * 开始调试
     */
    fun startDebug() {
        isDebugging = true
        currentLine = 0
        variableValues.clear()
        Log.d(TAG, "开始调试")
    }
    
    /**
     * 停止调试
     */
    fun stopDebug() {
        isDebugging = false
        shouldStep = false
        shouldContinue = true
        synchronized(stepSignal) {
            stepSignal.notifyAll()
        }
        Log.d(TAG, "停止调试")
    }
    
    /**
     * 添加断点
     */
    fun addBreakpoint(line: Int) {
        breakpoints.add(line)
        Log.d(TAG, "添加断点: 第 $line 行")
    }
    
    /**
     * 移除断点
     */
    fun removeBreakpoint(line: Int) {
        breakpoints.remove(line)
        Log.d(TAG, "移除断点: 第 $line 行")
    }
    
    /**
     * 清除所有断点
     */
    fun clearBreakpoints() {
        breakpoints.clear()
        Log.d(TAG, "清除所有断点")
    }
    
    /**
     * 获取断点列表
     */
    fun getBreakpoints(): Set<Int> = breakpoints.toSet()
    
    /**
     * 添加监视表达式
     */
    fun addWatchExpression(expression: String) {
        watchExpressions.add(expression)
    }
    
    /**
     * 移除监视表达式
     */
    fun removeWatchExpression(expression: String) {
        watchExpressions.remove(expression)
    }
    
    /**
     * 获取监视表达式
     */
    fun getWatchExpressions(): List<String> = watchExpressions.toList()
    
    /**
     * 更新变量值
     */
    fun updateVariable(name: String, value: String) {
        variableValues[name] = value
    }
    
    /**
     * 获取变量值
     */
    fun getVariable(name: String): String? = variableValues[name]
    
    /**
     * 获取所有变量
     */
    fun getAllVariables(): Map<String, String> = variableValues.toMap()
    
    /**
     * 执行到断点
     */
    fun continueExecution() {
        shouldContinue = true
        shouldStep = false
        synchronized(stepSignal) {
            stepSignal.notifyAll()
        }
    }
    
    /**
     * 单步执行
     */
    fun stepOver() {
        shouldStep = true
        shouldContinue = false
        synchronized(stepSignal) {
            stepSignal.notifyAll()
        }
    }
    
    /**
     * 检查是否应该暂停
     * @param line 当前行号
     * @return 是否暂停
     */
    fun shouldPause(line: Int): Boolean {
        if (!isDebugging) return false
        
        currentLine = line
        
        // 检查断点
        if (breakpoints.contains(line)) {
            Log.d(TAG, "命中断点: 第 $line 行")
            onBreakpointHit?.invoke(line, variableValues.toMap())
            waitForSignal()
            return true
        }
        
        // 检查单步
        if (shouldStep) {
            Log.d(TAG, "单步执行: 第 $line 行")
            onStepComplete?.invoke(line, variableValues.toMap())
            shouldStep = false
            waitForSignal()
            return true
        }
        
        return false
    }
    
    /**
     * 等待信号
     */
    private fun waitForSignal() {
        try {
            synchronized(stepSignal) {
                while (!shouldStep && !shouldContinue && isDebugging) {
                    stepSignal.wait(100)
                }
            }
        } catch (e: InterruptedException) {
            Log.e(TAG, "等待信号被中断: ${e.message}")
        }
    }
    
    /**
     * 生成调试信息
     */
    fun getDebugInfo(): DebugInfo {
        return DebugInfo(
            isDebugging = isDebugging,
            currentLine = currentLine,
            breakpoints = breakpoints.toList(),
            variables = variableValues.toMap(),
            watchExpressions = watchExpressions.toList()
        )
    }
    
    /**
     * 调试信息
     */
    data class DebugInfo(
        val isDebugging: Boolean,
        val currentLine: Int,
        val breakpoints: List<Int>,
        val variables: Map<String, String>,
        val watchExpressions: List<String>
    )
    
    /**
     * 调试命令
     */
    enum class DebugCommand {
        CONTINUE,   // 继续执行
        STEP_OVER,  // 单步跳过
        STEP_INTO,  // 单步进入
        STEP_OUT,   // 单步跳出
        STOP        // 停止调试
    }
}
