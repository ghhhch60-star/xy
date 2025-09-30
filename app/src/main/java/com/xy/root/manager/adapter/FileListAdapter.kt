package com.xy.root.manager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.xy.root.manager.R
import com.xy.root.manager.model.FileItem

class FileListAdapter(
    private val onFileSelectionChanged: (FileItem, Boolean) -> Unit = { _, _ -> }
) : ListAdapter<FileItem, FileListAdapter.ViewHolder>(FileItemDiffCallback()) {
    
    companion object {
        const val PAYLOAD_SELECTION_CHANGED = "selection_changed"
    }
    
    // 强制刷新所有复选框状态
    fun forceRefreshSelection(recyclerView: RecyclerView? = null) {
        android.util.Log.d("FileListAdapter", "forceRefreshSelection called, itemCount: $itemCount")
        
        // 简单粗暴但有效的方法：强制通知所有项更新
        // 使用payload确保只更新选择状态
        notifyItemRangeChanged(0, itemCount, PAYLOAD_SELECTION_CHANGED)
        
        android.util.Log.d("FileListAdapter", "Notified all items for selection change")
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            // 检查是否包含选择状态变化的payload
            val hasSelectionPayload = payloads.any { payload ->
                when (payload) {
                    is String -> payload == PAYLOAD_SELECTION_CHANGED
                    is List<*> -> payload.contains(PAYLOAD_SELECTION_CHANGED)
                    else -> false
                }
            }
            
            if (hasSelectionPayload) {
                // 只更新选择状态，提高性能
                holder.updateSelection(getItem(position))
                return
            }
        }
        
        // 完整绑定
        super.onBindViewHolder(holder, position, payloads)
    }
    
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val checkBoxFile: CheckBox = itemView.findViewById(R.id.checkBoxFile)
        private val textViewFileIcon: TextView = itemView.findViewById(R.id.textViewFileIcon)
        private val textViewFileName: TextView = itemView.findViewById(R.id.textViewFileName)
        private val textViewFilePath: TextView = itemView.findViewById(R.id.textViewFilePath)
        private val textViewFileSize: TextView = itemView.findViewById(R.id.textViewFileSize)
        private val textViewFileTime: TextView = itemView.findViewById(R.id.textViewFileTime)
        
        fun bind(fileItem: FileItem) {
            // 设置选择状态
            checkBoxFile.setOnCheckedChangeListener(null)
            checkBoxFile.isChecked = fileItem.isSelected
            checkBoxFile.setOnCheckedChangeListener { _, isChecked ->
                onFileSelectionChanged(fileItem, isChecked)
            }
            
            // 根据文件类型设置图标
            textViewFileIcon.text = when {
                fileItem.isDirectory -> "📁"
                fileItem.name.contains("映像文件") -> "💿"
                fileItem.name.contains("分区路径") -> "🔗"
                fileItem.name.contains("文件系统") -> "💾"
                fileItem.name.contains("Boot Magic") -> "⚡"
                fileItem.name.contains("文件类型") -> "🔍"
                fileItem.name.contains("分区头") -> "📋"
                fileItem.path.endsWith(".so") -> "⚙️"
                fileItem.path.endsWith(".xml") -> "📄"
                fileItem.path.endsWith(".apk") -> "📱"
                fileItem.path.endsWith(".jar") -> "☕"
                fileItem.path.endsWith(".dex") -> "🔧"
                fileItem.path.contains("lib") -> "📚"
                fileItem.path.contains("bin") -> "⚡"
                else -> "📄"
            }
            
            textViewFileName.text = fileItem.name
            
            // 显示路径（如果有）
            if (fileItem.path.isNotEmpty() && fileItem.path != "hexdump") {
                textViewFilePath.text = fileItem.path
                textViewFilePath.visibility = View.VISIBLE
            } else {
                textViewFilePath.visibility = View.GONE
            }
            
            // 显示大小（如果有）
            val sizeText = fileItem.getFormattedSize()
            if (sizeText.isNotEmpty()) {
                textViewFileSize.text = sizeText
                textViewFileSize.visibility = View.VISIBLE
            } else {
                textViewFileSize.visibility = View.GONE
            }
            
            // 显示时间（如果有）
            val timeText = fileItem.getFormattedTime()
            if (timeText.isNotEmpty()) {
                textViewFileTime.text = timeText
                textViewFileTime.visibility = View.VISIBLE
            } else {
                textViewFileTime.visibility = View.GONE
            }
        }
        
        fun updateSelection(fileItem: FileItem) {
            android.util.Log.d("FileListAdapter", "updateSelection called for ${fileItem.name}, current checkbox: ${checkBoxFile.isChecked}, target: ${fileItem.isSelected}")
            
            // 直接更新复选框状态，不检查是否改变
            checkBoxFile.setOnCheckedChangeListener(null)
            checkBoxFile.isChecked = fileItem.isSelected
            checkBoxFile.setOnCheckedChangeListener { _, isChecked ->
                onFileSelectionChanged(fileItem, isChecked)
            }
            
            android.util.Log.d("FileListAdapter", "updateSelection completed for ${fileItem.name}, checkbox now: ${checkBoxFile.isChecked}")
        }
    }
    
    class FileItemDiffCallback : DiffUtil.ItemCallback<FileItem>() {
        override fun areItemsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            return oldItem.path == newItem.path && oldItem.name == newItem.name
        }
        
        override fun areContentsTheSame(oldItem: FileItem, newItem: FileItem): Boolean {
            // 只比较除选择状态外的其他属性
            return oldItem.name == newItem.name &&
                   oldItem.path == newItem.path &&
                   oldItem.size == newItem.size &&
                   oldItem.isDirectory == newItem.isDirectory &&
                   oldItem.modifiedTime == newItem.modifiedTime
        }
        
        override fun getChangePayload(oldItem: FileItem, newItem: FileItem): Any? {
            val payloads = mutableListOf<String>()
            
            if (oldItem.isSelected != newItem.isSelected) {
                payloads.add(PAYLOAD_SELECTION_CHANGED)
            }
            
            return if (payloads.isNotEmpty()) payloads else null
        }
    }
}
