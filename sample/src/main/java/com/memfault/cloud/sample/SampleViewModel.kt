package com.memfault.cloud.sample

import android.util.Log
import androidx.lifecycle.ViewModel
import com.memfault.cloud.sdk.GetLatestReleaseCallback
import com.memfault.cloud.sdk.MemfaultApi
import com.memfault.cloud.sdk.MemfaultChunkSender
import com.memfault.cloud.sdk.MemfaultDeviceInfo
import com.memfault.cloud.sdk.MemfaultOtaPackage
import com.memfault.cloud.sdk.PostChunksCallback

private const val DEVICE_SERIAL = ""

class SampleViewModel : ViewModel() {
    val memfaultApi = MemfaultApi.Builder()
        .setApiKey("") // Add your API key here!
        .build()

    val chunkSender = MemfaultChunkSender.Builder()
        .setMemfaultApi(memfaultApi)
        .setDeviceSerialNumber(DEVICE_SERIAL)
        .build()

    val deviceInfo = MemfaultDeviceInfo(
        deviceSerial = DEVICE_SERIAL,
        hardwareVersion = "nrf-proto",
        currentVersion = "1.0.0",
        softwareType = "main"
    )

    override fun onCleared() {
        chunkSender.stop()
        memfaultApi.deinit()
    }

    fun getLatestRelease() {
        memfaultApi.getLatestRelease(deviceInfo, callback = object : GetLatestReleaseCallback {

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
    }

    @ExperimentalUnsignedTypes
    fun postChunks() {
        chunkSender.postChunks(
            listOf(CHUNK_ONE, CHUNK_TWO),
            callback = object : PostChunksCallback {
                override fun onError(e: Exception, sent: Int) {
                    Log.e("MFLT-SAMPLE", "Error: sent $sent ", e)
                }

                override fun onQueueEmpty(sent: Int) {
                    Log.i("MFLT-SAMPLE", "Queue empty: sent $sent")
                }

                override fun onRetryAfterDelay(delay: Long, sent: Int) {
                    Log.i("MFLT-SAMPLE", "Retry: delay $delay sent $sent")
                }
            })
    }

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
