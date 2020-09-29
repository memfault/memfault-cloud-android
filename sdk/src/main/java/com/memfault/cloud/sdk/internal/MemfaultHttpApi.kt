package com.memfault.cloud.sdk.internal

import android.net.Uri
import com.memfault.cloud.sdk.BuildConfig
import com.memfault.cloud.sdk.MemfaultDeviceInfo
import java.util.UUID

internal class MemfaultHttpApi(
    private val httpClient: HttpClient,
    private val baseApiUrl: String,
    private val baseChunksUrl: String,
    private val apiKey: String
) {
    private fun buildBaseHeaders(): Map<String, String> {
        return mapOf(
            MEMFAULT_PROJECT_KEY_HEADER to apiKey,
            REQUEST_ID_HEADER to UUID.randomUUID().toString()
        )
    }

    fun getLatestRelease(deviceInfo: MemfaultDeviceInfo): HttpResponse? {
        val uri = Uri.parse(baseApiUrl)
            .buildUpon()
            .appendPath("api")
            .appendPath("v0")
            .appendPath("releases")
            .appendPath("latest")
            .appendQueryParameter("hardware_version", deviceInfo.hardwareVersion)
            .appendQueryParameter("current_version", deviceInfo.currentVersion)
            .appendQueryParameter("device_serial", deviceInfo.deviceSerial)
            .appendQueryParameter("software_type", deviceInfo.softwareType)
            .appendQueryParameter(SDK_PLATFORM_PARAM, SDK_PLATFORM_VALUE)
            .appendQueryParameter(SDK_VERSION_PARAM, BuildConfig.VERSION_NAME)
            .build()
        val headers = buildBaseHeaders()
        return request("GET", uri, headers)
    }

    fun postChunks(deviceSerial: String, chunks: List<ByteArray>): HttpResponse? {
        val uri = Uri.parse(baseChunksUrl)
            .buildUpon()
            .appendPath("api")
            .appendPath("v0")
            .appendPath("chunks")
            .appendPath(deviceSerial)
            .appendQueryParameter(SDK_PLATFORM_PARAM, SDK_PLATFORM_VALUE)
            .appendQueryParameter(SDK_VERSION_PARAM, BuildConfig.VERSION_NAME)
            .build()
        val headers = buildBaseHeaders()
        val parts = chunks.map { Part(DataRequestBody(it)) }
        val multipartBody = MixedMultipartBody(parts)
        return request("POST", uri, headers, multipartBody)
    }

    private fun request(
        method: String,
        uri: Uri,
        headers: Map<String, String>,
        body: RequestBody? = null
    ): HttpResponse? {
        Logger.v("$method ${uri.path} ${headers[REQUEST_ID_HEADER]}")
        return httpClient.request(method, uri, headers, body)
    }

    companion object {
        private const val MEMFAULT_PROJECT_KEY_HEADER = "Memfault-Project-Key"
        private const val REQUEST_ID_HEADER = "X-Request-ID"
        private const val SDK_VERSION_PARAM = "sdkVersion"
        private const val SDK_PLATFORM_PARAM = "sdkPlatform"
        private const val SDK_PLATFORM_VALUE = "android-cloud"
    }
}
