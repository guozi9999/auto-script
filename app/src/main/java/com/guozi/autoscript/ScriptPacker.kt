package com.guozi.autoscript

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * 脚本打包器
 * 将脚本打包成可分享的格式
 */
class ScriptPacker(private val context: Context) {
    
    companion object {
        private const val TAG = "ScriptPacker"
        private const val EXPORT_DIR = "exported_scripts"
    }
    
    private val exportDir: File by lazy {
        File(context.getExternalFilesDir(null), EXPORT_DIR).apply { mkdirs() }
    }
    
    /**
     * 打包单个脚本
     */
    fun packScript(scriptPath: String, includeResources: Boolean = true): File? {
        return try {
            val scriptFile = File(scriptPath)
            if (!scriptFile.exists()) {
                Log.e(TAG, "脚本文件不存在: $scriptPath")
                return null
            }
            
            val scriptName = scriptFile.nameWithoutExtension
            val zipFile = File(exportDir, "$scriptName.aspack")
            
            ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
                // 添加脚本文件
                addFileToZip(zip, scriptFile, "script.js")
                
                // 添加资源文件
                if (includeResources) {
                    addResourcesToZip(zip, scriptFile.parentFile)
                }
                
                // 添加元数据
                val metadata = createMetadata(scriptFile)
                zip.putNextEntry(ZipEntry("metadata.json"))
                zip.write(metadata.toByteArray())
                zip.closeEntry()
            }
            
            Log.d(TAG, "打包完成: ${zipFile.absolutePath}")
            zipFile
        } catch (e: Exception) {
            Log.e(TAG, "打包失败: ${e.message}")
            null
        }
    }
    
    /**
     * 打包多个脚本
     */
    fun packScripts(scriptPaths: List<String>, packName: String): File? {
        return try {
            val zipFile = File(exportDir, "$packName.aspack")
            
            ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
                for (scriptPath in scriptPaths) {
                    val scriptFile = File(scriptPath)
                    if (scriptFile.exists()) {
                        addFileToZip(zip, scriptFile, "scripts/${scriptFile.name}")
                    }
                }
                
                // 添加元数据
                val metadata = createPackMetadata(scriptPaths)
                zip.putNextEntry(ZipEntry("metadata.json"))
                zip.write(metadata.toByteArray())
                zip.closeEntry()
            }
            
            Log.d(TAG, "打包完成: ${zipFile.absolutePath}")
            zipFile
        } catch (e: Exception) {
            Log.e(TAG, "打包失败: ${e.message}")
            null
        }
    }
    
    /**
     * 解包脚本
     */
    fun unpackScript(packPath: String, targetDir: String): Boolean {
        return try {
            val packFile = File(packPath)
            if (!packFile.exists()) {
                Log.e(TAG, "包文件不存在: $packPath")
                return false
            }
            
            val target = File(targetDir).apply { mkdirs() }.canonicalFile
            
            java.util.zip.ZipFile(packFile).use { zip ->
                zip.entries().asSequence().forEach { entry ->
                    val entryFile = File(target, entry.name).canonicalFile
                    if (!isInsideDirectory(target, entryFile)) {
                        throw SecurityException("非法压缩包路径: ${entry.name}")
                    }
                    
                    if (entry.isDirectory) {
                        entryFile.mkdirs()
                    } else {
                        entryFile.parentFile?.mkdirs()
                        zip.getInputStream(entry).use { input ->
                            FileOutputStream(entryFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                    }
                }
            }
            
            Log.d(TAG, "解包完成: $targetDir")
            true
        } catch (e: Exception) {
            Log.e(TAG, "解包失败: ${e.message}")
            false
        }
    }
    
    private fun isInsideDirectory(parent: File, child: File): Boolean {
        val parentPath = parent.canonicalPath
        val childPath = child.canonicalPath
        return childPath == parentPath || childPath.startsWith(parentPath + File.separator)
    }
    
    /**
     * 分享脚本包
     */
    fun shareScript(packFile: File) {
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            packFile
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, "分享脚本")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
    
    /**
     * 添加文件到 ZIP
     */
    private fun addFileToZip(zip: ZipOutputStream, file: File, entryName: String) {
        zip.putNextEntry(ZipEntry(entryName))
        FileInputStream(file).use { input ->
            input.copyTo(zip)
        }
        zip.closeEntry()
    }
    
    /**
     * 添加资源文件到 ZIP
     */
    private fun addResourcesToZip(zip: ZipOutputStream, scriptDir: File?) {
        if (scriptDir == null) return
        
        val resourcesDir = File(scriptDir, "resources")
        if (resourcesDir.exists() && resourcesDir.isDirectory) {
            resourcesDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    addFileToZip(zip, file, "resources/${file.name}")
                }
            }
        }
    }
    
    /**
     * 创建元数据
     */
    private fun createMetadata(scriptFile: File): String {
        val metadata = mapOf(
            "name" to scriptFile.nameWithoutExtension,
            "version" to "1.0.0",
            "author" to "自动脚本用户",
            "description" to "",
            "createdAt" to System.currentTimeMillis(),
            "fileSize" to scriptFile.length()
        )
        return com.google.gson.Gson().toJson(metadata)
    }
    
    /**
     * 创建包元数据
     */
    private fun createPackMetadata(scriptPaths: List<String>): String {
        val metadata = mapOf(
            "name" to "Script Pack",
            "version" to "1.0.0",
            "scriptCount" to scriptPaths.size,
            "createdAt" to System.currentTimeMillis()
        )
        return com.google.gson.Gson().toJson(metadata)
    }
    
    /**
     * 获取已导出的脚本包
     */
    fun getExportedPacks(): List<File> {
        return exportDir.listFiles()?.filter { it.extension == "aspack" } ?: emptyList()
    }
    
    /**
     * 删除导出的脚本包
     */
    fun deleteExportedPack(packPath: String): Boolean {
        return try {
            File(packPath).delete()
        } catch (e: Exception) {
            Log.e(TAG, "删除失败: ${e.message}")
            false
        }
    }
}
