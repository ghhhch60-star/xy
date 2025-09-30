package com.xy.root.manager.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val downloadUrl: String,
    val fileSize: Long,
    val releaseNotes: String,
    val isForced: Boolean = false,
    val md5Hash: String? = null
) : Parcelable

@Parcelize
data class UpdateResponse(
    val hasUpdate: Boolean,
    val updateInfo: UpdateInfo?
) : Parcelable
