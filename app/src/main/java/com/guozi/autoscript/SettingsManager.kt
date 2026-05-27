package com.guozi.autoscript

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * 设置管理器
 * 管理应用配置和用户偏好
 */
class SettingsManager(context: Context) {
    
    companion object {
        private const val TAG = "SettingsManager"
        private const val PREFS_NAME = "auto_script_settings"
        
        // 设置键
        private const val KEY_THEME = "theme"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_AUTO_SAVE = "auto_save"
        private const val KEY_SHOW_LOG = "show_log"
        private const val KEY_LOG_LEVEL = "log_level"
        private const val KEY_DEFAULT_CONFIDENCE = "default_confidence"
        private const val KEY_SCREENSHOT_DELAY = "screenshot_delay"
        private const val KEY_CLICK_DELAY = "click_delay"
        private const val KEY_SWIPE_DURATION = "swipe_duration"
        private const val KEY_ENABLE_FLOATING_WINDOW = "enable_floating_window"
        private const val KEY_FLOATING_WINDOW_OPACITY = "floating_window_opacity"
        private const val KEY_SCRIPT_ENCODING = "script_encoding"
        private const val KEY_AUTO_BACKUP = "auto_backup"
        private const val KEY_BACKUP_INTERVAL = "backup_interval"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // ===== 主题设置 =====
    
    var theme: String
        get() = prefs.getString(KEY_THEME, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_THEME, value).apply()
    
    var fontSize: Int
        get() = prefs.getInt(KEY_FONT_SIZE, 14)
        set(value) = prefs.edit().putInt(KEY_FONT_SIZE, value).apply()
    
    // ===== 编辑器设置 =====
    
    var autoSave: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SAVE, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SAVE, value).apply()
    
    var showLog: Boolean
        get() = prefs.getBoolean(KEY_SHOW_LOG, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_LOG, value).apply()
    
    var logLevel: String
        get() = prefs.getString(KEY_LOG_LEVEL, "debug") ?: "debug"
        set(value) = prefs.edit().putString(KEY_LOG_LEVEL, value).apply()
    
    // ===== 自动化设置 =====
    
    var defaultConfidence: Float
        get() = prefs.getFloat(KEY_DEFAULT_CONFIDENCE, 0.8f)
        set(value) = prefs.edit().putFloat(KEY_DEFAULT_CONFIDENCE, value).apply()
    
    var screenshotDelay: Long
        get() = prefs.getLong(KEY_SCREENSHOT_DELAY, 100)
        set(value) = prefs.edit().putLong(KEY_SCREENSHOT_DELAY, value).apply()
    
    var clickDelay: Long
        get() = prefs.getLong(KEY_CLICK_DELAY, 50)
        set(value) = prefs.edit().putLong(KEY_CLICK_DELAY, value).apply()
    
    var swipeDuration: Long
        get() = prefs.getLong(KEY_SWIPE_DURATION, 300)
        set(value) = prefs.edit().putLong(KEY_SWIPE_DURATION, value).apply()
    
    // ===== 悬浮窗设置 =====
    
    var enableFloatingWindow: Boolean
        get() = prefs.getBoolean(KEY_ENABLE_FLOATING_WINDOW, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLE_FLOATING_WINDOW, value).apply()
    
    var floatingWindowOpacity: Float
        get() = prefs.getFloat(KEY_FLOATING_WINDOW_OPACITY, 0.8f)
        set(value) = prefs.edit().putFloat(KEY_FLOATING_WINDOW_OPACITY, value).apply()
    
    // ===== 高级设置 =====
    
    var scriptEncoding: String
        get() = prefs.getString(KEY_SCRIPT_ENCODING, "UTF-8") ?: "UTF-8"
        set(value) = prefs.edit().putString(KEY_SCRIPT_ENCODING, value).apply()
    
    var autoBackup: Boolean
        get() = prefs.getBoolean(KEY_AUTO_BACKUP, false)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_BACKUP, value).apply()
    
    var backupInterval: Long
        get() = prefs.getLong(KEY_BACKUP_INTERVAL, 24 * 60 * 60 * 1000) // 默认24小时
        set(value) = prefs.edit().putLong(KEY_BACKUP_INTERVAL, value).apply()
    
    /**
     * 重置所有设置
     */
    fun resetAll() {
        prefs.edit().clear().apply()
        Log.d(TAG, "已重置所有设置")
    }
    
    /**
     * 导出设置
     */
    fun exportSettings(): Map<String, Any> {
        return prefs.all.toMap()
    }
    
    /**
     * 导入设置
     */
    fun importSettings(settings: Map<String, Any>) {
        val editor = prefs.edit()
        
        for ((key, value) in settings) {
            when (value) {
                is String -> editor.putString(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is Float -> editor.putFloat(key, value)
                is Boolean -> editor.putBoolean(key, value)
            }
        }
        
        editor.apply()
        Log.d(TAG, "已导入设置")
    }
    
    /**
     * 获取设置摘要
     */
    fun getSettingsSummary(): String {
        return """
            主题: $theme
            字体大小: ${fontSize}sp
            自动保存: ${if (autoSave) "开启" else "关闭"}
            显示日志: ${if (showLog) "开启" else "关闭"}
            日志级别: $logLevel
            默认置信度: ${(defaultConfidence * 100).toInt()}%
            截图延迟: ${screenshotDelay}ms
            点击延迟: ${clickDelay}ms
            滑动时长: ${swipeDuration}ms
            悬浮窗: ${if (enableFloatingWindow) "开启" else "关闭"}
            悬浮窗透明度: ${(floatingWindowOpacity * 100).toInt()}%
            脚本编码: $scriptEncoding
            自动备份: ${if (autoBackup) "开启" else "关闭"}
            备份间隔: ${backupInterval / (60 * 60 * 1000)}小时
        """.trimIndent()
    }
}
