package com.guozi.autoscript

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream

class FilePoolActivity : AppCompatActivity() {
    
    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 2001
    }
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var adapter: FilePoolAdapter
    private lateinit var imagesDir: File
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_pool)
        
        imagesDir = File(getExternalFilesDir(null), "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        
        recyclerView = findViewById(R.id.recyclerImages)
        tvEmpty = findViewById(R.id.tvEmpty)
        
        adapter = FilePoolAdapter(
            onCopyPath = { file ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("image_path", file.absolutePath)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Path copied: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
            },
            onDelete = { file ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Image")
                    .setMessage("Are you sure you want to delete ${file.name}?")
                    .setPositiveButton("Delete") { _, _ ->
                        file.delete()
                        loadImages()
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        
        findViewById<View>(R.id.btnSelectImage).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
        }
        
        loadImages()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                importImage(uri)
            }
        }
    }
    
    private fun importImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return
            
            // Get filename from URI
            val cursor = contentResolver.query(uri, null, null, null, null)
            val fileName = cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    if (nameIndex >= 0) it.getString(nameIndex) else "image_${System.currentTimeMillis()}.png"
                } else {
                    "image_${System.currentTimeMillis()}.png"
                }
            } ?: "image_${System.currentTimeMillis()}.png"
            
            // Copy to app directory
            val destFile = File(imagesDir, fileName)
            FileOutputStream(destFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            
            Toast.makeText(this, "Imported: $fileName", Toast.LENGTH_SHORT).show()
            loadImages()
        } catch (e: Exception) {
            Toast.makeText(this, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadImages() {
        val files = imagesDir.listFiles()
            ?.filter { it.isFile && it.extension.lowercase() in listOf("png", "jpg", "jpeg", "webp", "bmp") }
            ?.sortedByDescending { it.lastModified() }
            ?: emptyList()
        
        adapter.submitList(files)
        
        if (files.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}

class FilePoolAdapter(
    private val onCopyPath: (File) -> Unit,
    private val onDelete: (File) -> Unit
) : RecyclerView.Adapter<FilePoolAdapter.ViewHolder>() {
    
    private var items: List<File> = emptyList()
    
    fun submitList(list: List<File>) {
        items = list
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_file_pool, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = items[position]
        holder.bind(file)
    }
    
    override fun getItemCount() = items.size
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivThumbnail: ImageView = itemView.findViewById(R.id.ivThumbnail)
        private val tvFileName: TextView = itemView.findViewById(R.id.tvFileName)
        private val tvFilePath: TextView = itemView.findViewById(R.id.tvFilePath)
        
        fun bind(file: File) {
            tvFileName.text = file.name
            tvFilePath.text = file.absolutePath
            
            // Load thumbnail
            val options = BitmapFactory.Options().apply {
                inSampleSize = 4
            }
            val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
            ivThumbnail.setImageBitmap(bitmap)
            
            // Click to copy path
            itemView.setOnClickListener {
                onCopyPath(file)
            }
            
            // Long press to delete
            itemView.setOnLongClickListener {
                onDelete(file)
                true
            }
        }
    }
}
