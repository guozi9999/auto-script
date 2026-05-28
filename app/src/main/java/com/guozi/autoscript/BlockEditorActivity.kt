package com.guozi.autoscript

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.EditText
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
        val types = BlockType.entries.toTypedArray()
        val items = types.map { "${it.icon}  ${it.displayName}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("选择积木块")
            .setItems(items) { _, which ->
                val block = BlockModel(type = types[which])
                blocks.add(block)
                adapter.notifyItemInserted(blocks.size - 1)
                binding.rvBlocks.smoothScrollToPosition(blocks.size - 1)
                updateEmptyState()
                updateBlockCount()
            }
            .show()
    }

    /**
     * 显示编辑对话框
     */
    private fun showEditDialog(position: Int) {
        val block = blocks[position]
        val type = block.type

        if (type.paramKeys.isEmpty()) {
            Toast.makeText(this, "此积木块没有可配置参数", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("${type.icon} 编辑 ${type.displayName}")

        // 创建输入框
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val editTexts = mutableListOf<EditText>()

        type.paramKeys.forEachIndexed { index, key ->
            val label = TextView(this).apply {
                text = key
                textSize = 14f
                setTextColor(Color.DKGRAY)
                setPadding(0, 16, 0, 4)
            }
            layout.addView(label)

            val editText = EditText(this).apply {
                setText(block.params.getOrElse(index) { type.paramDefaults[index] })
                textSize = 16f
                setPadding(16, 12, 16, 12)
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 8f
                    setStroke(2, Color.parseColor("#CCCCCC"))
                }
                background = bg
                // 根据参数类型设置输入法
                inputType = if (key.contains("文字") || key.contains("文本") || key.contains("内容")) {
                    android.text.InputType.TYPE_CLASS_TEXT
                } else {
                    android.text.InputType.TYPE_CLASS_NUMBER
                }
            }
            layout.addView(editText)
            editTexts.add(editText)
        }

        val scrollView = ScrollView(this).apply {
            addView(layout)
        }
        builder.setView(scrollView)

        builder.setPositiveButton("确定") { _, _ ->
            editTexts.forEachIndexed { index, editText ->
                if (index < block.params.size) {
                    block.params[index] = editText.text.toString()
                }
            }
            adapter.notifyItemChanged(position)
        }

        builder.setNegativeButton("取消", null)
        builder.show()
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
        return blocks.joinToString("\n") { it.toJsCode() }
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

        AlertDialog.Builder(this)
            .setTitle("生成的 JavaScript 代码")
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

        // 输入文件名
        val editText = EditText(this).apply {
            hint = "请输入文件名"
            setText("block_script.js")
            setPadding(48, 24, 48, 24)
        }

        AlertDialog.Builder(this)
            .setTitle("保存脚本")
            .setView(editText)
            .setPositiveButton("保存") { _, _ ->
                val fileName = editText.text.toString().ifBlank { "block_script.js" }
                val file = File(getExternalFilesDir("scripts"), fileName)
                file.parentFile?.mkdirs()
                file.writeText(code)
                Toast.makeText(this, "已保存: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("取消", null)
            .show()
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

        // 检查无障碍服务
        if (AutoAccessibilityService.instance == null) {
            AlertDialog.Builder(this)
                .setTitle("需要开启无障碍服务")
                .setMessage("请在设置中找到 AutoScript 并开启无障碍服务")
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
}
