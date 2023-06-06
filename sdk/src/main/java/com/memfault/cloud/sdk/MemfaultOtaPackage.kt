package com.memfault.cloud.sdk

/**
 * An OTA package, returned by Memfault's server for a specific [MemfaultDeviceInfo].
 */
data class MemfaultOtaPackage(
    val location: String,
    val releaseNotes: String,
    val appVersion: String,
    val md5: String,
    val artifactExtraInfo: Map<String, String>,
    val releaseExtraInfo: Map<String, String>,
    val isForced: Boolean?,
    /**
     * The size of the artifact at the [location]
     */
    val size: Long,
    /**
     * Whether this package corresponds to a Delta or a Full Release.
     */
    val isDelta: Boolean,
) {
    @Deprecated(
        message = "Use artifactExtraInfo to be more specific.",
        replaceWith = ReplaceWith("artifactExtraInfo"),
    )
    val extraInfo: Map<String, String> get() = artifactExtraInfo
}
