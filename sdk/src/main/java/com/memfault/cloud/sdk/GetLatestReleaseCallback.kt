package com.memfault.cloud.sdk

/**
 * Receive the result of [MemfaultCloud.getLatestRelease].
 *
 * All methods are run on the main thread. Only one method will be called.
 */
interface GetLatestReleaseCallback : Callback {
    /**
     * Called when an update is available for the given device.
     *
     * @param otaPackage describing the update.
     */
    fun onUpdateAvailable(otaPackage: MemfaultOtaPackage)

    /**
     * Called when the device is up to date.
     */
    fun onUpToDate()

    /**
     * Called when the latest release could not be determined.
     *
     * @param e an exception describing the error that occurred.
     */
    fun onError(e: Exception)
}
