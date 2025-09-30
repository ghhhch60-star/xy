package com.xy.root.update

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.xy.root.manager.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * 更新对话框
 */
class UpdateDialog(private val context: Context) {
    
    private var dialog: AlertDialog? = null
    private var progressDialog: AlertDialog? = null
    
    /**
     * 显示更新信息对话框
     */
    fun showUpdateDialog(
        updateInfo: UpdateInfo,
        onDownloadClick: () -> Unit,
        onCancelClick: () -> Unit = {}
    ) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_update, null)
        
        // 找到视图控件
        val tvTitle: TextView = view.findViewById(R.id.tv_update_title)
        val tvVersion: TextView = view.findViewById(R.id.tv_update_version)
        val tvSize: TextView = view.findViewById(R.id.tv_update_size)
        val tvTime: TextView = view.findViewById(R.id.tv_update_time)
        val tvDescription: TextView = view.findViewById(R.id.tv_update_description)
        val btnDownload: Button = view.findViewById(R.id.btn_download)
        val btnCancel: Button = view.findViewById(R.id.btn_cancel)
        
        // 设置内容
        tvTitle.text = updateInfo.name
        tvVersion.text = "版本: ${updateInfo.tagName}"
        tvSize.text = "大小: ${updateInfo.getFormattedFileSize()}"
        
        // 格式化发布时间
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val date = inputFormat.parse(updateInfo.publishedAt)
            tvTime.text = "发布时间: ${outputFormat.format(date ?: Date())}"
        } catch (e: Exception) {
            tvTime.text = "发布时间: ${updateInfo.publishedAt}"
        }
        
        // 设置更新说明，去除Markdown格式
        var description = updateInfo.body
            .replace("#", "")
            .replace("*", "")
            .replace("- ", "• ")
            .trim()
        
        if (description.length > 300) {
            description = description.substring(0, 300) + "..."
        }
        
        tvDescription.text = description.ifEmpty { "本次更新包含功能改进和问题修复。" }
        
        // 设置按钮点击事件
        btnDownload.setOnClickListener {
            dialog?.dismiss()
            onDownloadClick()
        }
        
        btnCancel.setOnClickListener {
            dialog?.dismiss()
            onCancelClick()
        }
        
        // 创建并显示对话框
        dialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(true)
            .create()
        
        dialog?.show()
    }
    
    /**
     * 显示下载进度对话框
     */
    fun showDownloadProgressDialog(
        onCancelClick: () -> Unit = {}
    ): ProgressDialogController {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_download_progress, null)
        
        val progressBar: ProgressBar = view.findViewById(R.id.progress_bar)
        val tvProgress: TextView = view.findViewById(R.id.tv_progress)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val btnCancel: Button = view.findViewById(R.id.btn_cancel_download)
        
        btnCancel.setOnClickListener {
            progressDialog?.dismiss()
            onCancelClick()
        }
        
        progressDialog = MaterialAlertDialogBuilder(context)
            .setView(view)
            .setCancelable(false)
            .create()
        
        progressDialog?.show()
        
        return ProgressDialogController(progressBar, tvProgress, tvStatus)
    }
    
    /**
     * 显示错误对话框
     */
    fun showErrorDialog(message: String, onOkClick: () -> Unit = {}) {
        MaterialAlertDialogBuilder(context)
            .setTitle("❌ 更新失败")
            .setMessage(message)
            .setIcon(R.drawable.ic_error)
            .setPositiveButton("确定") { dialog, _ ->
                dialog.dismiss()
                onOkClick()
            }
            .setCancelable(true)
            .show()
    }
    
    /**
     * 显示无更新对话框
     */
    fun showNoUpdateDialog(currentVersion: String) {
        MaterialAlertDialogBuilder(context)
            .setTitle("✅ 检查更新")
            .setMessage("当前已是最新版本 $currentVersion\n\n您的应用已经是最新版本，无需更新。")
            .setIcon(R.drawable.ic_check_circle)
            .setPositiveButton("确定") { dialog, _ -> dialog.dismiss() }
            .setCancelable(true)
            .show()
    }
    
    /**
     * 关闭所有对话框
     */
    fun dismissAll() {
        dialog?.dismiss()
        progressDialog?.dismiss()
    }
    
    /**
     * 进度对话框控制器
     */
    class ProgressDialogController(
        private val progressBar: ProgressBar,
        private val tvProgress: TextView,
        private val tvStatus: TextView
    ) {
        
        fun updateProgress(progress: DownloadProgress) {
            progressBar.progress = progress.progress
            tvProgress.text = progress.getFormattedProgress()
        }
        
        fun updateStatus(status: String) {
            tvStatus.text = status
        }
        
        fun setIndeterminate(indeterminate: Boolean) {
            progressBar.isIndeterminate = indeterminate
        }
    }
}
