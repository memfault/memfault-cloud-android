package com.memfault.cloud.sdk

/**
 * An OTA package, returned by Memfault's server for a specific [MemfaultDeviceInfo].
 *
 * @param size the size of the artifact at the [location]
 * @param isDelta whether this package corresponds to a Delta or a Full Release.
 */
data class MemfaultOtaPackage(
    val location: String,
    val releaseNotes: String,
    val appVersion: String,
    val md5: String,
    val extraInfo: Map<String, String>,
    val releaseExtraInfo: Map<String, String>,
    val isForced: Boolean?,
    val size: Long,
    val isDelta: Boolean? = null,
)
