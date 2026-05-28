package com.guozi.autoscript

import java.util.UUID

/**
 * 积木块数据模型
 */
data class BlockModel(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType,
    val params: MutableList<String> = type.paramDefaults.toMutableList()
) {
    /**
     * 获取显示描述
     */
    fun getDescription(): String = type.getDescription(params)

    /**
     * 生成 JavaScript 代码
     */
    fun toJsCode(): String = type.toJsCode(params)
}
