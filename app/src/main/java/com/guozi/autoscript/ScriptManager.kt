package com.guozi.autoscript

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 脚本管理器
 * 管理脚本的保存、加载、分类、收藏等
 */
class ScriptManager(private val context: Context) {
    
    companion object {
        private const val TAG = "ScriptManager"
        private const val SCRIPTS_DIR = "scripts"
        private const val FAVORITES_FILE = "favorites.json"
        private const val HISTORY_FILE = "history.json"
        private const val MAX_HISTORY = 50
    }
    
    private val gson = Gson()
    private val scriptsDir: File by lazy {
        File(context.getExternalFilesDir(null), SCRIPTS_DIR).apply { mkdirs() }
    }
    
    private val favoritesFile: File by lazy {
        File(context.getExternalFilesDir(null), FAVORITES_FILE)
    }
    
    private val historyFile: File by lazy {
        File(context.getExternalFilesDir(null), HISTORY_FILE)
    }
    
    // 收藏列表
    private val favorites = mutableSetOf<String>()
    
    // 历史记录
    private val history = mutableListOf<HistoryEntry>()
    
    init {
        loadFavorites()
        loadHistory()
    }
    
    /**
     * 保存脚本
     */
    fun saveScript(name: String, content: String, category: String = "default"): ScriptInfo {
        val categoryDir = File(scriptsDir, category).apply { mkdirs() }
        val file = File(categoryDir, "$name.js")
        file.writeText(content)
        
        val info = ScriptInfo(
            name = name,
            category = category,
            path = file.absolutePath,
            size = file.length(),
            lastModified = Date(file.lastModified()),
            isFavorite = favorites.contains(file.absolutePath)
        )
        
        // 添加到历史
        addToHistory(info)
        
        Log.d(TAG, "保存脚本: ${file.absolutePath}")
        return info
    }
    
    /**
     * 加载脚本
     */
    fun loadScript(path: String): String? {
        return try {
            val file = File(path)
            if (file.exists()) {
                file.readText()
            } else {
                Log.e(TAG, "脚本文件不存在: $path")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载脚本失败: ${e.message}")
            null
        }
    }
    
    /**
     * 删除脚本
     */
    fun deleteScript(path: String): Boolean {
        return try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
                favorites.remove(path)
                saveFavorites()
                Log.d(TAG, "删除脚本: $path")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "删除脚本失败: ${e.message}")
            false
        }
    }
    
    /**
     * 获取所有脚本
     */
    fun getAllScripts(): List<ScriptInfo> {
        val scripts = mutableListOf<ScriptInfo>()
        
        scriptsDir.listFiles()?.forEach { categoryDir ->
            if (categoryDir.isDirectory) {
                categoryDir.listFiles()?.forEach { file ->
                    if (file.extension == "js") {
                        scripts.add(ScriptInfo(
                            name = file.nameWithoutExtension,
                            category = categoryDir.name,
                            path = file.absolutePath,
                            size = file.length(),
                            lastModified = Date(file.lastModified()),
                            isFavorite = favorites.contains(file.absolutePath)
                        ))
                    }
                }
            }
        }
        
        return scripts.sortedByDescending { it.lastModified }
    }
    
    /**
     * 获取指定分类的脚本
     */
    fun getScriptsByCategory(category: String): List<ScriptInfo> {
        return getAllScripts().filter { it.category == category }
    }
    
    /**
     * 获取收藏的脚本
     */
    fun getFavoriteScripts(): List<ScriptInfo> {
        return getAllScripts().filter { it.isFavorite }
    }
    
    /**
     * 获取所有分类
     */
    fun getCategories(): List<String> {
        return scriptsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?: emptyList()
    }
    
    /**
     * 创建分类
     */
    fun createCategory(name: String): Boolean {
        return try {
            File(scriptsDir, name).mkdirs()
            true
        } catch (e: Exception) {
            Log.e(TAG, "创建分类失败: ${e.message}")
            false
        }
    }
    
    /**
     * 删除分类
     */
    fun deleteCategory(name: String): Boolean {
        return try {
            val dir = File(scriptsDir, name)
            if (dir.isDirectory) {
                dir.deleteRecursively()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "删除分类失败: ${e.message}")
            false
        }
    }
    
    /**
     * 切换收藏状态
     */
    fun toggleFavorite(path: String): Boolean {
        return if (favorites.contains(path)) {
            favorites.remove(path)
            saveFavorites()
            false
        } else {
            favorites.add(path)
            saveFavorites()
            true
        }
    }
    
    /**
     * 搜索脚本
     */
    fun searchScripts(query: String): List<ScriptInfo> {
        val lowerQuery = query.lowercase()
        return getAllScripts().filter { script ->
            script.name.lowercase().contains(lowerQuery) ||
            script.category.lowercase().contains(lowerQuery)
        }
    }
    
    /**
     * 获取历史记录
     */
    fun getHistory(): List<HistoryEntry> {
        return history.toList()
    }
    
    /**
     * 清空历史记录
     */
    fun clearHistory() {
        history.clear()
        saveHistory()
    }
    
    /**
     * 导出脚本
     */
    fun exportScript(path: String, exportPath: String): Boolean {
        return try {
            val source = File(path)
            val target = File(exportPath)
            source.copyTo(target, overwrite = true)
            Log.d(TAG, "导出脚本: $path -> $exportPath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "导出脚本失败: ${e.message}")
            false
        }
    }
    
    /**
     * 导入脚本
     */
    fun importScript(importPath: String, category: String = "default"): ScriptInfo? {
        return try {
            val source = File(importPath)
            if (!source.exists()) return null
            
            val name = source.nameWithoutExtension
            val categoryDir = File(scriptsDir, category).apply { mkdirs() }
            val target = File(categoryDir, "${name}.js")
            
            source.copyTo(target, overwrite = true)
            
            val info = ScriptInfo(
                name = name,
                category = category,
                path = target.absolutePath,
                size = target.length(),
                lastModified = Date(target.lastModified()),
                isFavorite = false
            )
            
            addToHistory(info)
            Log.d(TAG, "导入脚本: $importPath -> ${target.absolutePath}")
            info
        } catch (e: Exception) {
            Log.e(TAG, "导入脚本失败: ${e.message}")
            null
        }
    }
    
    /**
     * 添加到历史记录
     */
    private fun addToHistory(script: ScriptInfo) {
        history.add(0, HistoryEntry(
            script = script,
            action = "open",
            timestamp = Date()
        ))
        
        // 限制历史记录数量
        while (history.size > MAX_HISTORY) {
            history.removeAt(history.size - 1)
        }
        
        saveHistory()
    }
    
    /**
     * 加载收藏列表
     */
    private fun loadFavorites() {
        try {
            if (favoritesFile.exists()) {
                val type = object : TypeToken<Set<String>>() {}.type
                val loaded: Set<String> = gson.fromJson(favoritesFile.readText(), type)
                favorites.clear()
                favorites.addAll(loaded)
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载收藏列表失败: ${e.message}")
        }
    }
    
    /**
     * 保存收藏列表
     */
    private fun saveFavorites() {
        try {
            favoritesFile.writeText(gson.toJson(favorites))
        } catch (e: Exception) {
            Log.e(TAG, "保存收藏列表失败: ${e.message}")
        }
    }
    
    /**
     * 加载历史记录
     */
    private fun loadHistory() {
        try {
            if (historyFile.exists()) {
                val type = object : TypeToken<List<HistoryEntry>>() {}.type
                val loaded: List<HistoryEntry> = gson.fromJson(historyFile.readText(), type)
                history.clear()
                history.addAll(loaded)
            }
        } catch (e: Exception) {
            Log.e(TAG, "加载历史记录失败: ${e.message}")
        }
    }
    
    /**
     * 保存历史记录
     */
    private fun saveHistory() {
        try {
            historyFile.writeText(gson.toJson(history))
        } catch (e: Exception) {
            Log.e(TAG, "保存历史记录失败: ${e.message}")
        }
    }
    
    /**
     * 脚本信息
     */
    data class ScriptInfo(
        val name: String,
        val category: String,
        val path: String,
        val size: Long,
        val lastModified: Date,
        val isFavorite: Boolean
    ) {
        val sizeFormatted: String
            get() {
                return when {
                    size < 1024 -> "$size B"
                    size < 1024 * 1024 -> "${size / 1024} KB"
                    else -> "${size / (1024 * 1024)} MB"
                }
            }
        
        val lastModifiedFormatted: String
            get() {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                return sdf.format(lastModified)
            }
    }
    
    /**
     * 历史记录条目
     */
    data class HistoryEntry(
        val script: ScriptInfo,
        val action: String,
        val timestamp: Date
    ) {
        val timestampFormatted: String
            get() {
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                return sdf.format(timestamp)
            }
    }
}
