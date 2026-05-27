package com.guozi.autoscript

import android.util.Log
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.ConcurrentHashMap

/**
 * 脚本扩展功能
 * 提供文件操作、HTTP 请求、定时器等功能
 */
class ScriptExtensions(private val logger: (String) -> Unit) {
    
    companion object {
        private const val TAG = "ScriptExtensions"
    }
    
    // 定时器管理
    private val timers = ConcurrentHashMap<String, Timer>()
    private var timerCounter = 0
    
    // ===== 文件操作 =====
    
    /**
     * 读取文件内容
     */
    fun readFile(path: String): String? {
        return try {
            val file = File(path)
            if (!file.exists()) {
                logger("文件不存在: $path")
                return null
            }
            file.readText()
        } catch (e: Exception) {
            logger("读取文件失败: ${e.message}")
            null
        }
    }
    
    /**
     * 写入文件内容
     */
    fun writeFile(path: String, content: String): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            logger("写入文件成功: $path")
            true
        } catch (e: Exception) {
            logger("写入文件失败: ${e.message}")
            false
        }
    }
    
    /**
     * 追加内容到文件
     */
    fun appendFile(path: String, content: String): Boolean {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.appendText(content)
            true
        } catch (e: Exception) {
            logger("追加文件失败: ${e.message}")
            false
        }
    }
    
    /**
     * 检查文件是否存在
     */
    fun fileExists(path: String): Boolean {
        return File(path).exists()
    }
    
    /**
     * 删除文件
     */
    fun deleteFile(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
                logger("删除文件: $path")
                true
            } else {
                logger("文件不存在: $path")
                false
            }
        } catch (e: Exception) {
            logger("删除文件失败: ${e.message}")
            false
        }
    }
    
    /**
     * 获取文件大小
     */
    fun fileSize(path: String): Long {
        return try {
            File(path).length()
        } catch (e: Exception) {
            -1
        }
    }
    
    /**
     * 列出目录下的文件
     */
    fun listFiles(path: String): Array<String> {
        return try {
            val dir = File(path)
            if (dir.isDirectory) {
                dir.list() ?: emptyArray()
            } else {
                emptyArray()
            }
        } catch (e: Exception) {
            emptyArray()
        }
    }
    
    // ===== HTTP 请求 =====
    
    /**
     * 发送 GET 请求
     */
    fun httpGet(url: String, headers: Map<String, String> = emptyMap()): HttpResult {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            
            val responseCode = connection.responseCode
            val body = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().readText()
            } else {
                connection.errorStream?.bufferedReader()?.readText() ?: ""
            }
            
            connection.disconnect()
            HttpResult(responseCode, body)
        } catch (e: Exception) {
            logger("HTTP GET 失败: ${e.message}")
            HttpResult(-1, e.message ?: "Unknown error")
        }
    }
    
    /**
     * 发送 POST 请求
     */
    fun httpPost(url: String, body: String, contentType: String = "application/json", headers: Map<String, String> = emptyMap()): HttpResult {
        return try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", contentType)
            
            headers.forEach { (key, value) ->
                connection.setRequestProperty(key, value)
            }
            
            connection.outputStream.bufferedWriter().use { writer ->
                writer.write(body)
            }
            
            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader().readText()
            } else {
                connection.errorStream?.bufferedReader()?.readText() ?: ""
            }
            
            connection.disconnect()
            HttpResult(responseCode, responseBody)
        } catch (e: Exception) {
            logger("HTTP POST 失败: ${e.message}")
            HttpResult(-1, e.message ?: "Unknown error")
        }
    }
    
    // ===== 定时器 =====
    
    /**
     * 设置延时执行
     * @param callback 回调函数名
     * @param delay 延时毫秒数
     * @return 定时器 ID
     */
    fun setTimeout(callback: () -> Unit, delay: Long): String {
        val id = "timeout_${++timerCounter}"
        val timer = Timer(id)
        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    callback()
                } catch (e: Exception) {
                    logger("setTimeout 回调错误: ${e.message}")
                } finally {
                    timers.remove(id)
                }
            }
        }, delay)
        timers[id] = timer
        return id
    }
    
    /**
     * 设置循环执行
     * @param callback 回调函数
     * @param interval 间隔毫秒数
     * @return 定时器 ID
     */
    fun setInterval(callback: () -> Unit, interval: Long): String {
        val id = "interval_${++timerCounter}"
        val timer = Timer(id)
        timer.schedule(object : TimerTask() {
            override fun run() {
                try {
                    callback()
                } catch (e: Exception) {
                    logger("setInterval 回调错误: ${e.message}")
                }
            }
        }, interval, interval)
        timers[id] = timer
        return id
    }
    
    /**
     * 清除定时器
     */
    fun clearTimeout(id: String) {
        timers.remove(id)?.cancel()
    }
    
    /**
     * 清除所有定时器
     */
    fun clearAllTimers() {
        timers.values.forEach { it.cancel() }
        timers.clear()
    }
    
    // ===== JSON 操作 =====
    
    /**
     * 解析 JSON 字符串
     */
    fun jsonParse(json: String): Any? {
        return try {
            com.google.gson.Gson().fromJson(json, Any::class.java)
        } catch (e: Exception) {
            logger("JSON 解析失败: ${e.message}")
            null
        }
    }
    
    /**
     * 对象转 JSON 字符串
     */
    fun jsonStringify(obj: Any): String {
        return try {
            com.google.gson.Gson().toJson(obj)
        } catch (e: Exception) {
            logger("JSON 序列化失败: ${e.message}")
            "{}"
        }
    }
    
    /**
     * 清理资源
     */
    fun destroy() {
        clearAllTimers()
    }
    
    /**
     * HTTP 响应结果
     */
    data class HttpResult(
        val statusCode: Int,
        val body: String
    ) {
        val isSuccess: Boolean get() = statusCode in 200..299
    }
}
