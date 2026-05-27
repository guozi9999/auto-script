package com.guozi.autoscript

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import java.io.File

/**
 * 图片查找器
 * 使用像素匹配实现模板匹配
 */
class ImageFinder {
    
    companion object {
        private const val TAG = "ImageFinder"
        
        /**
         * 在屏幕中查找图片位置
         * @param screen 屏幕截图
         * @param template 模板图片
         * @param confidence 匹配置信度 (0.0-1.0)
         * @return 匹配位置的中心坐标，未找到返回 null
         */
        fun findImage(screen: Bitmap, template: Bitmap, confidence: Double = 0.8): MatchResult? {
            val screenPixels = getPixels(screen)
            val templatePixels = getPixels(template)
            val templateW = template.width
            val templateH = template.height
            val screenW = screen.width
            val screenH = screen.height
            
            var bestScore = 0.0
            var bestX = 0
            var bestY = 0
            
            // 滑动窗口匹配
            for (y in 0..screenH - templateH) {
                for (x in 0..screenW - templateW) {
                    val score = calculateSimilarity(
                        screenPixels, screenW, x, y,
                        templatePixels, templateW, templateH
                    )
                    
                    if (score > bestScore) {
                        bestScore = score
                        bestX = x
                        bestY = y
                    }
                }
            }
            
            Log.d(TAG, "最佳匹配: ($bestX, $bestY), 分数: $bestScore")
            
            return if (bestScore >= confidence) {
                MatchResult(
                    x = bestX + templateW / 2,
                    y = bestY + templateH / 2,
                    score = bestScore,
                    rect = android.graphics.Rect(bestX, bestY, bestX + templateW, bestY + templateH)
                )
            } else {
                Log.d(TAG, "未找到匹配项 (最高分: $bestScore < $confidence)")
                null
            }
        }
        
        /**
         * 查找所有匹配位置
         */
        fun findAllImages(screen: Bitmap, template: Bitmap, confidence: Double = 0.8): List<MatchResult> {
            val results = mutableListOf<MatchResult>()
            val screenPixels = getPixels(screen)
            val templatePixels = getPixels(template)
            val templateW = template.width
            val templateH = template.height
            val screenW = screen.width
            val screenH = screen.height
            
            // 先计算所有位置的分数
            val scores = Array(screenH - templateH + 1) { y ->
                Array(screenW - templateW + 1) { x ->
                    calculateSimilarity(
                        screenPixels, screenW, x, y,
                        templatePixels, templateW, templateH
                    )
                }
            }
            
            // 找出所有符合条件的位置
            for (y in scores.indices) {
                for (x in scores[y].indices) {
                    if (scores[y][x] >= confidence) {
                        // 检查是否与已有结果太近
                        val tooClose = results.any { 
                            Math.abs(it.x - (x + templateW/2)) < templateW/2 &&
                            Math.abs(it.y - (y + templateH/2)) < templateH/2
                        }
                        
                        if (!tooClose) {
                            results.add(MatchResult(
                                x = x + templateW / 2,
                                y = y + templateH / 2,
                                score = scores[y][x],
                                rect = android.graphics.Rect(x, y, x + templateW, y + templateH)
                            ))
                        }
                    }
                }
            }
            
            Log.d(TAG, "找到 ${results.size} 个匹配位置")
            return results
        }
        
        /**
         * 计算两个区域的相似度
         */
        private fun calculateSimilarity(
            screenPixels: IntArray, screenW: Int, offsetX: Int, offsetY: Int,
            templatePixels: IntArray, templateW: Int, templateH: Int
        ): Double {
            var matchCount = 0
            val totalPixels = templateW * templateH
            
            for (ty in 0 until templateH) {
                for (tx in 0 until templateW) {
                    val screenIdx = (offsetY + ty) * screenW + (offsetX + tx)
                    val templateIdx = ty * templateW + tx
                    
                    if (screenIdx < screenPixels.size && templateIdx < templatePixels.size) {
                        val screenColor = screenPixels[screenIdx]
                        val templateColor = templatePixels[templateIdx]
                        
                        // 计算颜色相似度
                        if (isColorSimilar(screenColor, templateColor, 30)) {
                            matchCount++
                        }
                    }
                }
            }
            
            return matchCount.toDouble() / totalPixels
        }
        
        /**
         * 判断两个颜色是否相似
         */
        private fun isColorSimilar(color1: Int, color2: Int, threshold: Int): Boolean {
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
         * 获取图片像素数组
         */
        private fun getPixels(bitmap: Bitmap): IntArray {
            val pixels = IntArray(bitmap.width * bitmap.height)
            bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            return pixels
        }
        
        /**
         * 从文件加载图片
         */
        fun loadTemplate(filePath: String): Bitmap? {
            val file = File(filePath)
            if (!file.exists()) {
                Log.e(TAG, "模板文件不存在: $filePath")
                return null
            }
            return BitmapFactory.decodeFile(filePath)
        }
        
        /**
         * 从 assets 加载图片
         */
        fun loadTemplateFromAssets(context: android.content.Context, fileName: String): Bitmap? {
            return try {
                context.assets.open(fileName).use { 
                    BitmapFactory.decodeStream(it)
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载 assets 图片失败: ${e.message}")
                null
            }
        }
    }
    
    /**
     * 匹配结果
     */
    data class MatchResult(
        val x: Int,           // 中心 x 坐标
        val y: Int,           // 中心 y 坐标
        val score: Double,    // 匹配分数
        val rect: android.graphics.Rect  // 匹配区域
    )
}
