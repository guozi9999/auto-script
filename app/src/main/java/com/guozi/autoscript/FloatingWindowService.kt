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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/**
 * 悬浮窗服务
 * 提供控制脚本执行的悬浮按钮
 */
@SuppressLint("StaticFieldLeak")
class FloatingWindowService : Service() {
    
    @Suppress("StaticFieldLeak")
    companion object {
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
    
    // 状态回调
    var onRunClick: (() -> Unit)? = null
    var onStopClick: (() -> Unit)? = null
    var onLogUpdate: ((String) -> Unit)? = null
    
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
        try {
            windowManager.removeView(floatingView)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AutoScript 服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "AutoScript 脚本执行服务"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("AutoScript")
                .setContentText("脚本服务运行中")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("AutoScript")
                .setContentText("脚本服务运行中")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build()
        }
    }
    
    private fun createFloatingWindow() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        
        // 创建悬浮窗布局
        floatingView = createFloatingView()
        
        // 设置窗口参数
        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 100
        }
        
        // 添加悬浮窗
        windowManager.addView(floatingView, layoutParams)
        
        // 设置拖拽
        setupDrag()
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
}
