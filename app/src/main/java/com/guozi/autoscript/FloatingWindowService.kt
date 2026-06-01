package com.guozi.autoscript

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/**
 * 悬浮窗服务
 * 提供控制脚本执行的悬浮按钮
 */
@SuppressLint("StaticFieldLeak")
class FloatingWindowService : Service() {
    
    @Suppress("StaticFieldLeak")
    companion object {
        private const val TAG = "FloatingWindowService"
        private const val CHANNEL_ID = "auto_script_channel"
        private const val NOTIFICATION_ID = 1
        var instance: FloatingWindowService? = null
            private set
        var isRunning = false
            private set
    }
    
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private lateinit var layoutParams: WindowManager.LayoutParams
    private var coordinateOverlayWindowManager: WindowManager? = null
    
    // 状态回调
    var onRunClick: (() -> Unit)? = null
    var onStopClick: (() -> Unit)? = null
    var onLogUpdate: ((String) -> Unit)? = null
    
    // 坐标拾取模式
    private var isCoordinatePickerMode = false
    private var coordinateOverlay: View? = null
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        createFloatingWindow()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        isRunning = false
        isCoordinatePickerMode = false
        try {
            windowManager.removeView(floatingView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        coordinateOverlay?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        coordinateOverlay = null
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "自动脚本服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "自动脚本执行服务"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("自动脚本")
                .setContentText("脚本服务运行中")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("自动脚本")
                .setContentText("脚本服务运行中")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        }
    }
    
    private fun createFloatingWindow() {
        val preferredType = preferredWindowType()
        windowManager = windowManagerFor(preferredType)
        
        // 创建悬浮窗布局
        floatingView = createFloatingView()
        
        // 设置窗口参数
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            preferredType,
            floatingWindowFlags(),
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }
        
        // 添加悬浮窗
        addFloatingViewWithFallback()
        
        // 设置拖拽
        setupDrag()
    }

    private fun addFloatingViewWithFallback() {
        try {
            windowManager.addView(floatingView, layoutParams)
            Log.d(TAG, "悬浮窗已使用窗口类型: ${layoutParams.type}")
        } catch (e: Exception) {
            val normalType = normalWindowType()
            if (layoutParams.type == normalType) {
                throw e
            }

            Log.w(TAG, "高层级悬浮窗不可用，回退到普通悬浮窗", e)
            windowManager = appWindowManager()
            layoutParams.type = normalType
            windowManager.addView(floatingView, layoutParams)
        }
    }

    private fun appWindowManager(): WindowManager {
        return getSystemService(WINDOW_SERVICE) as WindowManager
    }

    private fun windowManagerFor(type: Int): WindowManager {
        return if (type == WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY) {
            AutoAccessibilityService.instance?.getSystemService(WINDOW_SERVICE) as? WindowManager
                ?: appWindowManager()
        } else {
            appWindowManager()
        }
    }

    private fun preferredWindowType(): Int {
        return if (AutoAccessibilityService.instance != null) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            normalWindowType()
        }
    }

    private fun normalWindowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun floatingWindowFlags(passThroughTouches: Boolean = false): Int {
        var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        if (passThroughTouches) {
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        }

        return flags
    }
    
    private fun createFloatingView(): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xE0333333.toInt())
            setPadding(16, 8, 16, 8)
        }
        
        // 状态文本
        val statusText = TextView(this).apply {
            text = "就绪"
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 12f
            tag = "status"
        }
        layout.addView(statusText)
        
        // 按钮容器
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 0)
        }
        
        // 运行按钮
        val runBtn = Button(this).apply {
            text = "▶"
            textSize = 16f
            setOnClickListener {
                onRunClick?.invoke()
            }
        }
        buttonLayout.addView(runBtn)
        
        // 停止按钮
        val stopBtn = Button(this).apply {
            text = "⏹"
            textSize = 16f
            setOnClickListener {
                onStopClick?.invoke()
            }
        }
        buttonLayout.addView(stopBtn)
        
        // 关闭按钮
        val closeBtn = Button(this).apply {
            text = "✕"
            textSize = 16f
            setOnClickListener {
                stopSelf()
            }
        }
        buttonLayout.addView(closeBtn)
        
        // 坐标拾取按钮
        val coordBtn = Button(this).apply {
            text = "📍"
            textSize = 16f
            tag = "coordBtn"
            setOnClickListener {
                toggleCoordinatePicker()
            }
        }
        buttonLayout.addView(coordBtn)
        
        layout.addView(buttonLayout)
        
        return layout
    }
    
    private fun setupDrag() {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        
        @Suppress("ClickableViewAccessibility")
        floatingView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams.x
                    initialY = layoutParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(floatingView, layoutParams)
                    true
                }
                else -> false
            }
        }
    }
    
    fun updateStatus(status: String) {
        floatingView.findViewWithTag<TextView>("status")?.text = status
    }
    
    private fun toggleCoordinatePicker() {
        if (isCoordinatePickerMode) {
            exitCoordinatePickerMode()
        } else {
            enterCoordinatePickerMode()
        }
    }
    
    private fun enterCoordinatePickerMode() {
        isCoordinatePickerMode = true
        floatingView.findViewWithTag<Button>("coordBtn")?.text = "❌"
        floatingView.findViewWithTag<TextView>("status")?.text = "点击屏幕获取坐标"
        
        // 更新悬浮窗为不拦截触摸事件
        layoutParams.flags = floatingWindowFlags(passThroughTouches = true)
        windowManager.updateViewLayout(floatingView, layoutParams)
        
        // 创建全屏透明覆盖层用于接收触摸事件
        val overlay = View(this).apply {
            setBackgroundColor(0x10FFFFFF)  // 半透明白色，让用户知道处于拾取模式
        }
        
        val overlayParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutParams.type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        
        overlay.setOnTouchListener { _, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                val x = event.rawX.toInt()
                val y = event.rawY.toInt()
                val coordText = "($x, $y)"
                
                // 复制坐标到剪贴板
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("坐标", coordText)
                clipboard.setPrimaryClip(clip)
                
                Toast.makeText(this, "坐标: $coordText (已复制)", Toast.LENGTH_SHORT).show()
                floatingView.findViewWithTag<TextView>("status")?.text = "坐标: $coordText"
                
                exitCoordinatePickerMode()
                true
            } else {
                false
            }
        }
        
        try {
            windowManager.addView(overlay, overlayParams)
            coordinateOverlayWindowManager = windowManager
            coordinateOverlay = overlay
        } catch (e: Exception) {
            val normalType = normalWindowType()
            if (overlayParams.type != normalType) {
                try {
                    overlayParams.type = normalType
                    val fallbackWindowManager = appWindowManager()
                    fallbackWindowManager.addView(overlay, overlayParams)
                    coordinateOverlayWindowManager = fallbackWindowManager
                    coordinateOverlay = overlay
                    return
                } catch (fallbackError: Exception) {
                    fallbackError.printStackTrace()
                }
            } else {
                e.printStackTrace()
            }
            exitCoordinatePickerMode()
        }
    }
    
    private fun exitCoordinatePickerMode() {
        isCoordinatePickerMode = false
        floatingView.findViewWithTag<Button>("coordBtn")?.text = "📍"
        floatingView.findViewWithTag<TextView>("status")?.text = "就绪"
        
        // 恢复悬浮窗触摸拦截
        layoutParams.flags = floatingWindowFlags()
        windowManager.updateViewLayout(floatingView, layoutParams)
        
        // 移除覆盖层
        coordinateOverlay?.let {
            try {
                (coordinateOverlayWindowManager ?: windowManager).removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        coordinateOverlayWindowManager = null
        coordinateOverlay = null
    }
}
