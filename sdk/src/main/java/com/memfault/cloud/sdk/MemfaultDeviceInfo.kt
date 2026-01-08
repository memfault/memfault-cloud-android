package com.memfault.cloud.sdk

/**
 * Information describing a device.
 */
data class MemfaultDeviceInfo(
    val deviceSerial: String,
    val hardwareVersion: String,
    val currentVersion: String,
    val softwareType: String,
)
