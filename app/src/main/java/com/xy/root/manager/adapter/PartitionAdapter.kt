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

class PartitionAdapter(
    private val onBackupClick: (Partition) -> Unit,
    private val onSelectionChanged: (Partition, Boolean) -> Unit
) : ListAdapter<Partition, PartitionAdapter.ViewHolder>(PartitionDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partition_anime, parent, false)
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val payload = payloads[0]
            if (payload == "selection_changed") {
                // 只更新选中状态，提高性能
                holder.updateSelection(getItem(position))
                return
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }
    
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBoxSelect: CheckBox = itemView.findViewById(R.id.checkBoxSelect)
        private val textViewPartitionIcon: TextView = itemView.findViewById(R.id.textViewPartitionIcon)
        private val textViewPartitionName: TextView = itemView.findViewById(R.id.textViewPartitionName)
        private val textViewPartitionPath: TextView = itemView.findViewById(R.id.textViewPartitionPath)
        private val textViewPartitionSize: TextView = itemView.findViewById(R.id.textViewPartitionSize)
        private val textViewBackupStatus: TextView = itemView.findViewById(R.id.textViewBackupStatus)
        private val btnBackup: MaterialButton = itemView.findViewById(R.id.btnBackup)
        
        fun bind(partition: Partition) {
            android.util.Log.d("PartitionAdapter", "bind (完整绑定): ${partition.name} isSelected=${partition.isSelected} position=$adapterPosition")
            // 设置复选框状态
            checkBoxSelect.isChecked = partition.isSelected
            checkBoxSelect.setOnCheckedChangeListener { _, isChecked ->
                onSelectionChanged(partition, isChecked)
            }
            
            // 根据分区名称设置不同的图标
            textViewPartitionIcon.text = when {
                partition.name.contains("boot") -> "🥾"
                partition.name.contains("system") -> "📱"
                partition.name.contains("recovery") -> "🔧"
                partition.name.contains("vendor") -> "📦"
                partition.name.contains("userdata") || partition.name.contains("data") -> "💾"
                partition.name.contains("cache") -> "🗑️"
                partition.name.contains("persist") -> "💿"
                partition.name.contains("modem") || partition.name.contains("radio") -> "📡"
                partition.name.contains("logo") || partition.name.contains("splash") -> "🎨"
                else -> "💽"
            }
            
            textViewPartitionName.text = partition.name
            textViewPartitionPath.text = partition.path
            textViewPartitionSize.text = "大小: ${FormatUtils.formatFileSize(partition.size)}"
            
            if (partition.isBackedUp) {
                textViewBackupStatus.visibility = View.VISIBLE
                textViewBackupStatus.text = "✓"
                btnBackup.text = "已备份"
                btnBackup.isEnabled = false
            } else {
                textViewBackupStatus.visibility = View.GONE
                btnBackup.text = "备份"
                btnBackup.isEnabled = true
            }
            
            btnBackup.setOnClickListener {
                onBackupClick(partition)
            }
        }
        
        // 用于局部更新选中状态，避免重新绑定整个视图
        fun updateSelection(partition: Partition) {
            android.util.Log.d("PartitionAdapter", "updateSelection (局部更新): ${partition.name} isSelected=${partition.isSelected} position=$adapterPosition")
            // 临时移除监听器，避免触发回调
            checkBoxSelect.setOnCheckedChangeListener(null)
            checkBoxSelect.isChecked = partition.isSelected
            // 重新设置监听器
            checkBoxSelect.setOnCheckedChangeListener { _, isChecked ->
                onSelectionChanged(partition, isChecked)
            }
        }
    }
    
    class PartitionDiffCallback : DiffUtil.ItemCallback<Partition>() {
        override fun areItemsTheSame(oldItem: Partition, newItem: Partition): Boolean {
            return oldItem.name == newItem.name && oldItem.path == newItem.path
        }
        
        override fun areContentsTheSame(oldItem: Partition, newItem: Partition): Boolean {
            return oldItem == newItem
        }
        
        override fun getChangePayload(oldItem: Partition, newItem: Partition): Any? {
            // 如果只是选中状态发生变化，返回特殊标记以便局部更新
            if (oldItem.copy(isSelected = newItem.isSelected) == newItem) {
                return "selection_changed"
            }
            return null
        }
    }
}
