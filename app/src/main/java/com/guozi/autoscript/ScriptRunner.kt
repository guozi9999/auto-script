package com.guozi.autoscript

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*

/**
 * 脚本执行器
 * 负责执行 JS 脚本并调用无障碍服务
 */
class ScriptRunner(private val context: Context) {
    
    companion object {
        private const val TAG = "ScriptRunner"
    }
    
    private val jsEngine = JsEngine(context)
    private val screenCapture = ScreenCapture(context)
    private val ocrHelper = OcrHelper(context)
    private val extensions = ScriptExtensions { message -> log(message) }
    private val handler = Handler(Looper.getMainLooper())
    private var job: Job? = null
    var isRunning = false
        private set
    
    // 日志回调
    var onLog: ((String) -> Unit)? = null
    
    init {
        jsEngine.setScreenCapture(screenCapture)
        jsEngine.setOcrHelper(ocrHelper)
        jsEngine.setExtensions(extensions)
        
        jsEngine.setApi(object : JsEngine.ScriptApi {
            // ===== 基础操作 =====
            
            override fun click(x: Int, y: Int) {
                val service = AutoAccessibilityService.instance
                if (service == null) {
                    log("错误：无障碍服务未启用")
                    return
                }
                service.click(x, y)
                log("点击: ($x, $y)")
            }
            
            override fun longClick(x: Int, y: Int, duration: Long) {
                val service = AutoAccessibilityService.instance
                if (service == null) {
                    log("错误：无障碍服务未启用")
                    return
                }
                service.longClick(x, y, duration)
                log("长按: ($x, $y) ${duration}ms")
            }
            
            override fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Long) {
                val service = AutoAccessibilityService.instance
                if (service == null) {
                    log("错误：无障碍服务未启用")
                    return
                }
                service.swipe(x1, y1, x2, y2, duration)
                log("滑动: ($x1,$y1) -> ($x2,$y2)")
            }
            
            override fun input(text: String) {
                val service = AutoAccessibilityService.instance
                if (service == null) {
                    log("错误：无障碍服务未启用")
                    return
                }
                service.inputText(text)
                log("输入: $text")
            }
            
            override fun back() {
                AutoAccessibilityService.instance?.back()
                log("返回")
            }
            
            override fun home() {
                AutoAccessibilityService.instance?.home()
                log("主页")
            }
            
            override fun recent() {
                AutoAccessibilityService.instance?.recent()
                log("最近任务")
            }
            
            override fun sleep(millis: Long) {
                Thread.sleep(millis)
            }
            
            override fun toast(message: String) {
                handler.post {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                log("提示: $message")
            }
            
            override fun log(message: String) {
                this@ScriptRunner.log(message)
            }
            
            // ===== 节点操作 =====
            
            override fun clickText(text: String): Boolean {
                val service = AutoAccessibilityService.instance
                if (service == null) {
                    log("错误：无障碍服务未启用")
                    return false
                }
                
                val nodes = NodeHelper.findByText(service, text)
                return if (nodes.isNotEmpty()) {
                    nodes[0].click(service)
                    log("点击文本: $text")
                    true
                } else {
                    log("未找到文本: $text")
                    false
                }
            }
            
            override fun clickId(id: String): Boolean {
                val service = AutoAccessibilityService.instance
                if (service == null) {
                    log("错误：无障碍服务未启用")
                    return false
                }
                
                val nodes = NodeHelper.findById(service, id)
                return if (nodes.isNotEmpty()) {
                    nodes[0].click(service)
                    log("点击 ID: $id")
                    true
                } else {
                    log("未找到 ID: $id")
                    false
                }
            }
            
            override fun setTextById(id: String, text: String): Boolean {
                val service = AutoAccessibilityService.instance
                if (service == null) {
                    log("错误：无障碍服务未启用")
                    return false
                }
                
                val nodes = NodeHelper.findById(service, id)
                return if (nodes.isNotEmpty()) {
                    nodes[0].setText(service, text)
                    log("设置文本: $id = $text")
                    true
                } else {
                    log("未找到 ID: $id")
                    false
                }
            }
            
            override fun findText(text: String): Boolean {
                val service = AutoAccessibilityService.instance
                if (service == null) {
                    log("错误：无障碍服务未启用")
                    return false
                }
                
                val nodes = NodeHelper.findByText(service, text)
                return if (nodes.isNotEmpty()) {
                    log("找到文本: $text (${nodes.size} 个)")
                    true
                } else {
                    log("未找到文本: $text")
                    false
                }
            }
            
            override fun findId(id: String): Boolean {
                val service = AutoAccessibilityService.instance
                if (service == null) {
                    log("错误：无障碍服务未启用")
                    return false
                }
                
                val nodes = NodeHelper.findById(service, id)
                return if (nodes.isNotEmpty()) {
                    log("找到 ID: $id (${nodes.size} 个)")
                    true
                } else {
                    log("未找到 ID: $id")
                    false
                }
            }
        })
    }
    
    /**
     * 获取截屏服务
     */
    fun getScreenCapture(): ScreenCapture = screenCapture
    
    fun execute(script: String) {
        if (isRunning) {
            log("脚本正在运行中")
            return
        }
        
        isRunning = true
        log("开始执行脚本...")
        
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                jsEngine.init()
                val result = jsEngine.execute(script)
                
                withContext(Dispatchers.Main) {
                    if (result != null) {
                        log("脚本返回: $result")
                    }
                    log("脚本执行完成")
                    isRunning = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    log("错误: ${e.message}")
                    isRunning = false
                }
            } finally {
                jsEngine.destroy()
            }
        }
    }
    
    fun stop() {
        job?.cancel()
        extensions.clearAllTimers()
        isRunning = false
        log("脚本已停止")
    }
    
    private fun log(message: String) {
        Log.d(TAG, message)
        handler.post {
            onLog?.invoke(message)
        }
    }
    
    fun destroy() {
        stop()
        screenCapture.destroy()
        ocrHelper.destroy()
        extensions.destroy()
    }
}
