package com.memfault.cloud.sdk.internal

import android.util.Log
import com.memfault.cloud.sdk.MemfaultApi

object Logger {
    const val TAG = "MFLT-SDK"
    @JvmStatic
    var minLevel: MemfaultApi.LogLevel = MemfaultApi.LogLevel.NONE

    @JvmStatic
    fun e(message: String) = log(MemfaultApi.LogLevel.ERROR, message)

    @JvmStatic
    fun w(message: String) = log(MemfaultApi.LogLevel.WARN, message)

    @JvmStatic
    fun i(message: String) = log(MemfaultApi.LogLevel.INFO, message)

    @JvmStatic
    fun d(message: String) = log(MemfaultApi.LogLevel.DEBUG, message)

    @JvmStatic
    fun v(message: String) = log(MemfaultApi.LogLevel.VERBOSE, message)

    @JvmStatic
    fun e(message: String, t: Throwable) = log(MemfaultApi.LogLevel.ERROR, message, t)

    @JvmStatic
    fun w(message: String, t: Throwable) = log(MemfaultApi.LogLevel.WARN, message, t)

    @JvmStatic
    fun i(message: String, t: Throwable) = log(MemfaultApi.LogLevel.INFO, message, t)

    @JvmStatic
    fun d(message: String, t: Throwable) = log(MemfaultApi.LogLevel.DEBUG, message, t)

    @JvmStatic
    fun v(message: String, t: Throwable) = log(MemfaultApi.LogLevel.VERBOSE, message, t)

    @JvmStatic
    private fun log(level: MemfaultApi.LogLevel, message: String) {
        if (level > minLevel) return
        when (level) {
            MemfaultApi.LogLevel.ERROR -> Log.e(TAG, message)
            MemfaultApi.LogLevel.WARN -> Log.w(TAG, message)
            MemfaultApi.LogLevel.INFO -> Log.i(TAG, message)
            MemfaultApi.LogLevel.DEBUG -> Log.d(TAG, message)
            MemfaultApi.LogLevel.VERBOSE -> Log.v(TAG, message)
            else -> return
        }
    }

    @JvmStatic
    private fun log(level: MemfaultApi.LogLevel, message: String, t: Throwable) {
        if (level > minLevel) return
        when (level) {
            MemfaultApi.LogLevel.ERROR -> Log.e(TAG, message, t)
            MemfaultApi.LogLevel.WARN -> Log.w(TAG, message, t)
            MemfaultApi.LogLevel.INFO -> Log.i(TAG, message, t)
            MemfaultApi.LogLevel.DEBUG -> Log.d(TAG, message, t)
            MemfaultApi.LogLevel.VERBOSE -> Log.v(TAG, message, t)
            else -> return
        }
    }
}
