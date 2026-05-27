package com.guozi.autoscript

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

/**
 * 节点操作工具类
 * 提供 UI 节点查找和操作功能
 */
class NodeHelper {
    
    companion object {
        private const val TAG = "NodeHelper"
        
        /**
         * 根据文本查找节点
         */
        fun findByText(service: AccessibilityService, text: String): List<NodeInfo> {
            val root = service.rootInActiveWindow ?: return emptyList()
            val nodes = mutableListOf<NodeInfo>()
            findByTextRecursive(root, text, nodes)
            return nodes
        }
        
        private fun findByTextRecursive(
            node: AccessibilityNodeInfo,
            text: String,
            list: MutableList<NodeInfo>
        ) {
            if (node.text?.toString()?.contains(text, ignoreCase = true) == true) {
                list.add(NodeInfo.fromAccessibilityNodeInfo(node))
            }
            if (node.contentDescription?.toString()?.contains(text, ignoreCase = true) == true) {
                list.add(NodeInfo.fromAccessibilityNodeInfo(node))
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                findByTextRecursive(child, text, list)
            }
        }
        
        /**
         * 根据 ID 查找节点
         */
        fun findById(service: AccessibilityService, id: String): List<NodeInfo> {
            val root = service.rootInActiveWindow ?: return emptyList()
            val nodes = mutableListOf<NodeInfo>()
            findByIdRecursive(root, id, nodes)
            return nodes
        }
        
        private fun findByIdRecursive(
            node: AccessibilityNodeInfo,
            id: String,
            list: MutableList<NodeInfo>
        ) {
            if (node.viewIdResourceName?.contains(id, ignoreCase = true) == true) {
                list.add(NodeInfo.fromAccessibilityNodeInfo(node))
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                findByIdRecursive(child, id, list)
            }
        }
        
        /**
         * 根据描述查找节点
         */
        fun findByDescription(service: AccessibilityService, description: String): List<NodeInfo> {
            val root = service.rootInActiveWindow ?: return emptyList()
            val nodes = mutableListOf<NodeInfo>()
            findByDescriptionRecursive(root, description, nodes)
            return nodes
        }
        
        private fun findByDescriptionRecursive(
            node: AccessibilityNodeInfo,
            description: String,
            list: MutableList<NodeInfo>
        ) {
            if (node.contentDescription?.toString()?.contains(description, ignoreCase = true) == true) {
                list.add(NodeInfo.fromAccessibilityNodeInfo(node))
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                findByDescriptionRecursive(child, description, list)
            }
        }
        
        /**
         * 查找所有可点击的节点
         */
        fun findClickable(service: AccessibilityService): List<NodeInfo> {
            val root = service.rootInActiveWindow ?: return emptyList()
            val nodes = mutableListOf<NodeInfo>()
            findClickableRecursive(root, nodes)
            return nodes
        }
        
        private fun findClickableRecursive(
            node: AccessibilityNodeInfo,
            list: MutableList<NodeInfo>
        ) {
            if (node.isClickable) {
                list.add(NodeInfo.fromAccessibilityNodeInfo(node))
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                findClickableRecursive(child, list)
            }
        }
        
        /**
         * 查找所有可编辑的节点
         */
        fun findEditable(service: AccessibilityService): List<NodeInfo> {
            val root = service.rootInActiveWindow ?: return emptyList()
            val nodes = mutableListOf<NodeInfo>()
            findEditableRecursive(root, nodes)
            return nodes
        }
        
        private fun findEditableRecursive(
            node: AccessibilityNodeInfo,
            list: MutableList<NodeInfo>
        ) {
            if (node.isEditable) {
                list.add(NodeInfo.fromAccessibilityNodeInfo(node))
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                findEditableRecursive(child, list)
            }
        }
        
        /**
         * 获取节点树结构
         */
        fun getNodeTree(service: AccessibilityService): NodeTree? {
            val root = service.rootInActiveWindow ?: return null
            return buildNodeTree(root, 0)
        }
        
        private fun buildNodeTree(node: AccessibilityNodeInfo, depth: Int): NodeTree {
            val children = mutableListOf<NodeTree>()
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                children.add(buildNodeTree(child, depth + 1))
            }
            
            return NodeTree(
                info = NodeInfo.fromAccessibilityNodeInfo(node),
                children = children,
                depth = depth
            )
        }
    }
    
    /**
     * 节点信息
     */
    data class NodeInfo(
        val text: String?,
        val contentDescription: String?,
        val viewId: String?,
        val className: String?,
        val packageName: String?,
        val isClickable: Boolean,
        val isEditable: Boolean,
        val isEnabled: Boolean,
        val isChecked: Boolean,
        val isSelected: Boolean,
        val bounds: Rect?
    ) {
        companion object {
            fun fromAccessibilityNodeInfo(node: AccessibilityNodeInfo): NodeInfo {
                val bounds = Rect()
                node.getBoundsInScreen(bounds)
                
                return NodeInfo(
                    text = node.text?.toString(),
                    contentDescription = node.contentDescription?.toString(),
                    viewId = node.viewIdResourceName,
                    className = node.className?.toString(),
                    packageName = node.packageName?.toString(),
                    isClickable = node.isClickable,
                    isEditable = node.isEditable,
                    isEnabled = node.isEnabled,
                    isChecked = node.isChecked,
                    isSelected = node.isSelected,
                    bounds = bounds
                )
            }
        }
        
        /**
         * 获取节点中心坐标
         */
        fun getCenterX(): Int? = bounds?.centerX()
        fun getCenterY(): Int? = bounds?.centerY()
        
        /**
         * 点击节点
         */
        fun click(service: AccessibilityService): Boolean {
            val root = service.rootInActiveWindow ?: return false
            val node = findNodeInTree(root) ?: return false
            return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        
        /**
         * 长按节点
         */
        fun longClick(service: AccessibilityService): Boolean {
            val root = service.rootInActiveWindow ?: return false
            val node = findNodeInTree(root) ?: return false
            return node.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK)
        }
        
        /**
         * 输入文字到节点
         */
        fun setText(service: AccessibilityService, text: String): Boolean {
            val root = service.rootInActiveWindow ?: return false
            val node = findNodeInTree(root) ?: return false
            val arguments = android.os.Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            }
            return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
        }
        
        private fun findNodeInTree(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
            // 匹配 ID
            if (viewId != null && node.viewIdResourceName == viewId) {
                return node
            }
            // 匹配文本
            if (text != null && node.text?.toString() == text) {
                return node
            }
            
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                val result = findNodeInTree(child)
                if (result != null) return result
            }
            return null
        }
    }
    
    /**
     * 节点树结构
     */
    data class NodeTree(
        val info: NodeInfo,
        val children: List<NodeTree>,
        val depth: Int
    ) {
        /**
         * 打印节点树
         */
        fun print(): String {
            val sb = StringBuilder()
            printRecursive(this, sb)
            return sb.toString()
        }
        
        private fun printRecursive(node: NodeTree, sb: StringBuilder) {
            val indent = "  ".repeat(node.depth)
            sb.appendLine("$indent[${node.info.className}] text=${node.info.text} id=${node.info.viewId}")
            
            for (child in node.children) {
                printRecursive(child, sb)
            }
        }
    }
}
