package com.memfault.cloud.sample

import android.util.Log
import androidx.lifecycle.ViewModel
import com.memfault.cloud.sdk.ChunkSender
import com.memfault.cloud.sdk.GetLatestReleaseCallback
import com.memfault.cloud.sdk.MemfaultCloud
import com.memfault.cloud.sdk.MemfaultDeviceInfo
import com.memfault.cloud.sdk.MemfaultOtaPackage
import com.memfault.cloud.sdk.SendChunksCallback

private const val DEVICE_SERIAL_ONE = ""
private const val DEVICE_SERIAL_TWO = ""

class SampleViewModel : ViewModel() {
    init {
        MemfaultCloud.setMinLogLevel(MemfaultCloud.LogLevel.VERBOSE)
    }

    private val memfaultCloud = MemfaultCloud.Builder()
        .setApiKey("") // Add your API key here!
        .build()

    private val chunkSenderMap = mutableMapOf<String, ChunkSender>()
    private val deviceInfoMap = mapOf(
        DEVICE_SERIAL_ONE to MemfaultDeviceInfo(
            deviceSerial = DEVICE_SERIAL_ONE,
            hardwareVersion = "nrf-proto",
            currentVersion = "1.0.0",
            softwareType = "main"
        ),
        DEVICE_SERIAL_TWO to MemfaultDeviceInfo(
            deviceSerial = DEVICE_SERIAL_TWO,
            hardwareVersion = "nrf-proto",
            currentVersion = "1.0.0",
            softwareType = "main"
        )
    )

    private fun deviceInfo(serial: String) = checkNotNull(deviceInfoMap[serial]) {
        "Illegal selection"
    }

    private fun chunkSender(serial: String) = chunkSenderMap.getOrPut(serial) {
        ChunkSender.Builder()
            .setMemfaultCloud(memfaultCloud)
            .setDeviceSerialNumber(serial)
            .build()
    }

    override fun onCleared() {
        memfaultCloud.deinit()
    }

    fun getLatestRelease(serial: String) =
        memfaultCloud.getLatestRelease(deviceInfo(serial), callback = object : GetLatestReleaseCallback {
            override fun onUpdateAvailable(otaPackage: MemfaultOtaPackage) {
                Log.d("MFLT-SAMPLE", "Update available")
            }

            override fun onUpToDate() {
                Log.d("MFLT-SAMPLE", "Release up to date")
            }

            override fun onError(e: Exception) {
                Log.d("MFLT-SAMPLE", "Failed to get latest release", e)
            }
        })

    @ExperimentalUnsignedTypes
    fun addChunks(serial: String) {
        Log.d("MFLT-SAMPLE", "Adding chunks")
        chunkSender(serial).addChunks(listOf(CHUNK_ONE, CHUNK_TWO))
    }

    fun sendChunks(serial: String) = chunkSender(serial).send(
        callback = object : SendChunksCallback {
            override fun onError(e: Exception, sent: Int) {
                Log.e("MFLT-SAMPLE", "Error: sent $sent ", e)
            }

            override fun onQueueEmpty(sent: Int) {
                Log.i("MFLT-SAMPLE", "Queue empty: sent $sent")
            }

            override fun onRetryAfterDelay(delay: Long, sent: Int) {
                Log.i("MFLT-SAMPLE", "Retry: delay $delay sent $sent")
            }
        }
    )

    companion object {
        @ExperimentalUnsignedTypes
        val CHUNK_ONE = ubyteArrayOf(
            0x40u,
            0x54u,
            0x31u,
            0xe4u,
            0x02u,
            0xa7u,
            0x02u,
            0x01u,
            0x03u,
            0x01u,
            0x07u,
            0x6au,
            0x54u,
            0x45u,
            0x53u,
            0x54u,
            0x53u,
            0x45u,
            0x52u,
            0x49u,
            0x41u,
            0x4cu,
            0x0au,
            0x6du,
            0x74u,
            0x65u,
            0x73u,
            0x74u,
            0x2du,
            0x73u,
            0x6fu,
            0x66u,
            0x74u,
            0x77u,
            0x61u,
            0x72u,
            0x65u,
            0x09u,
            0x6au,
            0x31u,
            0x2eu,
            0x30u,
            0x2eu,
            0x30u,
            0x2du,
            0x74u,
            0x65u,
            0x73u
        ).toByteArray()

        @ExperimentalUnsignedTypes
        val CHUNK_TWO = ubyteArrayOf(
            0x80u,
            0x2cu,
            0x74u,
            0x06u,
            0x6du,
            0x74u,
            0x65u,
            0x73u,
            0x74u,
            0x2du,
            0x68u,
            0x61u,
            0x72u,
            0x64u,
            0x77u,
            0x61u,
            0x72u,
            0x65u,
            0x04u,
            0xa1u,
            0x01u,
            0xa1u,
            0x72u,
            0x63u,
            0x68u,
            0x75u,
            0x6eu,
            0x6bu,
            0x5fu,
            0x74u,
            0x65u,
            0x73u,
            0x74u,
            0x5fu,
            0x73u,
            0x75u,
            0x63u,
            0x63u,
            0x65u,
            0x73u,
            0x73u,
            0x01u
        ).toByteArray()
    }
}
