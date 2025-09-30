package com.xy.root.manager.activity

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.xy.root.manager.R
import com.xy.root.manager.adapter.FileListAdapter
import com.xy.root.manager.model.Partition
import com.xy.root.manager.viewmodel.PartitionFilesViewModel

class PartitionFilesActivity : AppCompatActivity() {
    
    private lateinit var viewModel: PartitionFilesViewModel
    private lateinit var fileListAdapter: FileListAdapter
    private lateinit var recyclerViewFiles: RecyclerView
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var toolbar: MaterialToolbar
    private lateinit var layoutFileActions: LinearLayout
    private lateinit var buttonSelectAll: MaterialButton
    private lateinit var buttonRefreshSelection: MaterialButton
    private var isAllSelected = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partition_files)
        
        // 获取传递的分区信息
        val partition = intent.getParcelableExtra<Partition>("partition")
        if (partition == null) {
            Toast.makeText(this, "分区信息错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        initViews()
        setupViewModel()
        setupRecyclerView()
        setupToolbar(partition)
        setupFileActions()
        
        // 开始加载文件列表
        viewModel.loadPartitionFiles(partition)
    }
    
    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        recyclerViewFiles = findViewById(R.id.recyclerViewFiles)
        progressIndicator = findViewById(R.id.progressIndicator)
        layoutFileActions = findViewById(R.id.layoutFileActions)
        buttonSelectAll = findViewById(R.id.buttonSelectAll)
        buttonRefreshSelection = findViewById(R.id.buttonRefreshSelection)
    }
    
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[PartitionFilesViewModel::class.java]
        
        viewModel.fileList.observe(this) { files ->
            android.util.Log.d("PartitionFilesActivity", "fileList observed, size: ${files.size}")
            fileListAdapter.submitList(files) {
                android.util.Log.d("PartitionFilesActivity", "submitList completed, calling forceRefreshSelection")
                // submitList完成后，强制刷新复选框状态
                recyclerViewFiles.post {
                    fileListAdapter.forceRefreshSelection(recyclerViewFiles)
                }
            }
            // 显示/隐藏操作栏
            layoutFileActions.visibility = if (files.isNotEmpty()) View.VISIBLE else View.GONE
        }
        
        viewModel.isLoading.observe(this) { isLoading ->
            progressIndicator.visibility = if (isLoading) 
                android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.statusMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
        
        viewModel.selectedFiles.observe(this) { selectedFiles ->
            // 更新选择计数和按钮状态
            val totalFiles = viewModel.fileList.value?.size ?: 0
            val selectedCount = selectedFiles.size
            
            if (selectedCount == totalFiles && totalFiles > 0) {
                buttonSelectAll.text = "取消全选"
                isAllSelected = true
            } else {
                buttonSelectAll.text = "全选"
                isAllSelected = false
            }
            
            // 更新toolbar subtitle显示选择状态
            supportActionBar?.subtitle = if (selectedCount > 0) {
                "已选择 $selectedCount 个文件"
            } else {
                viewModel.fileList.value?.let { files ->
                    if (files.isNotEmpty()) {
                        val partition = intent.getParcelableExtra<Partition>("partition")
                        partition?.path ?: ""
                    } else ""
                } ?: ""
            }
        }
    }
    
    private fun setupRecyclerView() {
        fileListAdapter = FileListAdapter { file, isSelected ->
            viewModel.updateFileSelection(file, isSelected)
        }
        recyclerViewFiles.apply {
            adapter = fileListAdapter
            layoutManager = LinearLayoutManager(this@PartitionFilesActivity)
            setHasFixedSize(true)
        }
    }
    
    private fun setupToolbar(partition: Partition) {
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "${partition.name} 分区文件"
            subtitle = partition.path
        }
    }
    
    private fun setupFileActions() {
        buttonSelectAll.setOnClickListener {
            viewModel.selectAllFiles(!isAllSelected)
        }
        
        buttonRefreshSelection.setOnClickListener {
            viewModel.refreshFileSelection()
        }
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
