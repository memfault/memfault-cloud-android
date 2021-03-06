package com.memfault.cloud.sdk

import com.memfault.cloud.sdk.MemfaultCloud.Builder
import com.memfault.cloud.sdk.internal.GetLatestReleaseTask
import com.memfault.cloud.sdk.internal.Logger
import com.memfault.cloud.sdk.internal.MainThreadExecutor
import com.memfault.cloud.sdk.internal.MemfaultHttpApi
import com.memfault.cloud.sdk.internal.UrlConnectionHttpClient
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * The Memfault SDK entrypoint.
 *
 * ## Initialization
 *
 * We recommend creating only one instance of [MemfaultCloud] and using it across your entire
 * application. Use the [Builder] to create an instance.
 *
 * Kotlin:
 *
 * ```kotlin
 * val memfault = MemfaultCloud.Builder()
 *   .setApiKey(PROJECT_KEY)
 *   .build()
 * ```
 *
 * Java:
 *
 * ```java
 * final Memfault memfault = new Memfault.Builder()
 *    .setApiKey(PROJECT_KEY)
 *    .build();
 * ```
 *
 * To find your `PROJECT_KEY`, log in to [https://app.memfault.com/] and navigate to Settings.
 *
 * ## Getting the Latest Release
 *
 * You can check to see whether there's a new update for a particular device with
 * [MemfaultCloud.getLatestRelease]:
 *
 * Kotlin:
 *
 * ```kotlin
 * memfaultCloud.getLatestRelease(deviceInfo, callback = object : GetLatestReleaseCallback {
 *
 *   override fun onUpdateAvailable(otaPackage: MemfaultOtaPackage) {
 *     // There's a new software package
 *   }
 *
 *   override fun onUpToDate() {
 *     // Device is up to date, nothing to do!
 *   }
 *
 *   override fun onError(e: Exception) {
 *     // There was an error; handle it here
 *   }
 * })
 * ```
 *
 * Java:
 *
 * ```java
 * final GetLatestReleaseCallback callback = new GetLatestReleaseCallback() {
 *   @Override
 *   public void onUpdateAvailable(@NonNull MemfaultOtaPackage otaPackage) {
 *     // There's a new software package
 *   }
 *
 *   @Override
 *   public void onUpToDate() {
 *     // Device is up to date, nothing to do!
 *   }
 *
 *   @Override
 *   public void onError(@NonNull Exception e) {
 *     // There was an error; handle it here
 *   }
 * };
 * memfaultCloud.getLatestRelease(callback);
 * ```
 *
 * ## Permissions
 *
 * The SDK requires several permissions in order to implement all functionality. Callers should check for these permissions and,
 * if necessary, [request them][https://developer.android.com/training/permissions/requesting] from the user.
 *
 * - [Network][1]: [`INTERNET`][2]; [`ACCESS_NETWORK_STATE`][3]
 *
 * [1]: https://developer.android.com/training/basics/network-ops/connecting.html
 * [2]: https://developer.android.com/reference/android/Manifest.permission.html#INTERNET
 * [3]: https://developer.android.com/reference/android/Manifest.permission.html#ACCESS_NETWORK_STATE
 */
class MemfaultCloud internal constructor(
    internal val mainThreadExecutor: Executor,
    internal val workerThreadExecutor: Executor,
    private val baseApiUrl: String,
    private val baseChunksUrl: String,
    private val apiKey: String
) {

    /**
     * Stop and clean up any active tasks.
     *
     * This signals the end of the instance's lifecycle. The instance should not be used after calling this method.
     */
    fun deinit() {
        Logger.v("deinit")

        if (mainThreadExecutor is ExecutorService) {
            Logger.v("Shutting down main thread thread executor")
            mainThreadExecutor.shutdownNow()
        }
        if (workerThreadExecutor is ExecutorService) {
            Logger.v("Shutting down worker thread executor")
            workerThreadExecutor.shutdownNow()
        }
    }

    /**
     * Build a new [MemfaultCloud] instance.
     *
     * Calling [setApiKey] is required before calling [build].
     */
    class Builder {
        private var apiKey: String? = null

        /**
         * @suppress
         */
        internal var mainThreadExecutor: Executor? = null

        /**
         * @suppress
         */
        internal var workerThreadExecutor: Executor? = null

        /**
         * @suppress
         */
        var baseApiUrl = BuildConfig.MEMFAULT_BASE_API_URL

        /**
         * @suppress
         */
        var baseChunksUrl = BuildConfig.MEMFAULT_BASE_CHUNKS_URL

        /**
         * Provide the project key.
         */
        fun setApiKey(apiKey: String): Builder {
            this.apiKey = apiKey
            return this
        }

        /**
         * Create the [MemfaultCloud] instance using the configured values.
         */
        fun build(): MemfaultCloud = MemfaultCloud(
            mainThreadExecutor = mainThreadExecutor ?: MainThreadExecutor(),
            workerThreadExecutor = workerThreadExecutor ?: dynamicSingleThreadExecutor(),
            baseApiUrl = baseApiUrl,
            baseChunksUrl = baseChunksUrl,
            apiKey = checkNotNull(apiKey)
        )
    }

    /**
     * Queries Memfault's services to find if a new update is available.
     *
     * @param deviceInfo the device for which to check for an update.
     * @param callback called when a result has been determined.
     */
    fun getLatestRelease(deviceInfo: MemfaultDeviceInfo, callback: GetLatestReleaseCallback) {
        workerThreadExecutor.execute(
            GetLatestReleaseTask(
                deviceInfo, mainThreadExecutor, buildMemfaultHttpApi(), callback
            )
        )
    }

    internal fun buildMemfaultHttpApi() = MemfaultHttpApi(
        httpClient = UrlConnectionHttpClient(),
        baseApiUrl = baseApiUrl,
        baseChunksUrl = baseChunksUrl,
        apiKey = apiKey
    )

    enum class LogLevel(internal val level: Int) {
        NONE(0),
        ERROR(1),
        WARN(2),
        INFO(3),
        DEBUG(4),
        VERBOSE(5)
    }

    companion object {
        @JvmStatic
        internal fun dynamicSingleThreadExecutor(): ExecutorService = ThreadPoolExecutor(
            0, 1, 5L, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>()
        )

        /**
         * Logs at `level` and above will be logged to LogCat.
         */
        @JvmStatic
        fun setMinLogLevel(level: LogLevel) {
            Logger.minLevel = level
        }
    }
}
