package com.memfault.cloud.sdk.internal

import com.memfault.cloud.sdk.GetLatestReleaseCallback
import com.memfault.cloud.sdk.MemfaultDeviceInfo
import com.memfault.cloud.sdk.MemfaultOtaPackage
import okio.buffer
import okio.source
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.Executor

private const val HTTP_NO_CONTENT = 204

class GetLatestReleaseTask internal constructor(
    private val deviceInfo: MemfaultDeviceInfo,
    private val executor: Executor,
    private val memfaultHttpApi: MemfaultHttpApi,
    private val callback: GetLatestReleaseCallback
) : Runnable {

    private fun createCallbackTaskFromResponse(response: HttpResponse?): Runnable {
        when {
            (response == null) -> return Runnable {
                callback.onError(IOException("Network unavailable"))
            }
            (response.code > 299) -> return Runnable {
                callback.onError(Exception("Unexpected response: ${response.code} ${response.message}"))
            }
            response.code == HTTP_NO_CONTENT -> return Runnable {
                callback.onUpToDate()
            }
        }

        val body = response?.body?.source()?.buffer().use {
            it?.readUtf8()
        }

        try {
            checkNotNull(body) {
                throw JSONException("Empty response body")
            }
            val jsonObject = JSONObject(body)
            val otaPackage = jsonToOtaPackage(jsonObject)
            return Runnable { callback.onUpdateAvailable(otaPackage) }
        } catch (e: JSONException) {
            Logger.e("Failed to parse JSON response", e)
            return Runnable { callback.onError(e) }
        }
    }

    override fun run() {
        val response = memfaultHttpApi.getLatestRelease(deviceInfo)
        val callbackTask = createCallbackTaskFromResponse(response)
        executor.execute(callbackTask)
    }

    companion object {
        private const val ARTIFACTS = "artifacts"
        private const val URL = "url"
        private const val RELEASE_NOTES = "notes"
        private const val APP_VERSION = "version"

        internal fun jsonToOtaPackage(jsonObject: JSONObject): MemfaultOtaPackage {
            val artifactsObject = jsonObject.getJSONArray(ARTIFACTS).getJSONObject(0)
            val url = artifactsObject.getString(URL)
            val releaseNotes = jsonObject.getString(RELEASE_NOTES)
            val appVersion = jsonObject.getString(APP_VERSION)
            return MemfaultOtaPackage(url, releaseNotes, appVersion)
        }
    }
}