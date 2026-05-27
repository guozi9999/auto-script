package com.guozi.autoscript

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.guozi.autoscript.databinding.ActivityMainBinding
import java.io.File

/**
 * 主界面
 * 提供脚本编辑和执行功能
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var scriptRunner: ScriptRunner
    
    // 示例脚本
    private val sampleScripts = mapOf(
        "简单点击" to """
            // 在屏幕中央点击
            click(540, 960);
            log("点击完成");
        """.trimIndent(),
        
        "滑动操作" to """
            // 从下往上滑动（上滑）
            swipe(540, 1500, 540, 500, 300);
            log("上滑完成");
            sleep(1000);
            
            // 从上往下滑动（下滑）
            swipe(540, 500, 540, 1500, 300);
            log("下滑完成");
        """.trimIndent(),
        
        "循环点击" to """
            // 循环点击 5 次
            for (var i = 0; i < 5; i++) {
                click(540, 960);
                log("第 " + (i+1) + " 次点击");
                sleep(500);
            }
            log("循环完成");
        """.trimIndent(),
        
        "输入文字" to """
            // 点击输入框
            click(540, 500);
            sleep(300);
            
            // 输入文字
            input("Hello World!");
            log("输入完成");
        """.trimIndent(),
        
        "返回主页" to """
            // 按返回键
            back();
            sleep(500);
            
            // 按主页键
            home();
            log("已返回主页");
        """.trimIndent(),
        
        "点击文本" to """
            // 通过文本查找并点击
            clickText("确定");
            sleep(500);
            clickText("取消");
            log("点击完成");
        """.trimIndent(),
        
        "识图点击" to """
            // 查找图片并点击
            // 需要先将模板图片保存到手机
            var found = findAndClick("/sdcard/template.png");
            if (found) {
                log("找到并点击成功");
            } else {
                log("未找到图片");
            }
        """.trimIndent(),
        
        "颜色判断" to """
            // 获取指定坐标颜色
            var color = getColor(540, 960);
            log("颜色: " + color);
            
            // 判断颜色是否匹配
            if (colorMatch(540, 960, "#FF0000")) {
                log("颜色匹配");
            } else {
                log("颜色不匹配");
            }
        """.trimIndent(),
        
        "查找颜色" to """
            // 在屏幕中查找指定颜色
            var pos = findColor("#FF0000");
            if (pos) {
                log("找到颜色位置: " + pos.x + ", " + pos.y);
                click(pos.x, pos.y);
            } else {
                log("未找到颜色");
            }
        """.trimIndent()
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        scriptRunner = ScriptRunner(this)
        scriptRunner.onLog = { message ->
            runOnUiThread {
                appendLog(message)
            }
        }
        
        setupViews()
        checkPermissions()
    }
    
    private fun setupViews() {
        // 运行按钮
        binding.btnRun.setOnClickListener {
            val script = binding.etScript.text.toString()
            if (script.isBlank()) {
                Toast.makeText(this, "请输入脚本", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!isAccessibilityEnabled()) {
                showAccessibilityDialog()
                return@setOnClickListener
            }
            
            scriptRunner.execute(script)
        }
        
        // 停止按钮
        binding.btnStop.setOnClickListener {
            scriptRunner.stop()
        }
        
        // 清空日志
        binding.btnClearLog.setOnClickListener {
            binding.tvLog.text = ""
        }
        
        // 示例脚本
        binding.btnSample.setOnClickListener {
            showSampleDialog()
        }
        
        // 保存脚本
        binding.btnSave.setOnClickListener {
            saveScript()
        }
        
        // 加载脚本
        binding.btnLoad.setOnClickListener {
            loadScript()
        }
        
        // 悬浮窗按钮
        binding.btnFloating.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                requestOverlayPermission()
            } else {
                startFloatingService()
            }
        }
        
        // 截屏权限按钮
        binding.btnScreenCapture.setOnClickListener {
            if (!scriptRunner.getScreenCapture().hasPermission()) {
                requestScreenCapturePermission()
            } else {
                Toast.makeText(this, "截屏权限已获取", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun checkPermissions() {
        // 检查悬浮窗权限
        if (!Settings.canDrawOverlays(this)) {
            binding.btnFloating.text = "开启悬浮窗"
        } else {
            binding.btnFloating.text = "悬浮窗"
        }
        
        // 检查截屏权限
        if (scriptRunner.getScreenCapture().hasPermission()) {
            binding.btnScreenCapture.text = "截屏✓"
        } else {
            binding.btnScreenCapture.text = "截屏"
        }
    }
    
    private fun isAccessibilityEnabled(): Boolean {
        return AutoAccessibilityService.instance != null
    }
    
    private fun showAccessibilityDialog() {
        AlertDialog.Builder(this)
            .setTitle("需要开启无障碍服务")
            .setMessage("请在设置中找到 AutoScript 并开启无障碍服务")
            .setPositiveButton("去设置") { _, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun requestOverlayPermission() {
        AlertDialog.Builder(this)
            .setTitle("需要悬浮窗权限")
            .setMessage("悬浮窗功能需要授予悬浮窗权限")
            .setPositiveButton("去设置") { _, _ ->
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun requestScreenCapturePermission() {
        AlertDialog.Builder(this)
            .setTitle("需要截屏权限")
            .setMessage("识图功能需要截屏权限")
            .setPositiveButton("授权") { _, _ ->
                scriptRunner.getScreenCapture().requestPermission(this)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == ScreenCapture.REQUEST_CODE_SCREEN_CAPTURE) {
            if (scriptRunner.getScreenCapture().handlePermissionResult(resultCode, data)) {
                Toast.makeText(this, "截屏权限已获取", Toast.LENGTH_SHORT).show()
                checkPermissions()
            } else {
                Toast.makeText(this, "截屏权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun startFloatingService() {
        val intent = Intent(this, FloatingWindowService::class.java)
        startForegroundService(intent)
        Toast.makeText(this, "悬浮窗已开启", Toast.LENGTH_SHORT).show()
    }
    
    private fun showSampleDialog() {
        val names = sampleScripts.keys.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("选择示例脚本")
            .setItems(names) { _, which ->
                val name = names[which]
                val script = sampleScripts[name]
                binding.etScript.setText(script)
            }
            .show()
    }
    
    private fun saveScript() {
        val script = binding.etScript.text.toString()
        if (script.isBlank()) {
            Toast.makeText(this, "脚本为空", Toast.LENGTH_SHORT).show()
            return
        }
        
        val fileName = binding.etFileName.text.toString().ifBlank { "script.js" }
        val file = File(getExternalFilesDir("scripts"), fileName)
        file.writeText(script)
        Toast.makeText(this, "已保存: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
    }
    
    private fun loadScript() {
        val scriptsDir = getExternalFilesDir("scripts") ?: return
        val files = scriptsDir.listFiles()?.filter { it.extension == "js" }
        
        if (files.isNullOrEmpty()) {
            Toast.makeText(this, "没有保存的脚本", Toast.LENGTH_SHORT).show()
            return
        }
        
        val fileNames = files.map { it.name }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("选择脚本")
            .setItems(fileNames) { _, which ->
                val file = files[which]
                binding.etScript.setText(file.readText())
                binding.etFileName.setText(file.name)
            }
            .show()
    }
    
    private fun appendLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date())
        binding.tvLog.append("[$timestamp] $message\n")
        
        // 自动滚动到底部
        binding.scrollLog.post {
            binding.scrollLog.fullScroll(android.widget.ScrollView.FOCUS_DOWN)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        scriptRunner.destroy()
    }
}
