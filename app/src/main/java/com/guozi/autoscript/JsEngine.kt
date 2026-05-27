package com.guozi.autoscript

import android.content.Context
import org.mozilla.javascript.Context as RhinoContext
import org.mozilla.javascript.ScriptableObject
import org.mozilla.javascript.NativeObject

/**
 * JS 引擎封装
 * 使用 Rhino 执行 JavaScript 脚本
 */
class JsEngine(private val context: Context) {
    
    private var rhinoContext: RhinoContext? = null
    private var scope: ScriptableObject? = null
    private var screenCapture: ScreenCapture? = null
    private var ocrHelper: OcrHelper? = null
    private var extensions: ScriptExtensions? = null
    
    // API 接口
    interface ScriptApi {
        // 基础操作
        fun click(x: Int, y: Int)
        fun longClick(x: Int, y: Int, duration: Long = 1000)
        fun swipe(x1: Int, y1: Int, x2: Int, y2: Int, duration: Long = 300)
        fun input(text: String)
        fun back()
        fun home()
        fun recent()
        fun sleep(millis: Long)
        fun toast(message: String)
        fun log(message: String)
        
        // 节点操作
        fun clickText(text: String): Boolean
        fun clickId(id: String): Boolean
        fun setTextById(id: String, text: String): Boolean
        fun findText(text: String): Boolean
        fun findId(id: String): Boolean
    }
    
    private var api: ScriptApi? = null
    
    fun setApi(api: ScriptApi) {
        this.api = api
    }
    
    fun setScreenCapture(screenCapture: ScreenCapture) {
        this.screenCapture = screenCapture
    }
    
    fun setOcrHelper(ocrHelper: OcrHelper) {
        this.ocrHelper = ocrHelper
    }
    
    fun setExtensions(extensions: ScriptExtensions) {
        this.extensions = extensions
    }
    
    fun init() {
        rhinoContext = RhinoContext.enter()
        rhinoContext!!.optimizationLevel = -1 // 解释模式，兼容 Android
        scope = rhinoContext!!.initStandardObjects()
        
        // 注入 API 函数
        injectFunctions()
    }
    
    private fun injectFunctions() {
        val api = this.api ?: return
        val ext = this.extensions
        
        // ===== 基础操作 =====
        
        // click(x, y) - 点击
        registerFunction("click", 2) { args ->
            api.click(RhinoContext.toInt32(args[0]), RhinoContext.toInt32(args[1]))
        }
        
        // longClick(x, y, duration) - 长按
        registerFunction("longClick", 2) { args ->
            val duration = if (args.size >= 3) RhinoContext.toLong(args[2]) else 1000
            api.longClick(RhinoContext.toInt32(args[0]), RhinoContext.toInt32(args[1]), duration)
        }
        
        // swipe(x1, y1, x2, y2, duration) - 滑动
        registerFunction("swipe", 4) { args ->
            val duration = if (args.size >= 5) RhinoContext.toLong(args[4]) else 300
            api.swipe(
                RhinoContext.toInt32(args[0]), RhinoContext.toInt32(args[1]),
                RhinoContext.toInt32(args[2]), RhinoContext.toInt32(args[3]),
                duration
            )
        }
        
        // input(text) - 输入文字
        registerFunction("input", 1) { args ->
            api.input(RhinoContext.toString(args[0]))
        }
        
        // back() - 返回
        registerFunction("back", 0) { _ -> api.back() }
        
        // home() - 主页
        registerFunction("home", 0) { _ -> api.home() }
        
        // recent() - 最近任务
        registerFunction("recent", 0) { _ -> api.recent() }
        
        // sleep(millis) - 延迟
        registerFunction("sleep", 1) { args ->
            Thread.sleep(RhinoContext.toLong(args[0]))
        }
        
        // toast(message) - 显示提示
        registerFunction("toast", 1) { args ->
            api.toast(RhinoContext.toString(args[0]))
        }
        
        // log(message) - 日志
        registerFunction("log", 1) { args ->
            api.log(RhinoContext.toString(args[0]))
        }
        
        // ===== 节点操作 =====
        
        // clickText(text) - 通过文本点击
        registerFunction("clickText", 1) { args ->
            api.clickText(RhinoContext.toString(args[0]))
        }
        
        // clickId(id) - 通过 ID 点击
        registerFunction("clickId", 1) { args ->
            api.clickId(RhinoContext.toString(args[0]))
        }
        
        // setTextById(id, text) - 设置文本
        registerFunction("setTextById", 2) { args ->
            api.setTextById(RhinoContext.toString(args[0]), RhinoContext.toString(args[1]))
        }
        
        // findText(text) - 查找文本
        registerFunction("findText", 1) { args ->
            api.findText(RhinoContext.toString(args[0]))
        }
        
        // findId(id) - 查找 ID
        registerFunction("findId", 1) { args ->
            api.findId(RhinoContext.toString(args[0]))
        }
        
        // ===== 识图操作 =====
        
        // findImage(templatePath, confidence) - 查找图片
        registerFunction("findImage", 1) { args ->
            val templatePath = RhinoContext.toString(args[0])
            val confidence = if (args.size >= 2) RhinoContext.toNumber(args[1]) else 0.8
            
            val screen = screenCapture?.capture()
            val template = ImageFinder.loadTemplate(templatePath)
            
            if (screen == null || template == null) {
                api.log("截图或模板加载失败")
                null
            } else {
                val result = ImageFinder.findImage(screen, template, confidence)
                template.recycle()
                
                if (result != null) {
                    api.log("找到图片: (${result.x}, ${result.y}), 分数: ${result.score}")
                    val coord = rhinoContext?.newObject(scope)
                    coord?.put("x", coord, result.x)
                    coord?.put("y", coord, result.y)
                    coord
                } else {
                    api.log("未找到图片")
                    null
                }
            }
        }
        
        // findAndClick(templatePath, confidence) - 查找并点击图片
        registerFunction("findAndClick", 1) { args ->
            val templatePath = RhinoContext.toString(args[0])
            val confidence = if (args.size >= 2) RhinoContext.toNumber(args[1]) else 0.8
            
            val screen = screenCapture?.capture()
            val template = ImageFinder.loadTemplate(templatePath)
            
            if (screen == null || template == null) {
                api.log("截图或模板加载失败")
                false
            } else {
                val result = ImageFinder.findImage(screen, template, confidence)
                template.recycle()
                
                if (result != null) {
                    api.click(result.x, result.y)
                    api.log("点击图片位置: (${result.x}, ${result.y})")
                    true
                } else {
                    api.log("未找到图片")
                    false
                }
            }
        }
        
        // findAllImages(templatePath, confidence) - 查找所有匹配图片
        registerFunction("findAllImages", 1) { args ->
            val templatePath = RhinoContext.toString(args[0])
            val confidence = if (args.size >= 2) RhinoContext.toNumber(args[1]) else 0.8
            
            val screen = screenCapture?.capture()
            val template = ImageFinder.loadTemplate(templatePath)
            
            if (screen == null || template == null) {
                api.log("截图或模板加载失败")
                emptyList<Any>()
            } else {
                val results = ImageFinder.findAllImages(screen, template, confidence)
                template.recycle()
                
                api.log("找到 ${results.size} 个匹配图片")
                results.map { result ->
                    val coord = rhinoContext?.newObject(scope)
                    coord?.put("x", coord, result.x)
                    coord?.put("y", coord, result.y)
                    coord
                }
            }
        }
        
        // ===== 颜色操作 =====
        
        // getColor(x, y) - 获取颜色
        registerFunction("getColor", 2) { args ->
            val x = RhinoContext.toInt32(args[0])
            val y = RhinoContext.toInt32(args[1])
            
            val screen = screenCapture?.capture()
            if (screen == null) {
                api.log("截图失败")
                null
            } else {
                val color = ColorHelper.getColor(screen, x, y)
                screen.recycle()
                
                if (color != -1) {
                    val hex = ColorHelper.colorToHex(color)
                    api.log("颜色: ($x, $y) = $hex")
                    hex
                } else {
                    null
                }
            }
        }
        
        // findColor(targetColor, left, top, right, bottom) - 查找颜色
        registerFunction("findColor", 1) { args ->
            val targetColor = RhinoContext.toString(args[0])
            val region = if (args.size >= 5) {
                android.graphics.Rect(
                    RhinoContext.toInt32(args[1]),
                    RhinoContext.toInt32(args[2]),
                    RhinoContext.toInt32(args[3]),
                    RhinoContext.toInt32(args[4])
                )
            } else null
            
            val screen = screenCapture?.capture()
            if (screen == null) {
                api.log("截图失败")
                null
            } else {
                val results = ColorHelper.findColor(screen, targetColor, region)
                screen.recycle()
                
                if (results.isNotEmpty()) {
                    val first = results[0]
                    api.log("找到颜色: (${first.x}, ${first.y})")
                    val coord = rhinoContext?.newObject(scope)
                    coord?.put("x", coord, first.x)
                    coord?.put("y", coord, first.y)
                    coord
                } else {
                    api.log("未找到颜色")
                    null
                }
            }
        }
        
        // colorMatch(x, y, targetColor, threshold) - 颜色匹配
        registerFunction("colorMatch", 3) { args ->
            val x = RhinoContext.toInt32(args[0])
            val y = RhinoContext.toInt32(args[1])
            val targetColor = RhinoContext.toString(args[2])
            val threshold = if (args.size >= 4) RhinoContext.toInt32(args[3]) else 30
            
            val screen = screenCapture?.capture()
            if (screen == null) {
                api.log("截图失败")
                false
            } else {
                val color = ColorHelper.getColor(screen, x, y)
                screen.recycle()
                
                if (color != -1) {
                    ColorHelper.isColorMatch(color, targetColor, threshold)
                } else {
                    false
                }
            }
        }
        
        // ===== 文件操作 =====
        
        if (ext != null) {
            // readFile(path) - 读取文件
            registerFunction("readFile", 1) { args ->
                ext.readFile(RhinoContext.toString(args[0]))
            }
            
            // writeFile(path, content) - 写入文件
            registerFunction("writeFile", 2) { args ->
                ext.writeFile(RhinoContext.toString(args[0]), RhinoContext.toString(args[1]))
            }
            
            // appendFile(path, content) - 追加文件
            registerFunction("appendFile", 2) { args ->
                ext.appendFile(RhinoContext.toString(args[0]), RhinoContext.toString(args[1]))
            }
            
            // fileExists(path) - 文件是否存在
            registerFunction("fileExists", 1) { args ->
                ext.fileExists(RhinoContext.toString(args[0]))
            }
            
            // deleteFile(path) - 删除文件
            registerFunction("deleteFile", 1) { args ->
                ext.deleteFile(RhinoContext.toString(args[0]))
            }
            
            // listFiles(path) - 列出文件
            registerFunction("listFiles", 1) { args ->
                ext.listFiles(RhinoContext.toString(args[0]))
            }
            
            // ===== HTTP 请求 =====
            
            // httpGet(url, headers) - GET 请求
            registerFunction("httpGet", 1) { args ->
                val url = RhinoContext.toString(args[0])
                val result = ext.httpGet(url)
                val obj = rhinoContext?.newObject(scope)
                obj?.put("statusCode", obj, result.statusCode)
                obj?.put("body", obj, result.body)
                obj?.put("success", obj, result.isSuccess)
                obj
            }
            
            // httpPost(url, body, contentType) - POST 请求
            registerFunction("httpPost", 2) { args ->
                val url = RhinoContext.toString(args[0])
                val body = RhinoContext.toString(args[1])
                val contentType = if (args.size >= 3) RhinoContext.toString(args[2]) else "application/json"
                val result = ext.httpPost(url, body, contentType)
                val obj = rhinoContext?.newObject(scope)
                obj?.put("statusCode", obj, result.statusCode)
                obj?.put("body", obj, result.body)
                obj?.put("success", obj, result.isSuccess)
                obj
            }
            
            // ===== 定时器 =====
            
            // setTimeout(callback, delay) - 延时执行
            // 注意：Rhino 中传递函数比较复杂，这里用字符串脚本方式
            registerFunction("setTimeout", 2) { args ->
                val script = RhinoContext.toString(args[0])
                val delay = RhinoContext.toLong(args[1])
                ext.setTimeout({ execute(script) }, delay)
            }
            
            // setInterval(callback, interval) - 循环执行
            registerFunction("setInterval", 2) { args ->
                val script = RhinoContext.toString(args[0])
                val interval = RhinoContext.toLong(args[1])
                ext.setInterval({ execute(script) }, interval)
            }
            
            // clearTimeout(id) - 清除定时器
            registerFunction("clearTimeout", 1) { args ->
                ext.clearTimeout(RhinoContext.toString(args[0]))
            }
            
            // clearInterval(id) - 清除循环定时器
            registerFunction("clearInterval", 1) { args ->
                ext.clearTimeout(RhinoContext.toString(args[0]))
            }
            
            // ===== JSON 操作 =====
            
            // JSON.parse(str) - 解析 JSON
            val jsonObj = rhinoContext?.newObject(scope)
            val parseFunc = object : org.mozilla.javascript.BaseFunction() {
                override fun call(cx: RhinoContext, scope: org.mozilla.javascript.Scriptable, thisObj: org.mozilla.javascript.Scriptable, args: Array<out Any>): Any {
                    return ext.jsonParse(RhinoContext.toString(args[0])) ?: RhinoContext.getUndefinedValue()
                }
            }
            val stringifyFunc = object : org.mozilla.javascript.BaseFunction() {
                override fun call(cx: RhinoContext, scope: org.mozilla.javascript.Scriptable, thisObj: org.mozilla.javascript.Scriptable, args: Array<out Any>): Any {
                    return ext.jsonStringify(args[0])
                }
            }
            jsonObj?.put("parse", jsonObj, parseFunc)
            jsonObj?.put("stringify", jsonObj, stringifyFunc)
            scope?.put("JSON", scope, jsonObj)
        }
    }
    
    private fun registerFunction(name: String, argCount: Int, handler: (Array<out Any>) -> Any?) {
        val func = object : org.mozilla.javascript.BaseFunction() {
            override fun call(
                cx: RhinoContext,
                scope: org.mozilla.javascript.Scriptable,
                thisObj: org.mozilla.javascript.Scriptable,
                args: Array<out Any>
            ): Any {
                return try {
                    handler(args) ?: RhinoContext.getUndefinedValue()
                } catch (e: Exception) {
                    api?.log("错误: ${e.message}")
                    RhinoContext.getUndefinedValue()
                }
            }
        }
        scope?.put(name, scope, func)
    }
    
    fun execute(script: String): String? {
        return try {
            val result = rhinoContext?.evaluateString(scope, script, "script", 1, null)
            if (result != null && result != RhinoContext.getUndefinedValue()) {
                RhinoContext.toString(result)
            } else {
                null
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
    
    fun destroy() {
        RhinoContext.exit()
        rhinoContext = null
        scope = null
    }
}
