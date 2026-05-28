package com.guozi.autoscript

/**
 * 积木块类型定义
 */
enum class BlockType(
    val displayName: String,
    val icon: String,
    val color: Long,
    val paramKeys: List<String>,    // 参数名称列表
    val paramDefaults: List<String>  // 参数默认值列表
) {
    CLICK(
        displayName = "点击屏幕",
        icon = "👆",
        color = 0xFF4CAF50,
        paramKeys = listOf("X 坐标", "Y 坐标"),
        paramDefaults = listOf("540", "960")
    ),
    LONG_CLICK(
        displayName = "长按屏幕",
        icon = "👇",
        color = 0xFF388E3C,
        paramKeys = listOf("X 坐标", "Y 坐标", "持续时间(毫秒)"),
        paramDefaults = listOf("540", "960", "1000")
    ),
    SWIPE(
        displayName = "滑动",
        icon = "👉",
        color = 0xFF2196F3,
        paramKeys = listOf("起点 X", "起点 Y", "终点 X", "终点 Y"),
        paramDefaults = listOf("100", "500", "100", "200")
    ),
    INPUT(
        displayName = "输入文字",
        icon = "⌨️",
        color = 0xFFFF9800,
        paramKeys = listOf("文字内容"),
        paramDefaults = listOf("Hello")
    ),
    SLEEP(
        displayName = "等待",
        icon = "⏱️",
        color = 0xFF9C27B0,
        paramKeys = listOf("等待时间(毫秒)"),
        paramDefaults = listOf("1000")
    ),
    BACK(
        displayName = "按下返回键",
        icon = "⬅️",
        color = 0xFF607D8B,
        paramKeys = emptyList(),
        paramDefaults = emptyList()
    ),
    HOME(
        displayName = "按下主页键",
        icon = "🏠",
        color = 0xFF795548,
        paramKeys = emptyList(),
        paramDefaults = emptyList()
    ),
    CLICK_TEXT(
        displayName = "点击文本",
        icon = "📝",
        color = 0xFFE91E63,
        paramKeys = listOf("文本内容"),
        paramDefaults = listOf("确定")
    ),
    LOG(
        displayName = "记录日志",
        icon = "📋",
        color = 0xFF00BCD4,
        paramKeys = listOf("日志内容"),
        paramDefaults = listOf("完成")
    ),
    TOAST(
        displayName = "显示提示",
        icon = "💬",
        color = 0xFFFFC107,
        paramKeys = listOf("提示内容"),
        paramDefaults = listOf("任务完成")
    ),
    FIND_IMAGE(
        displayName = "查找图片",
        icon = "🖼️",
        color = 0xFF673AB7,
        paramKeys = listOf("图片路径"),
        paramDefaults = listOf("/sdcard/template.png")
    ),
    FIND_AND_CLICK(
        displayName = "识图点击",
        icon = "🔍",
        color = 0xFF5C6BC0,
        paramKeys = listOf("图片路径"),
        paramDefaults = listOf("/sdcard/template.png")
    ),
    GET_COLOR(
        displayName = "获取颜色",
        icon = "🎨",
        color = 0xFF795548,
        paramKeys = listOf("X 坐标", "Y 坐标"),
        paramDefaults = listOf("540", "960")
    ),
    FIND_COLOR(
        displayName = "查找颜色",
        icon = "🔎",
        color = 0xFF8BC34A,
        paramKeys = listOf("颜色值(HEX)"),
        paramDefaults = listOf("#FF0000")
    ),
    COLOR_MATCH(
        displayName = "颜色匹配",
        icon = "✅",
        color = 0xFF009688,
        paramKeys = listOf("X 坐标", "Y 坐标", "颜色值(HEX)"),
        paramDefaults = listOf("540", "960", "#FF0000")
    ),
    CLICK_ID(
        displayName = "点击ID元素",
        icon = "🏷️",
        color = 0xFF3F51B5,
        paramKeys = listOf("元素ID"),
        paramDefaults = listOf("btn_submit")
    ),
    FIND_TEXT(
        displayName = "查找文本",
        icon = "📄",
        color = 0xFF00BCD4,
        paramKeys = listOf("文本内容"),
        paramDefaults = listOf("确定")
    );

    /**
     * 生成描述文本
     */
    fun getDescription(params: List<String>): String {
        return when (this) {
            CLICK -> "点击屏幕 (${params.getOrElse(0) { "540" }}, ${params.getOrElse(1) { "960" }})"
            LONG_CLICK -> "长按 (${params.getOrElse(0) { "540" }}, ${params.getOrElse(1) { "960" }}) 持续 ${params.getOrElse(2) { "1000" }} 毫秒"
            SWIPE -> "从 (${params.getOrElse(0) { "100" }}, ${params.getOrElse(1) { "500" }}) 滑动到 (${params.getOrElse(2) { "100" }}, ${params.getOrElse(3) { "200" }})"
            INPUT -> "输入文字: ${params.getOrElse(0) { "Hello" }}"
            SLEEP -> "等待 ${params.getOrElse(0) { "1000" }} 毫秒"
            BACK -> "按下返回键"
            HOME -> "按下主页键"
            CLICK_TEXT -> "点击文本: ${params.getOrElse(0) { "确定" }}"
            LOG -> "记录日志: ${params.getOrElse(0) { "完成" }}"
            TOAST -> "显示提示: ${params.getOrElse(0) { "任务完成" }}"
            FIND_IMAGE -> "查找图片: ${params.getOrElse(0) { "/sdcard/template.png" }}"
            FIND_AND_CLICK -> "识图点击: ${params.getOrElse(0) { "/sdcard/template.png" }}"
            GET_COLOR -> "获取颜色 (${params.getOrElse(0) { "540" }}, ${params.getOrElse(1) { "960" }})"
            FIND_COLOR -> "查找颜色: ${params.getOrElse(0) { "#FF0000" }}"
            COLOR_MATCH -> "颜色匹配 (${params.getOrElse(0) { "540" }}, ${params.getOrElse(1) { "960" }}) = ${params.getOrElse(2) { "#FF0000" }}"
            CLICK_ID -> "点击ID元素: ${params.getOrElse(0) { "btn_submit" }}"
            FIND_TEXT -> "查找文本: ${params.getOrElse(0) { "确定" }}"
        }
    }

    /**
     * 生成 JavaScript 代码
     */
    fun toJsCode(params: List<String>): String {
        return when (this) {
            CLICK -> "click(${params.getOrElse(0) { "540" }}, ${params.getOrElse(1) { "960" }});"
            LONG_CLICK -> "longClick(${params.getOrElse(0) { "540" }}, ${params.getOrElse(1) { "960" }}, ${params.getOrElse(2) { "1000" }});"
            SWIPE -> "swipe(${params.getOrElse(0) { "100" }}, ${params.getOrElse(1) { "500" }}, ${params.getOrElse(2) { "100" }}, ${params.getOrElse(3) { "200" }});"
            INPUT -> "input(\"${params.getOrElse(0) { "Hello" }}\");"
            SLEEP -> "sleep(${params.getOrElse(0) { "1000" }});"
            BACK -> "back();"
            HOME -> "home();"
            CLICK_TEXT -> "clickText(\"${params.getOrElse(0) { "确定" }}\");"
            LOG -> "log(\"${params.getOrElse(0) { "完成" }}\");"
            TOAST -> "toast(\"${params.getOrElse(0) { "任务完成" }}\");"
            FIND_IMAGE -> "findImage(\"${params.getOrElse(0) { "/sdcard/template.png" }}\");"
            FIND_AND_CLICK -> "findAndClick(\"${params.getOrElse(0) { "/sdcard/template.png" }}\");"
            GET_COLOR -> "getColor(${params.getOrElse(0) { "540" }}, ${params.getOrElse(1) { "960" }});"
            FIND_COLOR -> "findColor(\"${params.getOrElse(0) { "#FF0000" }}\");"
            COLOR_MATCH -> "colorMatch(${params.getOrElse(0) { "540" }}, ${params.getOrElse(1) { "960" }}, \"${params.getOrElse(2) { "#FF0000" }}\");"
            CLICK_ID -> "clickId(\"${params.getOrElse(0) { "btn_submit" }}\");"
            FIND_TEXT -> "findText(\"${params.getOrElse(0) { "确定" }}\");"
        }
    }
}
