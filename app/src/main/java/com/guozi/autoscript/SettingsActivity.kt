package com.guozi.autoscript

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * 设置界面
 */
@SuppressLint("SetTextI18n")
class SettingsActivity : AppCompatActivity() {
    
    private lateinit var settingsManager: SettingsManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        settingsManager = SettingsManager(this)
        
        setupViews()
        loadSettings()
    }
    
    private fun setupViews() {
        // 主题选择
        val spinnerTheme = findViewById<Spinner>(R.id.spinnerTheme)
        spinnerTheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val themes = arrayOf("system", "light", "dark")
                settingsManager.theme = themes[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 字体大小
        val seekBarFontSize = findViewById<SeekBar>(R.id.seekBarFontSize)
        val tvFontSize = findViewById<TextView>(R.id.tvFontSize)
        seekBarFontSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvFontSize.text = "${progress}sp"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                settingsManager.fontSize = seekBar?.progress ?: 14
            }
        })
        
        // 自动保存
        val cbAutoSave = findViewById<CheckBox>(R.id.cbAutoSave)
        cbAutoSave.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.autoSave = isChecked
        }
        
        // 显示日志
        val cbShowLog = findViewById<CheckBox>(R.id.cbShowLog)
        cbShowLog.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.showLog = isChecked
        }
        
        // 日志级别
        val spinnerLogLevel = findViewById<Spinner>(R.id.spinnerLogLevel)
        spinnerLogLevel.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val levels = arrayOf("verbose", "debug", "info", "warning", "error")
                settingsManager.logLevel = levels[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // 置信度
        val seekBarConfidence = findViewById<SeekBar>(R.id.seekBarConfidence)
        val tvConfidence = findViewById<TextView>(R.id.tvConfidence)
        seekBarConfidence.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvConfidence.text = "${progress}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                settingsManager.defaultConfidence = (seekBar?.progress ?: 80) / 100f
            }
        })
        
        // 截图延迟
        val etScreenshotDelay = findViewById<EditText>(R.id.etScreenshotDelay)
        
        // 点击延迟
        val etClickDelay = findViewById<EditText>(R.id.etClickDelay)
        
        // 滑动时长
        val etSwipeDuration = findViewById<EditText>(R.id.etSwipeDuration)
        
        // 悬浮窗
        val cbFloatingWindow = findViewById<CheckBox>(R.id.cbFloatingWindow)
        cbFloatingWindow.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.enableFloatingWindow = isChecked
        }
        
        // 悬浮窗透明度
        val seekBarOpacity = findViewById<SeekBar>(R.id.seekBarOpacity)
        val tvOpacity = findViewById<TextView>(R.id.tvOpacity)
        seekBarOpacity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvOpacity.text = "${progress}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                settingsManager.floatingWindowOpacity = (seekBar?.progress ?: 80) / 100f
            }
        })
        
        // 自动备份
        val cbAutoBackup = findViewById<CheckBox>(R.id.cbAutoBackup)
        cbAutoBackup.setOnCheckedChangeListener { _, isChecked ->
            settingsManager.autoBackup = isChecked
        }
        
        // 备份间隔
        val etBackupInterval = findViewById<EditText>(R.id.etBackupInterval)
        
        // 保存按钮
        val btnSave = findViewById<Button>(R.id.btnSave)
        btnSave.setOnClickListener {
            saveSettings()
            Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show()
            finish()
        }
        
        // 重置按钮
        val btnReset = findViewById<Button>(R.id.btnReset)
        btnReset.setOnClickListener {
            settingsManager.resetAll()
            loadSettings()
            Toast.makeText(this, "已重置为默认设置", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadSettings() {
        // 主题
        val spinnerTheme = findViewById<Spinner>(R.id.spinnerTheme)
        val themes = arrayOf("system", "light", "dark")
        spinnerTheme.setSelection(themes.indexOf(settingsManager.theme))
        
        // 字体大小
        val seekBarFontSize = findViewById<SeekBar>(R.id.seekBarFontSize)
        val tvFontSize = findViewById<TextView>(R.id.tvFontSize)
        seekBarFontSize.progress = settingsManager.fontSize
        tvFontSize.text = "${settingsManager.fontSize}sp"
        
        // 自动保存
        val cbAutoSave = findViewById<CheckBox>(R.id.cbAutoSave)
        cbAutoSave.isChecked = settingsManager.autoSave
        
        // 显示日志
        val cbShowLog = findViewById<CheckBox>(R.id.cbShowLog)
        cbShowLog.isChecked = settingsManager.showLog
        
        // 日志级别
        val spinnerLogLevel = findViewById<Spinner>(R.id.spinnerLogLevel)
        val levels = arrayOf("verbose", "debug", "info", "warning", "error")
        spinnerLogLevel.setSelection(levels.indexOf(settingsManager.logLevel))
        
        // 置信度
        val seekBarConfidence = findViewById<SeekBar>(R.id.seekBarConfidence)
        val tvConfidence = findViewById<TextView>(R.id.tvConfidence)
        seekBarConfidence.progress = (settingsManager.defaultConfidence * 100).toInt()
        tvConfidence.text = "${seekBarConfidence.progress}%"
        
        // 延迟设置
        val etScreenshotDelay = findViewById<EditText>(R.id.etScreenshotDelay)
        etScreenshotDelay.setText(settingsManager.screenshotDelay.toString())
        
        val etClickDelay = findViewById<EditText>(R.id.etClickDelay)
        etClickDelay.setText(settingsManager.clickDelay.toString())
        
        val etSwipeDuration = findViewById<EditText>(R.id.etSwipeDuration)
        etSwipeDuration.setText(settingsManager.swipeDuration.toString())
        
        // 悬浮窗
        val cbFloatingWindow = findViewById<CheckBox>(R.id.cbFloatingWindow)
        cbFloatingWindow.isChecked = settingsManager.enableFloatingWindow
        
        // 悬浮窗透明度
        val seekBarOpacity = findViewById<SeekBar>(R.id.seekBarOpacity)
        val tvOpacity = findViewById<TextView>(R.id.tvOpacity)
        seekBarOpacity.progress = (settingsManager.floatingWindowOpacity * 100).toInt()
        tvOpacity.text = "${seekBarOpacity.progress}%"
        
        // 自动备份
        val cbAutoBackup = findViewById<CheckBox>(R.id.cbAutoBackup)
        cbAutoBackup.isChecked = settingsManager.autoBackup
        
        // 备份间隔
        val etBackupInterval = findViewById<EditText>(R.id.etBackupInterval)
        etBackupInterval.setText((settingsManager.backupInterval / (60 * 60 * 1000)).toString())
    }
    
    private fun saveSettings() {
        // 延迟设置
        val etScreenshotDelay = findViewById<EditText>(R.id.etScreenshotDelay)
        settingsManager.screenshotDelay = etScreenshotDelay.text.toString().toLongOrNull() ?: 100
        
        val etClickDelay = findViewById<EditText>(R.id.etClickDelay)
        settingsManager.clickDelay = etClickDelay.text.toString().toLongOrNull() ?: 50
        
        val etSwipeDuration = findViewById<EditText>(R.id.etSwipeDuration)
        settingsManager.swipeDuration = etSwipeDuration.text.toString().toLongOrNull() ?: 300
        
        // 备份间隔
        val etBackupInterval = findViewById<EditText>(R.id.etBackupInterval)
        val hours = etBackupInterval.text.toString().toLongOrNull() ?: 24
        settingsManager.backupInterval = hours * 60 * 60 * 1000
    }
}
