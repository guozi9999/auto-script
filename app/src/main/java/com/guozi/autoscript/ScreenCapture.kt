package com.guozi.autoscript

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import java.io.File
import java.io.FileOutputStream

/**
 * 屏幕截图服务
 * 使用 MediaProjection API 实现截屏
 */
class ScreenCapture(private val context: Context) {
    
    companion object {
        private const val TAG = "ScreenCapture"
        private const val VIRTUAL_DISPLAY_NAME = "AutoScript Screen Capture"
        
        // 请求截屏权限的请求码
        const val REQUEST_CODE_SCREEN_CAPTURE = 1001
    }
    
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var screenWidth = 0
    private var screenHeight = 0
    private var screenDensity = 0
    
    private val handler = Handler(Looper.getMainLooper())
    
    init {
        // 获取屏幕尺寸
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        screenDensity = metrics.densityDpi
        
        Log.d(TAG, "屏幕尺寸: ${screenWidth}x${screenHeight}, 密度: $screenDensity")
    }
    
    /**
     * 请求截屏权限
     * 需要在 Activity 中调用
     */
    fun requestPermission(activity: Activity) {
        val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val captureIntent = projectionManager.createScreenCaptureIntent()
        activity.startActivityForResult(captureIntent, REQUEST_CODE_SCREEN_CAPTURE)
    }
    
    /**
     * 处理权限结果
     * 需要在 Activity.onActivityResult 中调用
     */
    fun handlePermissionResult(resultCode: Int, data: Intent?): Boolean {
        if (resultCode != Activity.RESULT_OK || data == null) {
            Log.e(TAG, "截屏权限被拒绝")
            return false
        }
        
        val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        
        if (mediaProjection == null) {
            Log.e(TAG, "无法创建 MediaProjection")
            return false
        }
        
        Log.d(TAG, "截屏权限已获取")
        return true
    }
    
    /**
     * 检查是否已获取权限
     */
    fun hasPermission(): Boolean {
        return mediaProjection != null
    }
    
    /**
     * 截取当前屏幕
     * @return Bitmap 截图，失败返回 null
     */
    fun capture(): Bitmap? {
        if (mediaProjection == null) {
            Log.e(TAG, "未获取截屏权限")
            return null
        }
        
        // 创建 ImageReader
        imageReader = ImageReader.newInstance(
            screenWidth, screenHeight,
            PixelFormat.RGBA_8888, 2
        )
        
        // 创建虚拟显示
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            VIRTUAL_DISPLAY_NAME,
            screenWidth, screenHeight, screenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface,
            null, handler
        )
        
        // 等待图像可用
        Thread.sleep(100)
        
        // 获取图像
        val image = imageReader?.acquireLatestImage()
        if (image == null) {
            Log.e(TAG, "无法获取屏幕图像")
            release()
            return null
        }
        
        // 转换为 Bitmap
        val bitmap = imageToBitmap(image)
        image.close()
        
        // 释放资源
        release()
        
        return bitmap
    }
    
    /**
     * 截图并保存到文件
     */
    fun captureToFile(filePath: String): Boolean {
        val bitmap = capture() ?: return false
        
        return try {
            val file = File(filePath)
            file.parentFile?.mkdirs()
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            bitmap.recycle()
            Log.d(TAG, "截图已保存: $filePath")
            true
        } catch (e: Exception) {
            Log.e(TAG, "保存截图失败: ${e.message}")
            false
        }
    }
    
    /**
     * 将 Image 转换为 Bitmap
     */
    private fun imageToBitmap(image: Image): Bitmap {
        val planes = image.planes
        val buffer = planes[0].buffer
        val pixelStride = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStride * screenWidth
        
        val bitmap = Bitmap.createBitmap(
            screenWidth + rowPadding / pixelStride,
            screenHeight,
            Bitmap.Config.ARGB_8888
        )
        bitmap.copyPixelsFromBuffer(buffer)
        
        // 裁剪到实际屏幕尺寸
        return Bitmap.createBitmap(bitmap, 0, 0, screenWidth, screenHeight)
    }
    
    /**
     * 释放资源
     */
    private fun release() {
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
    }
    
    /**
     * 销毁服务
     */
    fun destroy() {
        release()
        mediaProjection?.stop()
        mediaProjection = null
    }
}
