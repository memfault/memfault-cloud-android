package com.memfault.cloud.sdk

/**
 * An OTA package, returned by Memfault's server for a specific [MemfaultDeviceInfo].
 */
data class MemfaultOtaPackage(
    val location: String,
    val releaseNotes: String,
    val appVersion: String
)
