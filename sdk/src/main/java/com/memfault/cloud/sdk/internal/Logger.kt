package com.memfault.cloud.sdk.internal

import android.util.Log
import com.memfault.cloud.sdk.MemfaultCloud

object Logger {
    const val TAG = "MFLT-SDK"
    @JvmStatic
    var minLevel: MemfaultCloud.LogLevel = MemfaultCloud.LogLevel.NONE

    @JvmStatic
    fun e(message: String) = log(MemfaultCloud.LogLevel.ERROR, message)

    @JvmStatic
    fun w(message: String) = log(MemfaultCloud.LogLevel.WARN, message)

    @JvmStatic
    fun i(message: String) = log(MemfaultCloud.LogLevel.INFO, message)

    @JvmStatic
    fun d(message: String) = log(MemfaultCloud.LogLevel.DEBUG, message)

    @JvmStatic
    fun v(message: String) = log(MemfaultCloud.LogLevel.VERBOSE, message)

    @JvmStatic
    fun e(message: String, t: Throwable) = log(MemfaultCloud.LogLevel.ERROR, message, t)

    @JvmStatic
    fun w(message: String, t: Throwable) = log(MemfaultCloud.LogLevel.WARN, message, t)

    @JvmStatic
    fun i(message: String, t: Throwable) = log(MemfaultCloud.LogLevel.INFO, message, t)

    @JvmStatic
    fun d(message: String, t: Throwable) = log(MemfaultCloud.LogLevel.DEBUG, message, t)

    @JvmStatic
    fun v(message: String, t: Throwable) = log(MemfaultCloud.LogLevel.VERBOSE, message, t)

    @JvmStatic
    private fun log(level: MemfaultCloud.LogLevel, message: String) {
        if (level > minLevel) return
        when (level) {
            MemfaultCloud.LogLevel.ERROR -> Log.e(TAG, message)
            MemfaultCloud.LogLevel.WARN -> Log.w(TAG, message)
            MemfaultCloud.LogLevel.INFO -> Log.i(TAG, message)
            MemfaultCloud.LogLevel.DEBUG -> Log.d(TAG, message)
            MemfaultCloud.LogLevel.VERBOSE -> Log.v(TAG, message)
            else -> return
        }
    }

    @JvmStatic
    private fun log(level: MemfaultCloud.LogLevel, message: String, t: Throwable) {
        if (level > minLevel) return
        when (level) {
            MemfaultCloud.LogLevel.ERROR -> Log.e(TAG, message, t)
            MemfaultCloud.LogLevel.WARN -> Log.w(TAG, message, t)
            MemfaultCloud.LogLevel.INFO -> Log.i(TAG, message, t)
            MemfaultCloud.LogLevel.DEBUG -> Log.d(TAG, message, t)
            MemfaultCloud.LogLevel.VERBOSE -> Log.v(TAG, message, t)
            else -> return
        }
    }
}
