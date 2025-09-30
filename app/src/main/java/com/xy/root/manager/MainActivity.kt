package com.xy.root.manager

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.xy.root.manager.adapter.PartitionAdapterAnime
import com.xy.root.manager.model.Partition
import com.xy.root.manager.model.UpdateInfo
import com.xy.root.manager.utils.UpdateManager
import com.xy.root.manager.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    
    // 视图引用
    private lateinit var rootStatusCard: MaterialCardView
    private lateinit var imageViewRootStatus: ImageView
    private lateinit var textViewRootStatus: TextView
    private lateinit var textViewRootType: TextView
    private lateinit var btnRequestRoot: MaterialButton
    private lateinit var btnRefreshPartitions: MaterialButton
    private lateinit var btnBatchBackup: MaterialButton
    private lateinit var btnSelectAll: MaterialButton
    private lateinit var btnCheckUpdate: MaterialButton
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var textViewProgress: TextView
    private lateinit var textViewPartitionCount: TextView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var recyclerViewPartitions: RecyclerView
    
    // 备份进度相关视图
    private lateinit var backupProgressCard: MaterialCardView
    private lateinit var backupProgressText: TextView
    private lateinit var backupProgressPercent: TextView
    private lateinit var backupProgressBar: LinearProgressIndicator
    private lateinit var currentBackupFile: TextView
    
    private lateinit var viewModel: MainViewModel
    private lateinit var partitionAdapter: PartitionAdapterAnime
    private lateinit var updateManager: UpdateManager
    
    private val STORAGE_PERMISSION_CODE = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_anime)
        
        // 设置窗口为全屏，让背景图片延伸到状态栏
        window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }
        
        // 手动绑定视图
        bindViews()
        
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        updateManager = UpdateManager(this)
        
        setupUI()
        observeViewModel()
        checkPermissions()
        
        // 添加进入动画
        rootStatusCard.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in))
        
        // 显示欢迎弹窗
        showWelcomeDialog()
        
        // 自动检测Root权限
        autoDetectRootPermission()
    }
    
    private fun bindViews() {
        rootStatusCard = findViewById(R.id.rootStatusCard)
        imageViewRootStatus = findViewById(R.id.imageViewRootStatus)
        textViewRootStatus = findViewById(R.id.textViewRootStatus)
        textViewRootType = findViewById(R.id.textViewRootType)
        btnRequestRoot = findViewById(R.id.btnRequestRoot)
        btnRefreshPartitions = findViewById(R.id.btnRefreshPartitions)
        btnBatchBackup = findViewById(R.id.btnBatchBackup)
        btnSelectAll = findViewById(R.id.btnSelectAll)
        btnCheckUpdate = findViewById(R.id.btnCheckUpdate)
        progressIndicator = findViewById(R.id.progressIndicator)
        textViewProgress = findViewById(R.id.textViewProgress)
        textViewPartitionCount = findViewById(R.id.textViewPartitionCount)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        recyclerViewPartitions = findViewById(R.id.recyclerViewPartitions)
        
        // 备份进度相关视图
        backupProgressCard = findViewById(R.id.backupProgressCard)
        backupProgressText = findViewById(R.id.backupProgressText)
        backupProgressPercent = findViewById(R.id.backupProgressPercent)
        backupProgressBar = findViewById(R.id.backupProgressBar)
        currentBackupFile = findViewById(R.id.currentBackupFile)
    }
    
    private fun setupUI() {
        // 设置RecyclerView
        partitionAdapter = PartitionAdapterAnime(
            onBackupClick = { partition ->
                if (viewModel.isRooted.value == true) {
                    showBackupConfirmation(partition)
                } else {
                    showMessage("请先获取Root权限")
                }
            },
            onSelectionChanged = { partition, isSelected ->
                viewModel.updatePartitionSelection(partition, isSelected)
            },
            onPartitionClick = { partition ->
                openPartitionFiles(partition)
            }
        )
        
        recyclerViewPartitions.apply {
            adapter = partitionAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            
            // 性能优化配置 - 针对全选操作优化
            itemAnimator = null // 禁用默认动画，提高性能
            isNestedScrollingEnabled = true // 确保嵌套滚动正常
            
            // 设置RecyclerView缓存池大小
            recycledViewPool.setMaxRecycledViews(0, 30) // 增加ViewHolder缓存
            
            // 预取优化
            (layoutManager as LinearLayoutManager).apply {
                isItemPrefetchEnabled = true
                initialPrefetchItemCount = 8 // 预取更多item
            }
            
            // 优化全选操作的绘制性能
            setItemViewCacheSize(20) // 增加视图缓存大小
            isDrawingCacheEnabled = true // 启用绘制缓存
        }
        
        // 设置按钮点击事件
        btnRequestRoot.setOnClickListener {
            checkAndRequestRoot()
        }
        
        btnRefreshPartitions.setOnClickListener {
            if (viewModel.isRooted.value == true) {
                viewModel.loadPartitions()
                showMessage("正在扫描分区...")
            } else {
                showMessage("请先获取Root权限")
            }
        }
        
        btnBatchBackup.setOnClickListener {
            if (viewModel.isRooted.value == true) {
                val selectedPartitions = viewModel.getSelectedPartitions()
                if (selectedPartitions.isNotEmpty()) {
                    showBatchBackupConfirmation(selectedPartitions)
                } else {
                    showMessage("请先选择要备份的分区")
                }
            } else {
                showMessage("请先获取Root权限")
            }
        }
        
        btnCheckUpdate.setOnClickListener {
            checkForUpdate()
        }
        
        // 添加点击去抖动，避免快速点击造成的性能问题
        var lastSelectAllClickTime = 0L
        btnSelectAll.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastSelectAllClickTime < 300) { // 300ms去抖动
                return@setOnClickListener
            }
            lastSelectAllClickTime = currentTime
            
            if (viewModel.isRooted.value == true) {
                val partitions = viewModel.partitionList.value ?: return@setOnClickListener
                if (partitions.isEmpty()) return@setOnClickListener
                
                val allSelected = partitions.all { it.isSelected }
                
                // 临时禁用按钮，防止重复点击
                btnSelectAll.isEnabled = false
                
                android.util.Log.d("MainActivity", "Select all clicked: allSelected=$allSelected")
                
                if (allSelected) {
                    // 如果全部选中，则取消全选
                    android.util.Log.d("MainActivity", "Deselecting all partitions")
                    viewModel.selectAllPartitions(false)
                } else {
                    // 否则全选
                    android.util.Log.d("MainActivity", "Selecting all partitions")
                    viewModel.selectAllPartitions(true)
                }
                
                // 不再立即调用刷新，而是等待数据更新完成后在Observer中自动刷新
                
                // 延迟重新启用按钮
                btnSelectAll.postDelayed({
                    btnSelectAll.isEnabled = true
                }, 200)
                
            } else {
                showMessage("请先获取Root权限")
            }
        }
        
        // 初始化备份进度卡片为隐藏状态
        backupProgressCard.visibility = View.GONE
        
    }
    
    
    private fun observeViewModel() {
        viewModel.isRooted.observe(this) { isRooted ->
            updateRootStatus(isRooted)
            // 更新按钮状态
            btnRefreshPartitions.isEnabled = isRooted
            btnSelectAll.isEnabled = isRooted
            val selectedCount = viewModel.partitionList.value?.count { it.isSelected } ?: 0
            btnBatchBackup.isEnabled = selectedCount > 0 && isRooted
        }
        
        viewModel.rootType.observe(this) { rootType ->
            updateRootTypeDisplay(rootType.toString())
        }
        
        viewModel.partitionList.observe(this) { partitions ->
            android.util.Log.d("MainActivity", "收到分区列表更新: size=${partitions.size}")
            
            // 打印前几个分区的选择状态
            partitions.take(3).forEachIndexed { index, partition ->
                android.util.Log.d("MainActivity", "  Partition $index: ${partition.name} isSelected=${partition.isSelected}")
            }
            
            // 直接提交列表，DiffUtil会自动计算变化并优化更新
            partitionAdapter.submitList(partitions) {
                // submitList完成后的回调
                android.util.Log.d("MainActivity", "submitList completed")
            }
            updatePartitionCount(partitions.size)
            emptyStateLayout.visibility = if (partitions.isEmpty()) View.VISIBLE else View.GONE
            
            // 更新批量备份按钮状态 - 只在文本实际变化时更新，避免闪烁
            val selectedCount = partitions.count { it.isSelected }
            val isRooted = viewModel.isRooted.value == true
            val newEnabled = selectedCount > 0 && isRooted
            val newBackupText = if (selectedCount > 0) "备份选中($selectedCount)" else "备份选中"
            
            if (btnBatchBackup.isEnabled != newEnabled) {
                btnBatchBackup.isEnabled = newEnabled
            }
            if (btnBatchBackup.text.toString() != newBackupText) {
                btnBatchBackup.text = newBackupText
            }
            
            // 更新全选按钮状态 - 只在文本实际变化时更新，避免闪烁
            val allSelected = partitions.isNotEmpty() && partitions.all { it.isSelected }
            val newSelectText = if (allSelected) "取消全选" else "全选"
            if (btnSelectAll.text.toString() != newSelectText) {
                btnSelectAll.text = newSelectText
                
                // 当按钮状态发生变化时，强制刷新可见项选择状态
                android.util.Log.d("MainActivity", "Selection state changed, forcing refresh")
                partitionAdapter.forceRefreshSelection()
            }
        }
        
        viewModel.isScanning.observe(this) { isScanning ->
            // 扫描时显示进度条
            progressIndicator.visibility = if (isScanning) View.VISIBLE else View.GONE
            textViewProgress.visibility = if (isScanning) View.VISIBLE else View.GONE
        }
        
        viewModel.backupProgress.observe(this) { progress ->
            progressIndicator.progress = progress
            textViewProgress.text = "$progress%"
            
            // 显示或隐藏进度条
            if (progress > 0 && progress < 100) {
                progressIndicator.visibility = View.VISIBLE
                textViewProgress.visibility = View.VISIBLE
            } else {
                progressIndicator.visibility = View.GONE
                textViewProgress.visibility = View.GONE
            }
        }
        
        viewModel.statusMessage.observe(this) { message ->
            message?.let { showMessage(it) }
        }
        
        viewModel.errorMessage.observe(this) { error ->
            error?.let { 
                Snackbar.make(rootStatusCard, it, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(getColor(R.color.anime_red))
                    .show()
            }
        }
        
        // 观察详细备份进度
        viewModel.isBackupInProgress.observe(this) { inProgress ->
            backupProgressCard.visibility = if (inProgress) View.VISIBLE else View.GONE
        }
        
        viewModel.backupProgressText.observe(this) { text ->
            backupProgressText.text = text
        }
        
        viewModel.currentBackupFile.observe(this) { file ->
            currentBackupFile.text = file
        }
        
        
        // 更新详细进度条和百分比
        viewModel.backupProgress.observe(this) { progress ->
            backupProgressBar.progress = progress
            backupProgressPercent.text = "$progress%"
        }
    }
    
    private fun checkAndRequestRoot() {
        // 显示Root权限提示弹窗
        showRootPermissionDialog()
    }
    
    private fun showRootPermissionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("🔑 获取Root权限")
            .setMessage("如果使用的是内核级Root管理器请手动授权Root权限\n\n获取Root权限后请重启App，否则扫描不到分区文件\n\n如还有问题请联系我\n\n🐧: 3302719731")
            .setIcon(R.drawable.ic_check_circle)
            .setPositiveButton("继续获取") { _, _ ->
                // 用户确认后继续获取Root权限
                performRootCheck()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun performRootCheck() {
        viewModel.checkRootStatus()
        btnRequestRoot.isEnabled = false
        
        // 延迟重新启用按钮
        btnRequestRoot.postDelayed({
            btnRequestRoot.isEnabled = true
        }, 3000)
    }
    
    /**
     * 自动检测Root权限
     * 在应用启动时静默检查，如果已有权限则自动加载分区
     */
    private fun autoDetectRootPermission() {
        // 延迟一点点时间，让欢迎弹窗先显示
        btnRequestRoot.postDelayed({
            // 静默检测Root权限，并在检测成功后自动加载分区
            viewModel.autoCheckRootAndLoadPartitions()
        }, 1000) // 1秒后开始检测
    }
    
    private fun updateRootStatus(isRooted: Boolean) {
        if (isRooted) {
            imageViewRootStatus.setImageResource(R.drawable.ic_check_circle)
            textViewRootStatus.text = "已获取Root权限 ✓"
            textViewRootStatus.setTextColor(getColor(R.color.success_green))
            btnRequestRoot.visibility = View.GONE
            btnRefreshPartitions.isEnabled = true
            btnBatchBackup.isEnabled = false // 初始状态为禁用，需要选择分区后才能启用
            
            // 自动加载分区
            viewModel.loadPartitions()
        } else {
            imageViewRootStatus.setImageResource(R.drawable.ic_error)
            textViewRootStatus.text = "未获取Root权限"
            textViewRootStatus.setTextColor(getColor(R.color.anime_red))
            btnRequestRoot.visibility = View.VISIBLE
            btnRefreshPartitions.isEnabled = false
            btnBatchBackup.isEnabled = false
        }
    }
    
    private fun updateRootTypeDisplay(rootType: String?) {
        if (!rootType.isNullOrEmpty() && rootType != "未知") {
            textViewRootType.visibility = View.VISIBLE
            textViewRootType.text = "Root类型: $rootType"
        } else {
            textViewRootType.visibility = View.GONE
        }
    }
    
    private fun updatePartitionCount(count: Int) {
        textViewPartitionCount.text = "发现 $count 个分区"
    }
    
    private fun showBackupConfirmation(partition: Partition) {
        MaterialAlertDialogBuilder(this)
            .setTitle("备份分区")
            .setMessage("确定要备份 ${partition.name} 分区吗？\n路径: ${partition.path}")
            .setPositiveButton("备份") { _, _ ->
                viewModel.backupPartition(partition)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun showBatchBackupConfirmation(partitions: List<Partition>) {
        val partitionNames = partitions.joinToString(", ") { it.name }
        MaterialAlertDialogBuilder(this)
            .setTitle("批量备份分区")
            .setMessage("确定要备份以下 ${partitions.size} 个分区吗？\n\n$partitionNames\n\n这可能需要较长时间，请保持设备电量充足。")
            .setPositiveButton("开始备份") { _, _ ->
                viewModel.batchBackupPartitions(partitions)
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivityForResult(intent, STORAGE_PERMISSION_CODE)
            }
        } else {
            val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            
            if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            }
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                showMessage("存储权限已授予")
            } else {
                showMessage("需要存储权限才能保存备份文件")
            }
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                showMessage("存储权限已授予")
            }
        }
    }
    
    private fun openPartitionFiles(partition: Partition) {
        val intent = Intent(this, com.xy.root.manager.activity.PartitionFilesActivity::class.java)
        intent.putExtra("partition", partition)
        startActivity(intent)
    }
    
    private fun showWelcomeDialog() {
        // 检查是否显示欢迎对话框
        val sharedPrefs = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val showWelcome = sharedPrefs.getBoolean("show_welcome_dialog", true)
        
        if (!showWelcome) {
            return // 不显示欢迎对话框
        }
        
        MaterialAlertDialogBuilder(this)
            .setTitle("🎉 欢迎使用")
            .setMessage("欢迎使用小Y系统分区备份管理器")
            .setIcon(R.drawable.ic_check_circle)
            .setPositiveButton("开始使用") { _, _ ->
                // 用户点击确定，继续使用
            }
            .setNegativeButton("不再提示") { _, _ ->
                // 保存用户选择，下次不再显示
                sharedPrefs.edit()
                    .putBoolean("show_welcome_dialog", false)
                    .apply()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showMessage(message: String) {
        Snackbar.make(rootStatusCard, message, Snackbar.LENGTH_SHORT)
            .setBackgroundTint(getColor(R.color.anime_blue))
            .setTextColor(Color.WHITE)
            .show()
    }
    
    // ========== 更新功能相关方法 ==========
    
    /**
     * 检查应用更新
     */
    private fun checkForUpdate() {
        // 禁用按钮防止重复点击
        btnCheckUpdate.isEnabled = false
        btnCheckUpdate.text = getString(R.string.checking_update)
        
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val updateResponse = withContext(Dispatchers.IO) {
                    updateManager.checkForUpdate()
                }
                
                if (updateResponse.hasUpdate && updateResponse.updateInfo != null) {
                    showUpdateDialog(updateResponse.updateInfo)
                } else {
                    showMessage(getString(R.string.no_update))
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                showMessage(getString(R.string.network_error))
            } finally {
                // 恢复按钮状态
                btnCheckUpdate.isEnabled = true
                btnCheckUpdate.text = getString(R.string.check_update)
            }
        }
    }
    
    /**
     * 显示更新对话框
     */
    private fun showUpdateDialog(updateInfo: UpdateInfo) {
        val currentVersion = updateManager.getCurrentVersionInfo()
        val newVersion = "v${updateInfo.versionName} (${updateInfo.versionCode})"
        val fileSize = updateManager.formatFileSize(updateInfo.fileSize)
        
        val message = StringBuilder().apply {
            append("发现新版本可供下载\n\n")
            append("${getString(R.string.current_version)}: $currentVersion\n")
            append("${getString(R.string.new_version)}: $newVersion\n")
            append("${getString(R.string.update_size)}: $fileSize\n\n")
            
            if (updateInfo.releaseNotes.isNotBlank()) {
                append("${getString(R.string.release_notes)}:\n")
                append(updateInfo.releaseNotes.take(200)) // 限制显示长度
                if (updateInfo.releaseNotes.length > 200) {
                    append("...")
                }
            }
        }.toString()
        
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.update_dialog_title))
            .setMessage(message)
            .setIcon(R.drawable.ic_update)
            .setPositiveButton(getString(R.string.update_download)) { _, _ ->
                downloadUpdate(updateInfo)
            }
            .setNegativeButton(getString(R.string.update_later)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(!updateInfo.isForced) // 强制更新不允许取消
            .show()
    }
    
    /**
     * 下载更新
     */
    private fun downloadUpdate(updateInfo: UpdateInfo) {
        // 显示下载进度对话框
        val progressDialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.downloading_update))
            .setMessage("正在下载 v${updateInfo.versionName}...")
            .setIcon(R.drawable.ic_update)
            .setCancelable(false)
            .create()
        
        progressDialog.show()
        
        updateManager.downloadUpdate(
            updateInfo = updateInfo,
            onProgress = { progress ->
                // 可以在这里更新进度，如果需要的话
                runOnUiThread {
                    progressDialog.setMessage("正在下载 v${updateInfo.versionName}... $progress%")
                }
            },
            onComplete = { success, error ->
                runOnUiThread {
                    progressDialog.dismiss()
                    
                    if (success) {
                        showInstallDialog()
                    } else {
                        showMessage("${getString(R.string.update_error)}: ${error ?: "未知错误"}")
                    }
                }
            }
        )
    }
    
    /**
     * 显示安装对话框
     */
    private fun showInstallDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.download_complete))
            .setMessage("更新包下载完成，是否立即安装？")
            .setIcon(R.drawable.ic_check_circle)
            .setPositiveButton(getString(R.string.install_update)) { _, _ ->
                installUpdate()
            }
            .setNegativeButton(getString(R.string.update_later)) { dialog, _ ->
                dialog.dismiss()
                showMessage("更新包已保存，您可以稍后手动安装")
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * 安装更新
     */
    private fun installUpdate() {
        try {
            updateManager.installApk()
            showMessage("正在启动安装程序...")
        } catch (e: Exception) {
            e.printStackTrace()
            showMessage("安装失败: ${e.message}")
        }
    }
}
