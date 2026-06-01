package com.guozi.autoscript

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.guozi.autoscript.databinding.ActivityBlockEditorBinding
import java.io.File

/**
 * 积木块编辑器 - 类似 Scratch 的可视化编程界面
 */
class BlockEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlockEditorBinding
    private lateinit var adapter: BlockAdapter
    private val blocks = mutableListOf<BlockModel>()
    private var itemTouchHelper: ItemTouchHelper? = null
    private val conditionTypes = listOf(
        BlockType.IF_TEXT_EXISTS,
        BlockType.IF_TEXT_NOT_EXISTS,
        BlockType.IF_IMAGE_FOUND,
        BlockType.IF_IMAGE_NOT_FOUND,
        BlockType.IF_COLOR_MATCH,
        BlockType.IF_FILE_EXISTS,
        BlockType.IF_CUSTOM_CONDITION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupAddButton()
        updateEmptyState()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        // 添加菜单按钮
        binding.toolbar.menu.apply {
            add("保存").setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM)
            add("运行").setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM)
            add("生成代码").setShowAsAction(android.view.MenuItem.SHOW_AS_ACTION_NEVER)
        }

        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.title) {
                "保存" -> saveScript()
                "运行" -> runScript()
                "生成代码" -> showGeneratedCode()
            }
            true
        }
    }

    private fun setupRecyclerView() {
        adapter = BlockAdapter(
            blocks = blocks,
            onBlockClick = { position -> showEditDialog(position) },
            onBlockLongClick = { position -> showDeleteDialog(position) },
            onDragStart = { /* handled by touch helper */ }
        )

        binding.rvBlocks.layoutManager = LinearLayoutManager(this)
        binding.rvBlocks.adapter = adapter

        // 拖拽支持
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val from = viewHolder.adapterPosition
                val to = target.adapterPosition
                adapter.onItemMove(from, to)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // 不处理滑动删除
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.8f
                    viewHolder?.itemView?.scaleX = 1.05f
                    viewHolder?.itemView?.scaleY = 1.05f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
                viewHolder.itemView.scaleX = 1.0f
                viewHolder.itemView.scaleY = 1.0f
            }
        }

        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper?.attachToRecyclerView(binding.rvBlocks)
    }

    private fun setupAddButton() {
        binding.btnAddBlock.setOnClickListener {
            showBlockTypeDialog()
        }
    }

    /**
     * 显示积木块类型选择对话框
     */
    private fun showBlockTypeDialog() {
        val actionTypes = BlockType.entries
            .filterNot { it.isConditionStart() || it.isBranchMarker() }
            .toTypedArray()
        val items = mutableListOf("🔀  条件分支")
        items.addAll(actionTypes.map { "${it.icon}  ${it.displayName}" })

        AlertDialog.Builder(this)
            .setTitle("选择积木块")
            .setItems(items.toTypedArray()) { _, which ->
                if (which == 0) {
                    showConditionTypeDialog()
                } else {
                    showCreateBlockDialog(actionTypes[which - 1])
                }
            }
            .show()
    }

    /**
     * 显示条件类型选择对话框
     */
    private fun showConditionTypeDialog() {
        val items = conditionTypes
            .map { "${it.icon}  ${it.displayName}" }
            .toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("选择条件")
            .setMessage("选择一个容易理解的条件，系统会自动添加“否则”和“结束条件分支”。")
            .setItems(items) { _, which ->
                showCreateBlockDialog(conditionTypes[which], insertBranchScaffold = true)
            }
            .show()
    }

    /**
     * 创建新积木块，条件分支会自动带出否则和结束标记
     */
    private fun showCreateBlockDialog(
        type: BlockType,
        insertBranchScaffold: Boolean = false
    ) {
        if (type.paramKeys.isEmpty()) {
            addBlock(type)
            return
        }

        val title = if (insertBranchScaffold) {
            "${type.icon} 创建条件分支"
        } else {
            "${type.icon} 添加 ${type.displayName}"
        }
        val positiveText = if (insertBranchScaffold) "创建分支" else "添加"

        showParamDialog(
            title = title,
            type = type,
            params = type.paramDefaults,
            positiveText = positiveText
        ) { params ->
            addBlock(type, params, insertBranchScaffold)
        }
    }

    /**
     * 显示编辑对话框
     */
    private fun showEditDialog(position: Int) {
        if (position !in blocks.indices) return
        
        val block = blocks[position]
        val type = block.type

        if (type.paramKeys.isEmpty()) {
            if (type.isBranchMarker()) {
                Toast.makeText(this, "这是条件分支的结构标记，不需要填写内容", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "此积木块没有可配置参数", Toast.LENGTH_SHORT).show()
            }
            return
        }

        showParamDialog(
            title = "${type.icon} 编辑 ${type.displayName}",
            type = type,
            params = block.params,
            positiveText = "确定"
        ) { params ->
            block.params.clear()
            block.params.addAll(params)
            adapter.notifyItemChanged(position)
        }
    }

    /**
     * 显示参数填写对话框
     */
    private fun showParamDialog(
        title: String,
        type: BlockType,
        params: List<String>,
        positiveText: String,
        onConfirm: (List<String>) -> Unit
    ) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)

        // 创建输入框
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val paramReaders = mutableListOf<() -> String>()

        type.paramKeys.forEachIndexed { index, key ->
            val label = TextView(this).apply {
                text = key
                textSize = 14f
                setTextColor(Color.DKGRAY)
                setPadding(0, 16, 0, 4)
            }
            layout.addView(label)

            if (isFilePoolParam(key)) {
                val selectedValue = params.getOrElse(index) { type.paramDefaults.getOrElse(index) { "" } }
                val valueView = TextView(this).apply {
                    text = selectedValue.ifBlank { "点击从文件池选择" }
                    textSize = 16f
                    setTextColor(Color.parseColor("#333333"))
                    setPadding(16, 18, 16, 18)
                    val bg = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 8f
                        setStroke(2, Color.parseColor("#CCCCCC"))
                        setColor(Color.WHITE)
                    }
                    background = bg
                    setOnClickListener {
                        showFilePoolPicker(key) { path ->
                            text = path
                        }
                    }
                }
                layout.addView(valueView)
                paramReaders.add {
                    val text = valueView.text.toString()
                    if (text == "点击从文件池选择") "" else text
                }
            } else {
                val editText = EditText(this).apply {
                    setText(params.getOrElse(index) { type.paramDefaults.getOrElse(index) { "" } })
                    textSize = 16f
                    setPadding(16, 12, 16, 12)
                    val bg = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 8f
                        setStroke(2, Color.parseColor("#CCCCCC"))
                    }
                    background = bg
                    inputType = getInputTypeForParam(key)
                }
                layout.addView(editText)
                paramReaders.add { editText.text.toString() }
            }
        }

        val scrollView = ScrollView(this).apply {
            addView(layout)
        }
        builder.setView(scrollView)

        builder.setPositiveButton(positiveText) { _, _ ->
            onConfirm(paramReaders.map { it() })
        }

        builder.setNegativeButton("取消", null)
        builder.show()
    }

    private fun addBlock(
        type: BlockType,
        params: List<String> = type.paramDefaults,
        insertBranchScaffold: Boolean = false
    ) {
        val insertPosition = blocks.size
        blocks.add(BlockModel(type = type, params = params.toMutableList()))

        if (insertBranchScaffold) {
            blocks.add(BlockModel(type = BlockType.ELSE))
            blocks.add(BlockModel(type = BlockType.END_IF))
        }

        val insertedCount = if (insertBranchScaffold) 3 else 1
        adapter.notifyItemRangeInserted(insertPosition, insertedCount)
        binding.rvBlocks.smoothScrollToPosition(blocks.size - 1)
        updateEmptyState()
        updateBlockCount()
    }

    /**
     * 显示删除确认对话框
     */
    private fun showDeleteDialog(position: Int) {
        val block = blocks[position]
        AlertDialog.Builder(this)
            .setTitle("删除积木块")
            .setMessage("确定要删除 \"${block.getDescription()}\" 吗？")
            .setPositiveButton("删除") { _, _ ->
                blocks.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, blocks.size - position)
                updateEmptyState()
                updateBlockCount()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    /**
     * 生成 JavaScript 代码
     */
    private fun generateJsCode(): String {
        if (blocks.isEmpty()) return ""
        
        val lines = mutableListOf<String>()
        var indent = 0
        blocks.forEach { block ->
            if (block.type.closesCodeBlockBeforeLine()) {
                indent = maxOf(0, indent - 1)
            }
            
            lines.add("    ".repeat(indent) + block.toJsCode())
            
            if (block.type.opensCodeBlock()) {
                indent++
            }
        }
        return lines.joinToString("\n")
    }

    /**
     * 显示生成的代码
     */
    private fun showGeneratedCode() {
        val code = generateJsCode()
        if (code.isBlank()) {
            Toast.makeText(this, "没有积木块，无法生成代码", Toast.LENGTH_SHORT).show()
            return
        }
        validateRequiredParams()?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            return
        }
        validateBranchBlocks()?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("生成的脚本代码")
            .setMessage(code)
            .setPositiveButton("确定", null)
            .setNeutralButton("复制") { _, _ ->
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("js_code", code)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "代码已复制到剪贴板", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    /**
     * 保存脚本
     */
    private fun saveScript() {
        val code = generateJsCode()
        if (code.isBlank()) {
            Toast.makeText(this, "没有积木块，无法保存", Toast.LENGTH_SHORT).show()
            return
        }
        validateRequiredParams()?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            return
        }
        validateBranchBlocks()?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            return
        }

        // 输入文件名
        val editText = EditText(this).apply {
            hint = "请输入文件名"
            setText("积木脚本")
            setPadding(48, 24, 48, 24)
        }

        AlertDialog.Builder(this)
            .setTitle("保存脚本")
            .setView(editText)
            .setPositiveButton("保存") { _, _ ->
                val fileName = scriptFileNameFrom(editText.text.toString(), "积木脚本")
                val file = File(getExternalFilesDir("scripts"), fileName)
                file.parentFile?.mkdirs()
                file.writeText(code)
                Toast.makeText(this, "已保存: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    private fun scriptFileNameFrom(input: String, defaultName: String): String {
        val trimmed = input.trim().ifBlank { defaultName }
        return if (trimmed.endsWith(".js", ignoreCase = true)) trimmed else "$trimmed.js"
    }

    /**
     * 运行脚本
     */
    private fun runScript() {
        val code = generateJsCode()
        if (code.isBlank()) {
            Toast.makeText(this, "没有积木块，无法运行", Toast.LENGTH_SHORT).show()
            return
        }
        validateRequiredParams()?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            return
        }
        validateBranchBlocks()?.let { error ->
            Toast.makeText(this, error, Toast.LENGTH_LONG).show()
            return
        }

        // 检查无障碍服务
        if (AutoAccessibilityService.instance == null) {
            AlertDialog.Builder(this)
                .setTitle("需要开启无障碍服务")
                .setMessage("请在设置中找到自动脚本并开启无障碍服务")
                .setPositiveButton("去设置") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                .setNegativeButton("取消", null)
                .show()
            return
        }

        // 跳转到主界面并运行
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("run_script", code)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
        Toast.makeText(this, "脚本已发送到主界面运行", Toast.LENGTH_SHORT).show()
    }

    /**
     * 更新空状态显示
     */
    private fun updateEmptyState() {
        if (blocks.isEmpty()) {
            binding.tvEmpty.visibility = android.view.View.VISIBLE
            binding.rvBlocks.visibility = android.view.View.GONE
        } else {
            binding.tvEmpty.visibility = android.view.View.GONE
            binding.rvBlocks.visibility = android.view.View.VISIBLE
        }
    }

    /**
     * 更新积木块计数
     */
    private fun updateBlockCount() {
        binding.tvBlockCount.text = "共 ${blocks.size} 块"
    }
    
    private fun getInputTypeForParam(key: String): Int {
        return if (key.contains("坐标") || key.contains("时间") || key.contains("毫秒")) {
            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        }
    }

    private fun isFilePoolParam(key: String): Boolean {
        return key.contains("图片路径") || key.contains("文件路径")
    }

    private fun showFilePoolPicker(key: String, onSelected: (String) -> Unit) {
        val imageOnly = key.contains("图片路径")
        val files = getFilePoolFiles(imageOnly = imageOnly)

        if (files.isEmpty()) {
            val message = if (imageOnly) {
                "文件池里还没有图片，请先导入图片。"
            } else {
                "文件池里还没有可选文件。"
            }
            AlertDialog.Builder(this)
                .setTitle("文件池为空")
                .setMessage(message)
                .setPositiveButton("去文件池") { _, _ ->
                    startActivity(Intent(this, FilePoolActivity::class.java))
                }
                .setNegativeButton("取消", null)
                .show()
            return
        }

        if (imageOnly) {
            showImageFilePicker(files, onSelected)
            return
        }

        val baseDir = getExternalFilesDir(null)
        val names = files.map { file ->
            baseDir?.let { file.relativeToOrSelf(it).path } ?: file.name
        }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("从文件池选择文件")
            .setItems(names) { _, which ->
                onSelected(files[which].absolutePath)
            }
            .show()
    }

    private fun showImageFilePicker(files: List<File>, onSelected: (String) -> Unit) {
        val baseDir = getExternalFilesDir(null)
        val list = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        lateinit var dialog: AlertDialog

        files.forEach { file ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(16), dp(10), dp(16), dp(10))
                background = selectableItemBackground()
                setOnClickListener {
                    onSelected(file.absolutePath)
                    dialog.dismiss()
                }
            }

            val thumbnail = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(dp(72), dp(72))
                scaleType = ImageView.ScaleType.CENTER_CROP
                setBackgroundColor(Color.parseColor("#EEEEEE"))
                setImageBitmap(decodeScaledBitmap(file, 180, 180))
                setOnClickListener {
                    showImagePreviewDialog(file)
                }
            }
            row.addView(thumbnail)

            val labelGroup = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(12), 0, 0, 0)
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }

            labelGroup.addView(TextView(this).apply {
                text = file.name
                textSize = 15f
                setTextColor(Color.parseColor("#222222"))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            })

            labelGroup.addView(TextView(this).apply {
                text = baseDir?.let { file.relativeToOrSelf(it).path } ?: file.absolutePath
                textSize = 12f
                setTextColor(Color.parseColor("#777777"))
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
            })

            labelGroup.addView(TextView(this).apply {
                text = "点击缩略图可放大"
                textSize = 11f
                setTextColor(Color.parseColor("#999999"))
            })

            row.addView(labelGroup)
            list.addView(row)

            list.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    dp(1)
                )
                setBackgroundColor(Color.parseColor("#EEEEEE"))
            })
        }

        val scrollView = ScrollView(this).apply {
            addView(list)
        }

        dialog = AlertDialog.Builder(this)
            .setTitle("从文件池选择图片")
            .setView(scrollView)
            .setNegativeButton("取消", null)
            .create()
        dialog.show()
    }

    private fun showImagePreviewDialog(file: File) {
        val imageView = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setBackgroundColor(Color.BLACK)
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setImageBitmap(decodeScaledBitmap(file, 1600, 1600))
        }

        AlertDialog.Builder(this)
            .setTitle(file.name)
            .setView(imageView)
            .setPositiveButton("关闭", null)
            .show()
    }

    private fun getFilePoolFiles(imageOnly: Boolean): List<File> {
        val baseDir = getExternalFilesDir(null) ?: return emptyList()
        val imageExtensions = setOf("png", "jpg", "jpeg", "webp", "bmp")

        return if (imageOnly) {
            File(baseDir, "images")
                .listFiles()
                ?.filter { it.isFile && it.extension.lowercase() in imageExtensions }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } else {
            baseDir.walkTopDown()
                .filter { it.isFile }
                .sortedByDescending { it.lastModified() }
                .toList()
        }
    }

    private fun decodeScaledBitmap(file: File, reqWidth: Int, reqHeight: Int): android.graphics.Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(file.absolutePath, options)

        var sampleSize = 1
        while (
            options.outHeight / sampleSize > reqHeight ||
            options.outWidth / sampleSize > reqWidth
        ) {
            sampleSize *= 2
        }

        return BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply { inSampleSize = sampleSize }
        )
    }

    private fun selectableItemBackground(): android.graphics.drawable.Drawable? {
        val attrs = intArrayOf(android.R.attr.selectableItemBackground)
        val typedArray = obtainStyledAttributes(attrs)
        return typedArray.getDrawable(0).also {
            typedArray.recycle()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun validateRequiredParams(): String? {
        blocks.forEachIndexed { blockIndex, block ->
            block.type.paramKeys.forEachIndexed { paramIndex, key ->
                if (isFilePoolParam(key) && block.params.getOrNull(paramIndex).isNullOrBlank()) {
                    val target = if (key.contains("图片路径")) "图片" else "文件"
                    return "第 ${blockIndex + 1} 块“${block.type.displayName}”还没有选择$target"
                }
            }
        }
        return null
    }
    
    private fun validateBranchBlocks(): String? {
        val branchStack = mutableListOf<Boolean>()
        
        blocks.forEachIndexed { index, block ->
            when {
                block.type.isConditionStart() -> {
                    branchStack.add(false)
                }
                
                block.type == BlockType.ELSE -> {
                    if (branchStack.isEmpty()) {
                        return "第 ${index + 1} 块“否则执行”前缺少条件分支"
                    }
                    if (branchStack.last()) {
                        return "第 ${index + 1} 块“否则执行”重复了"
                    }
                    branchStack[branchStack.lastIndex] = true
                }
                
                block.type == BlockType.END_IF -> {
                    if (branchStack.isEmpty()) {
                        return "第 ${index + 1} 块“结束条件分支”前缺少条件分支"
                    }
                    branchStack.removeAt(branchStack.lastIndex)
                }
            }
        }
        
        return if (branchStack.isNotEmpty()) {
            "有 ${branchStack.size} 个条件分支缺少“结束条件分支”"
        } else {
            null
        }
    }
}
