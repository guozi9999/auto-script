package com.guozi.autoscript

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.guozi.autoscript.databinding.ItemBlockBinding
import java.util.Collections

/**
 * 积木块适配器
 */
class BlockAdapter(
    private val blocks: MutableList<BlockModel>,
    private val onBlockClick: (Int) -> Unit,
    private val onBlockLongClick: (Int) -> Unit,
    private val onDragStart: () -> Unit
) : RecyclerView.Adapter<BlockAdapter.BlockViewHolder>() {

    inner class BlockViewHolder(val binding: ItemBlockBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val binding = ItemBlockBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BlockViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: BlockViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val block = blocks[position]
        val binding = holder.binding

        // 设置图标
        binding.tvIcon.text = block.type.icon

        // 设置描述
        binding.tvDescription.text = block.getDescription()

        // 设置序号
        binding.tvIndex.text = "${position + 1}"

        // 条件分支内部缩进，帮助用户看清“成立时/否则时”的层级
        val depth = calculateIndent(position)
        (binding.root.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
            val density = holder.itemView.resources.displayMetrics.density
            params.marginStart = ((12 + depth * 28) * density).toInt()
            params.marginEnd = (12 * density).toInt()
            binding.root.layoutParams = params
        }

        // 设置背景颜色
        val bgDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 16f
            setColor(block.type.color.toInt())
            setStroke(2, Color.argb(40, 0, 0, 0))
        }
        binding.cardBlock.background = bgDrawable

        // 设置拖拽手柄触摸事件
        binding.tvDragHandle.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                onDragStart()
            }
            false
        }

        // 点击编辑
        binding.root.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onBlockClick(currentPosition)
            }
        }

        // 长按删除
        binding.root.setOnLongClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                onBlockLongClick(currentPosition)
            }
            true
        }
    }

    override fun getItemCount(): Int = blocks.size

    /**
     * 交换位置（拖拽时调用）
     */
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(blocks, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(blocks, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        // 更新受影响位置的序号
        val minPos = minOf(fromPosition, toPosition)
        val maxPos = maxOf(fromPosition, toPosition)
        notifyItemRangeChanged(minPos, maxPos - minPos + 1)
    }

    private fun calculateIndent(position: Int): Int {
        var depth = 0

        for (i in 0 until position) {
            val type = blocks[i].type
            if (type.closesCodeBlockBeforeLine()) {
                depth = maxOf(0, depth - 1)
            }
            if (type.opensCodeBlock()) {
                depth++
            }
        }

        if (blocks[position].type.closesCodeBlockBeforeLine()) {
            depth = maxOf(0, depth - 1)
        }

        return depth
    }
}
