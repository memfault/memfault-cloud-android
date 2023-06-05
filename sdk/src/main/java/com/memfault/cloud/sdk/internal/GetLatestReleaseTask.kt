package com.memfault.cloud.sdk.internal

import com.memfault.cloud.sdk.GetLatestReleaseCallback
import com.memfault.cloud.sdk.MemfaultDeviceInfo
import com.memfault.cloud.sdk.MemfaultOtaPackage
import java.io.IOException
import java.util.concurrent.Executor
import okio.buffer
import okio.source
import org.json.JSONException
import org.json.JSONObject

private const val HTTP_NO_CONTENT = 204

class GetLatestReleaseTask internal constructor(
    private val deviceInfo: MemfaultDeviceInfo,
    private val executor: Executor,
    private val memfaultHttpApi: MemfaultHttpApi,
    private val callback: GetLatestReleaseCallback
) : Runnable {

    internal fun createCallbackTaskFromResponse(response: HttpResponse?): Runnable {
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

        val body = response?.body?.source()?.buffer()?.readUtf8()

        return try {
            checkNotNull(body) {
                throw JSONException("Empty response body")
            }
            val jsonObject = JSONObject(body)
            val otaPackage = responseToOtaPackage(jsonObject)
            Runnable { callback.onUpdateAvailable(otaPackage) }
        } catch (e: JSONException) {
            Logger.e("Failed to parse JSON response", e)
            Runnable { callback.onError(e) }
        }
    }

    override fun run() = memfaultHttpApi.getLatestRelease(deviceInfo).use {
        createCallbackTaskFromResponse(it)
    }.let {
        executor.execute(it)
    }

    companion object {
        private const val ARTIFACTS = "artifacts"
        private const val URL = "url"
        private const val RELEASE_NOTES = "notes"
        private const val APP_VERSION = "version"
        private const val MD5 = "md5"
        private const val EXTRA_INFO = "extra_info"
        private const val IS_FORCED = "is_forced"

        internal fun responseToOtaPackage(responseJson: JSONObject): MemfaultOtaPackage {
            val artifactsObject = responseJson.getJSONArray(ARTIFACTS).getJSONObject(0)
            val url = artifactsObject.getString(URL)
            val releaseNotes = responseJson.getString(RELEASE_NOTES)
            val appVersion = responseJson.getString(APP_VERSION)
            val isForced = responseJson.getBooleanOrNull(IS_FORCED)
            val md5 = artifactsObject.getString(MD5)
            val artifactsExtraInfo = extraInfoToMap(artifactsObject)
            val releaseExtraInfo = extraInfoToMap(responseJson)

            return MemfaultOtaPackage(
                location = url,
                releaseNotes = releaseNotes,
                appVersion = appVersion,
                md5 = md5,
                extraInfo = artifactsExtraInfo,
                releaseExtraInfo = releaseExtraInfo,
                isForced = isForced,
            )
        }

        private fun extraInfoToMap(artifactsObject: JSONObject): Map<String, String> = try {
            val extraInfoObj = artifactsObject.getJSONObject(EXTRA_INFO)
            extraInfoObj.keys().asSequence().map {
                it to extraInfoObj.getString(it)
            }.toMap()
        } catch (e: JSONException) {
            emptyMap()
        }
    }
}

/**
 * There is no nullable boolean getter on JSONObject - add our own.
 */
private fun JSONObject.getBooleanOrNull(key: String): Boolean? = try {
    getBoolean(key)
} catch (e: JSONException) {
    null
}
