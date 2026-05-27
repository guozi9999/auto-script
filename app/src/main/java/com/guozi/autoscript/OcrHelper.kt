package com.guozi.autoscript

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * OCR 文字识别
 * 使用 Google ML Kit 实现文字识别
 */
class OcrHelper(private val context: Context) {
    
    companion object {
        private const val TAG = "OcrHelper"
    }
    
    private var recognizer: TextRecognizer? = null
    
    init {
        // 初始化中文识别器
        recognizer = TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
        Log.d(TAG, "OCR 初始化完成")
    }
    
    /**
     * 识别图片中的文字
     * @param bitmap 图片
     * @return 识别结果
     */
    suspend fun recognizeText(bitmap: Bitmap): OcrResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        return suspendCancellableCoroutine { continuation ->
            recognizer?.process(image)
                ?.addOnSuccessListener { visionText ->
                    val textBlocks = visionText.textBlocks.map { block ->
                        TextBlock(
                            text = block.text,
                            boundingBox = block.boundingBox,
                            lines = block.lines.map { line ->
                                TextLine(
                                    text = line.text,
                                    boundingBox = line.boundingBox,
                                    elements = line.elements.map { element ->
                                        TextElement(
                                            text = element.text,
                                            boundingBox = element.boundingBox
                                        )
                                    }
                                )
                            }
                        )
                    }
                    
                    val result = OcrResult(
                        fullText = visionText.text,
                        blocks = textBlocks
                    )
                    
                    Log.d(TAG, "识别完成: ${textBlocks.size} 个文本块")
                    continuation.resume(result)
                }
                ?.addOnFailureListener { e ->
                    Log.e(TAG, "识别失败: ${e.message}")
                    continuation.resumeWithException(e)
                }
        }
    }
    
    /**
     * 在图片中查找指定文字的位置
     * @param bitmap 图片
     * @param targetText 目标文字
     * @return 匹配位置列表
     */
    suspend fun findText(bitmap: Bitmap, targetText: String): List<TextMatchResult> {
        val ocrResult = recognizeText(bitmap)
        val matches = mutableListOf<TextMatchResult>()
        
        for (block in ocrResult.blocks) {
            for (line in block.lines) {
                for (element in line.elements) {
                    if (element.text.contains(targetText, ignoreCase = true)) {
                        element.boundingBox?.let { rect ->
                            matches.add(TextMatchResult(
                                text = element.text,
                                centerX = rect.centerX(),
                                centerY = rect.centerY(),
                                rect = rect
                            ))
                        }
                    }
                }
            }
        }
        
        Log.d(TAG, "找到 ${matches.size} 个匹配 '$targetText'")
        return matches
    }
    
    /**
     * 在指定区域内查找文字
     * @param bitmap 图片
     * @param targetText 目标文字
     * @param region 搜索区域
     * @return 匹配位置列表
     */
    suspend fun findTextInRegion(
        bitmap: Bitmap,
        targetText: String,
        region: Rect
    ): List<TextMatchResult> {
        // 裁剪图片
        val croppedBitmap = Bitmap.createBitmap(
            bitmap,
            region.left,
            region.top,
            region.width(),
            region.height()
        )
        
        // 在裁剪后的图片中查找
        val matches = findText(croppedBitmap, targetText)
        
        // 调整坐标到原图坐标系
        return matches.map { match ->
            match.copy(
                centerX = match.centerX + region.left,
                centerY = match.centerY + region.top,
                rect = Rect(
                    match.rect.left + region.left,
                    match.rect.top + region.top,
                    match.rect.right + region.left,
                    match.rect.bottom + region.top
                )
            )
        }
    }
    
    /**
     * 检查图片中是否包含指定文字
     * @param bitmap 图片
     * @param text 目标文字
     * @return 是否包含
     */
    suspend fun containsText(bitmap: Bitmap, text: String): Boolean {
        val ocrResult = recognizeText(bitmap)
        return ocrResult.fullText.contains(text, ignoreCase = true)
    }
    
    /**
     * 释放资源
     */
    fun destroy() {
        recognizer?.close()
        recognizer = null
    }
    
    /**
     * OCR 识别结果
     */
    data class OcrResult(
        val fullText: String,
        val blocks: List<TextBlock>
    )
    
    /**
     * 文本块
     */
    data class TextBlock(
        val text: String,
        val boundingBox: Rect?,
        val lines: List<TextLine>
    )
    
    /**
     * 文本行
     */
    data class TextLine(
        val text: String,
        val boundingBox: Rect?,
        val elements: List<TextElement>
    )
    
    /**
     * 文本元素
     */
    data class TextElement(
        val text: String,
        val boundingBox: Rect?
    )
    
    /**
     * 文字匹配结果
     */
    data class TextMatchResult(
        val text: String,
        val centerX: Int,
        val centerY: Int,
        val rect: Rect
    )
}
