package com.guozi.autoscript

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 手势录制器
 * 录制用户的手势操作并可回放
 */
class GestureRecorder(private val service: AccessibilityService) {
    
    companion object {
        private const val TAG = "GestureRecorder"
    }
    
    // 录制状态
    var isRecording = false
        private set
    
    // 录制的手势列表
    private val gestures = CopyOnWriteArrayList<GestureAction>()
    
    // 录制开始时间
    private var startTime = 0L
    
    /**
     * 开始录制
     */
    fun startRecording() {
        gestures.clear()
        startTime = System.currentTimeMillis()
        isRecording = true
        Log.d(TAG, "开始录制手势")
    }
    
    /**
     * 停止录制
     */
    fun stopRecording(): List<GestureAction> {
        isRecording = false
        Log.d(TAG, "停止录制，共 ${gestures.size} 个手势")
        return gestures.toList()
    }
    
    /**
     * 记录点击
     */
    fun recordClick(x: Int, y: Int) {
        if (!isRecording) return
        
        val delay = if (gestures.isEmpty()) 0 else System.currentTimeMillis() - startTime - gestures.last().endTime
        gestures.add(GestureAction(
            type = GestureType.CLICK,
            startX = x,
            startY = y,
            endX = x,
            endY = y,
            duration = 100,
            delayAfter = delay
        ))
    }
    
    /**
     * 记录长按
     */
    fun recordLongClick(x: Int, y: Int, duration: Long) {
        if (!isRecording) return
        
        val delay = if (gestures.isEmpty()) 0 else System.currentTimeMillis() - startTime - gestures.last().endTime
        gestures.add(GestureAction(
            type = GestureType.LONG_CLICK,
            startX = x,
            startY = y,
            endX = x,
            endY = y,
            duration = duration,
            delayAfter = delay
        ))
    }
    
    /**
     * 记录滑动
     */
    fun recordSwipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Long) {
        if (!isRecording) return
        
        val delay = if (gestures.isEmpty()) 0 else System.currentTimeMillis() - startTime - gestures.last().endTime
        gestures.add(GestureAction(
            type = GestureType.SWIPE,
            startX = x1,
            startY = y1,
            endX = x2,
            endY = y2,
            duration = duration,
            delayAfter = delay
        ))
    }
    
    /**
     * 回放手势
     */
    fun playback(onComplete: () -> Unit = {}) {
        if (gestures.isEmpty()) {
            onComplete()
            return
        }
        
        Log.d(TAG, "开始回放 ${gestures.size} 个手势")
        
        Thread {
            for (gesture in gestures) {
                // 等待
                if (gesture.delayAfter > 0) {
                    Thread.sleep(gesture.delayAfter)
                }
                
                // 执行手势
                when (gesture.type) {
                    GestureType.CLICK -> {
                        performClick(gesture.startX, gesture.startY)
                    }
                    GestureType.LONG_CLICK -> {
                        performLongClick(gesture.startX, gesture.startY, gesture.duration)
                    }
                    GestureType.SWIPE -> {
                        performSwipe(gesture.startX, gesture.startY, gesture.endX, gesture.endY, gesture.duration)
                    }
                }
                
                Thread.sleep(100) // 手势间隔
            }
            
            Log.d(TAG, "回放完成")
            onComplete()
        }.start()
    }
    
    /**
     * 转换为 JS 脚本
     */
    fun toScript(): String {
        val sb = StringBuilder()
        sb.appendLine("// 录制的手势脚本")
        sb.appendLine("// 共 ${gestures.size} 个手势")
        sb.appendLine()
        
        for ((index, gesture) in gestures.withIndex()) {
            if (gesture.delayAfter > 0) {
                sb.appendLine("sleep(${gesture.delayAfter});")
            }
            
            when (gesture.type) {
                GestureType.CLICK -> {
                    sb.appendLine("click(${gesture.startX}, ${gesture.startY});")
                }
                GestureType.LONG_CLICK -> {
                    sb.appendLine("longClick(${gesture.startX}, ${gesture.startY}, ${gesture.duration});")
                }
                GestureType.SWIPE -> {
                    sb.appendLine("swipe(${gesture.startX}, ${gesture.startY}, ${gesture.endX}, ${gesture.endY}, ${gesture.duration});")
                }
            }
        }
        
        return sb.toString()
    }
    
    /**
     * 从脚本加载手势
     */
    fun fromScript(script: String): List<GestureAction> {
        val actions = mutableListOf<GestureAction>()
        val lines = script.lines()
        
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("//") || trimmed.isEmpty()) continue
            
            // 解析 click(x, y)
            val clickMatch = Regex("""click\((\d+),\s*(\d+)\)""").find(trimmed)
            if (clickMatch != null) {
                val (x, y) = clickMatch.destructured
                actions.add(GestureAction(
                    type = GestureType.CLICK,
                    startX = x.toInt(),
                    startY = y.toInt(),
                    endX = x.toInt(),
                    endY = y.toInt(),
                    duration = 100,
                    delayAfter = 0
                ))
                continue
            }
            
            // 解析 swipe(x1, y1, x2, y2, duration)
            val swipeMatch = Regex("""swipe\((\d+),\s*(\d+),\s*(\d+),\s*(\d+)(?:,\s*(\d+))?\)""").find(trimmed)
            if (swipeMatch != null) {
                val (x1, y1, x2, y2, duration) = swipeMatch.destructured
                actions.add(GestureAction(
                    type = GestureType.SWIPE,
                    startX = x1.toInt(),
                    startY = y1.toInt(),
                    endX = x2.toInt(),
                    endY = y2.toInt(),
                    duration = if (duration.isNotEmpty()) duration.toLong() else 300,
                    delayAfter = 0
                ))
                continue
            }
            
            // 解析 sleep(ms)
            val sleepMatch = Regex("""sleep\((\d+)\)""").find(trimmed)
            if (sleepMatch != null) {
                val (ms) = sleepMatch.destructured
                if (actions.isNotEmpty()) {
                    val last = actions.last()
                    actions[actions.size - 1] = last.copy(delayAfter = ms.toLong())
                }
            }
        }
        
        return actions
    }
    
    private fun performClick(x: Int, y: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 100))
            .build()
        
        service.dispatchGesture(gesture, null, null)
    }
    
    private fun performLongClick(x: Int, y: Int, duration: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        
        val path = Path().apply { moveTo(x.toFloat(), y.toFloat()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
        
        service.dispatchGesture(gesture, null, null)
    }
    
    private fun performSwipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        
        val path = Path().apply {
            moveTo(x1.toFloat(), y1.toFloat())
            lineTo(x2.toFloat(), y2.toFloat())
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, duration))
            .build()
        
        service.dispatchGesture(gesture, null, null)
    }
    
    /**
     * 手势类型
     */
    enum class GestureType {
        CLICK,
        LONG_CLICK,
        SWIPE
    }
    
    /**
     * 手势动作
     */
    data class GestureAction(
        val type: GestureType,
        val startX: Int,
        val startY: Int,
        val endX: Int,
        val endY: Int,
        val duration: Long,
        val delayAfter: Long
    ) {
        val endTime: Long get() = startTime + duration
        
        companion object {
            private var startTime = 0L
        }
    }
}
