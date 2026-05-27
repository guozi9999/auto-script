package com.guozi.autoscript

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log

/**
 * 颜色工具类
 * 提供颜色识别和匹配功能
 */
class ColorHelper {
    
    companion object {
        private const val TAG = "ColorHelper"
        
        /**
         * 获取指定坐标的颜色
         * @param bitmap 屏幕截图
         * @param x x 坐标
         * @param y y 坐标
         * @return 颜色值 (ARGB)，坐标无效返回 -1
         */
        fun getColor(bitmap: Bitmap, x: Int, y: Int): Int {
            if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) {
                Log.e(TAG, "坐标越界: ($x, $y), 屏幕尺寸: ${bitmap.width}x${bitmap.height}")
                return -1
            }
            return bitmap.getPixel(x, y)
        }
        
        /**
         * 获取颜色的十六进制字符串
         */
        fun colorToHex(color: Int): String {
            return String.format("#%06X", 0xFFFFFF and color)
        }
        
        /**
         * 十六进制字符串转颜色
         */
        fun hexToColor(hex: String): Int {
            val cleanHex = hex.removePrefix("#")
            return when (cleanHex.length) {
                6 -> Color.parseColor("#FF$cleanHex")
                8 -> Color.parseColor("#$cleanHex")
                else -> {
                    Log.e(TAG, "无效的颜色格式: $hex")
                    -1
                }
            }
        }
        
        /**
         * 判断两个颜色是否相似
         * @param color1 颜色1
         * @param color2 颜色2
         * @param threshold 阈值 (0-255)，越小越严格
         * @return 是否相似
         */
        fun isColorSimilar(color1: Int, color2: Int, threshold: Int = 30): Boolean {
            val r1 = Color.red(color1)
            val g1 = Color.green(color1)
            val b1 = Color.blue(color1)
            
            val r2 = Color.red(color2)
            val g2 = Color.green(color2)
            val b2 = Color.blue(color2)
            
            val diff = Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2)
            return diff <= threshold * 3
        }
        
        /**
         * 判断颜色是否与指定颜色相似
         * @param color 要检查的颜色
         * @param targetColor 目标颜色（十六进制）
         * @param threshold 阈值
         */
        fun isColorMatch(color: Int, targetColor: String, threshold: Int = 30): Boolean {
            val target = hexToColor(targetColor)
            if (target == -1) return false
            return isColorSimilar(color, target, threshold)
        }
        
        /**
         * 在指定区域内查找颜色
         * @param bitmap 屏幕截图
         * @param targetColor 目标颜色（十六进制）
         * @param region 搜索区域 (left, top, right, bottom)，null 表示全屏
         * @param threshold 阈值
         * @return 找到的位置列表
         */
        fun findColor(
            bitmap: Bitmap,
            targetColor: String,
            region: android.graphics.Rect? = null,
            threshold: Int = 30
        ): List<ColorMatchResult> {
            val target = hexToColor(targetColor)
            if (target == -1) return emptyList()
            
            val results = mutableListOf<ColorMatchResult>()
            val left = region?.left ?: 0
            val top = region?.top ?: 0
            val right = region?.right ?: bitmap.width
            val bottom = region?.bottom ?: bitmap.height
            
            for (y in top until bottom) {
                for (x in left until right) {
                    val pixel = bitmap.getPixel(x, y)
                    if (isColorSimilar(pixel, target, threshold)) {
                        results.add(ColorMatchResult(x, y, pixel))
                    }
                }
            }
            
            Log.d(TAG, "在区域内找到 ${results.size} 个匹配颜色")
            return results
        }
        
        /**
         * 在指定坐标范围内查找颜色（多点匹配）
         * @param bitmap 屏幕截图
         * @param points 颜色点列表 [(x, y, colorHex), ...]
         * @param threshold 阈值
         * @return 是否所有点都匹配
         */
        fun multiColorMatch(
            bitmap: Bitmap,
            points: List<Triple<Int, Int, String>>,
            threshold: Int = 30
        ): Boolean {
            for ((x, y, colorHex) in points) {
                val pixel = getColor(bitmap, x, y)
                if (pixel == -1) return false
                if (!isColorMatch(pixel, colorHex, threshold)) {
                    return false
                }
            }
            return true
        }
        
        /**
         * 查找颜色块的中心位置
         * @param bitmap 屏幕截图
         * @param targetColor 目标颜色
         * @param minSize 最小颜色块大小（像素数）
         * @param threshold 阈值
         * @return 颜色块中心位置列表
         */
        fun findColorBlocks(
            bitmap: Bitmap,
            targetColor: String,
            minSize: Int = 10,
            threshold: Int = 30
        ): List<ColorMatchResult> {
            val target = hexToColor(targetColor)
            if (target == -1) return emptyList()
            
            // 创建访问标记数组
            val visited = Array(bitmap.height) { BooleanArray(bitmap.width) }
            val blocks = mutableListOf<ColorMatchResult>()
            
            for (y in 0 until bitmap.height) {
                for (x in 0 until bitmap.width) {
                    if (!visited[y][x]) {
                        val pixel = bitmap.getPixel(x, y)
                        if (isColorSimilar(pixel, target, threshold)) {
                            // BFS 查找连通区域
                            val block = findConnectedRegion(bitmap, x, y, target, threshold, visited)
                            if (block.size >= minSize) {
                                // 计算中心点
                                val centerX = block.sumOf { it.first } / block.size
                                val centerY = block.sumOf { it.second } / block.size
                                blocks.add(ColorMatchResult(centerX, centerY, target))
                            }
                        }
                    }
                }
            }
            
            Log.d(TAG, "找到 ${blocks.size} 个颜色块")
            return blocks
        }
        
        /**
         * BFS 查找连通区域
         */
        private fun findConnectedRegion(
            bitmap: Bitmap,
            startX: Int,
            startY: Int,
            targetColor: Int,
            threshold: Int,
            visited: Array<BooleanArray>
        ): List<Pair<Int, Int>> {
            val region = mutableListOf<Pair<Int, Int>>()
            val queue = ArrayDeque<Pair<Int, Int>>()
            
            queue.add(startX to startY)
            visited[startY][startX] = true
            
            while (queue.isNotEmpty()) {
                val (x, y) = queue.removeFirst()
                region.add(x to y)
                
                // 检查四个方向
                val directions = listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
                for ((dx, dy) in directions) {
                    val nx = x + dx
                    val ny = y + dy
                    
                    if (nx in 0 until bitmap.width && ny in 0 until bitmap.height && !visited[ny][nx]) {
                        val pixel = bitmap.getPixel(nx, ny)
                        if (isColorSimilar(pixel, targetColor, threshold)) {
                            visited[ny][nx] = true
                            queue.add(nx to ny)
                        }
                    }
                }
            }
            
            return region
        }
    }
    
    /**
     * 颜色匹配结果
     */
    data class ColorMatchResult(
        val x: Int,
        val y: Int,
        val color: Int
    ) {
        val hexColor: String get() = colorToHex(color)
    }
}
