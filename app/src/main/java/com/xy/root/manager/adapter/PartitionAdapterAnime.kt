package com.xy.root.manager.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.xy.root.manager.R
import com.xy.root.manager.model.Partition
import com.xy.root.manager.utils.FormatUtils

class PartitionAdapterAnime(
    private val onBackupClick: (Partition) -> Unit,
    private val onSelectionChanged: (Partition, Boolean) -> Unit,
    private val onPartitionClick: (Partition) -> Unit = {}
) : ListAdapter<Partition, PartitionAdapterAnime.ViewHolder>(PartitionDiffCallback()) {
    
    // 预计算图标映射，避免每次bind时重复计算
    private val iconMap = mapOf(
        "boot" to "⚡",
        "system" to "🌟", 
        "recovery" to "🔮",
        "vendor" to "📦",
        "userdata" to "💎",
        "data" to "💎",
        "cache" to "🌙",
        "persist" to "🌸",
        "modem" to "📡",
        "radio" to "📡",
        "logo" to "🎨",
        "splash" to "🎨"
    )
    
    private var recyclerView: RecyclerView? = null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partition_anime, parent, false)
        
        // 保存RecyclerView引用用于post操作
        if (recyclerView == null && parent is RecyclerView) {
            recyclerView = parent
        }
        
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            // 处理部分更新，提高性能
            holder.bindPartial(getItem(position), payloads)
        }
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBoxSelect: CheckBox = itemView.findViewById(R.id.checkBoxSelect)
        private val textViewPartitionIcon: TextView = itemView.findViewById(R.id.textViewPartitionIcon)
        private val textViewPartitionName: TextView = itemView.findViewById(R.id.textViewPartitionName)
        private val textViewPartitionPath: TextView = itemView.findViewById(R.id.textViewPartitionPath)
        private val textViewPartitionSize: TextView = itemView.findViewById(R.id.textViewPartitionSize)
        private val textViewBackupStatus: TextView = itemView.findViewById(R.id.textViewBackupStatus)
        private val btnBackup: MaterialButton = itemView.findViewById(R.id.btnBackup)
        
        // 缓存格式化的大小字符串，避免重复计算
        private var cachedSizeText: String? = null
        private var cachedSize: Long = -1
        
        fun bind(partition: Partition) {
            android.util.Log.d("PartitionAdapter", "bind (完整绑定): ${partition.name} isSelected=${partition.isSelected} position=$adapterPosition")
            
            // 强制设置复选框状态 - 先清除监听器，然后设置状态
            checkBoxSelect.setOnCheckedChangeListener(null)
            
            // 强制刷新复选框状态，确保正确显示
            checkBoxSelect.isChecked = false  // 先重置
            checkBoxSelect.isChecked = partition.isSelected  // 再设置正确的状态
            
            // 重新设置监听器
            checkBoxSelect.setOnCheckedChangeListener { _, isChecked ->
                android.util.Log.d("PartitionAdapter", "用户点击复选框: ${partition.name} -> $isChecked")
                onSelectionChanged(partition, isChecked)
            }
            android.util.Log.d("PartitionAdapter", "bind done: ${partition.name} checkbox=${checkBoxSelect.isChecked}")
            
            // 使用预计算的图标映射，提高性能
            textViewPartitionIcon.text = iconMap.entries.find { 
                partition.name.contains(it.key) 
            }?.value ?: "✨"
            
            textViewPartitionName.text = partition.name
            
            // 根据是否为映像文件显示不同的路径信息
            textViewPartitionPath.text = if (partition.isImageFile) {
                "映像文件: ${partition.path.substringAfterLast("/")}"
            } else {
                partition.path
            }
            
            // 缓存大小格式化，避免重复计算
            if (cachedSize != partition.size) {
                cachedSize = partition.size
                cachedSizeText = "大小: ${FormatUtils.formatFileSize(partition.size)}"
            }
            textViewPartitionSize.text = cachedSizeText
            
            // 映像文件不需要备份状态，而是显示为可选择状态
            when {
                partition.isImageFile -> {
                    textViewBackupStatus.visibility = View.GONE
                    btnBackup.text = "📁 选择备份"
                    btnBackup.isEnabled = true
                }
                partition.isBackedUp -> {
                    textViewBackupStatus.visibility = View.VISIBLE
                    textViewBackupStatus.text = "✅"
                    btnBackup.text = "已备份"
                    btnBackup.isEnabled = false
                }
                else -> {
                    textViewBackupStatus.visibility = View.GONE
                    btnBackup.text = "✨ 备份"
                    btnBackup.isEnabled = true
                }
            }
            
            // 移除动画，直接设置点击事件，提高性能
            btnBackup.setOnClickListener {
                onBackupClick(partition)
            }
            
            // 添加分区点击事件
            itemView.setOnClickListener {
                onPartitionClick(partition)
            }
        }
        
        // 部分更新方法，只更新变化的部分
        fun bindPartial(partition: Partition, payloads: MutableList<Any>) {
            payloads.forEach { payload ->
                when (payload) {
                    "selection" -> {
                        android.util.Log.d("PartitionAdapter", "bindPartial selection: ${partition.name} isSelected=${partition.isSelected} position=$adapterPosition")
                        
                        // 强制设置复选框状态 - 确保状态正确更新
                        checkBoxSelect.setOnCheckedChangeListener(null)
                        
                        // 强制刷新复选框状态
                        checkBoxSelect.isChecked = false  // 先重置
                        checkBoxSelect.isChecked = partition.isSelected  // 再设置正确的状态
                        
                        checkBoxSelect.setOnCheckedChangeListener { _, isChecked ->
                            android.util.Log.d("PartitionAdapter", "用户点击复选框(partial): ${partition.name} -> $isChecked")
                            onSelectionChanged(partition, isChecked)
                        }
                        android.util.Log.d("PartitionAdapter", "bindPartial selection done: ${partition.name} checkbox=${checkBoxSelect.isChecked}")
                    }
                    "backup_status" -> {
                        if (partition.isBackedUp) {
                            textViewBackupStatus.visibility = View.VISIBLE
                            textViewBackupStatus.text = "✅"
                            btnBackup.text = "已备份"
                            btnBackup.isEnabled = false
                        } else {
                            textViewBackupStatus.visibility = View.GONE
                            btnBackup.text = "✨ 备份"
                            btnBackup.isEnabled = true
                        }
                    }
                }
            }
        }
    }
    
    // 强制刷新所有复选框状态
    fun forceRefreshSelection() {
        android.util.Log.d("PartitionAdapter", "forceRefreshSelection called, itemCount: $itemCount")
        
        // 打印当前列表中前几个项的状态
        for (i in 0 until minOf(3, itemCount)) {
            val partition = getItem(i)
            android.util.Log.d("PartitionAdapter", "  Item $i: ${partition.name} isSelected=${partition.isSelected}")
        }
        
        // 只在确实需要时才强制刷新
        if (itemCount > 0) {
            // 尝试两种刷新方式
            
            // 方式1: 使用payload只更新选择状态
            android.util.Log.d("PartitionAdapter", "Using payload method")
            notifyItemRangeChanged(0, itemCount, "selection")
            
            // 方式2: 如果payload不工作，强制完整重新绑定
            // 延迟执行，确保payload方式先尝试
            recyclerView?.post {
                android.util.Log.d("PartitionAdapter", "Fallback: forcing full rebind")
                notifyItemRangeChanged(0, itemCount)
            }
            
            android.util.Log.d("PartitionAdapter", "Force refreshed selection for $itemCount items")
        }
    }
    

class PartitionDiffCallback : DiffUtil.ItemCallback<Partition>() {
        override fun areItemsTheSame(oldItem: Partition, newItem: Partition): Boolean {
            val result = oldItem.name == newItem.name && oldItem.path == newItem.path
            android.util.Log.d("PartitionDiffCallback", "areItemsTheSame: ${oldItem.name} -> $result")
            return result
        }
        
        override fun areContentsTheSame(oldItem: Partition, newItem: Partition): Boolean {
            val result = oldItem == newItem
            android.util.Log.d("PartitionDiffCallback", "areContentsTheSame: ${oldItem.name} -> $result (old.isSelected=${oldItem.isSelected}, new.isSelected=${newItem.isSelected})")
            return result
        }
        
        override fun getChangePayload(oldItem: Partition, newItem: Partition): Any? {
            val payloads = mutableListOf<String>()
            
            if (oldItem.isSelected != newItem.isSelected) {
                payloads.add("selection")
                android.util.Log.d("PartitionDiffCallback", "getChangePayload: ${oldItem.name} selection changed ${oldItem.isSelected} -> ${newItem.isSelected}")
            }
            
            if (oldItem.isBackedUp != newItem.isBackedUp) {
                payloads.add("backup_status")
                android.util.Log.d("PartitionDiffCallback", "getChangePayload: ${oldItem.name} backup status changed")
            }
            
            val result = if (payloads.isNotEmpty()) payloads else null
            android.util.Log.d("PartitionDiffCallback", "getChangePayload: ${oldItem.name} -> payloads: $payloads")
            return result
        }
    }
}
