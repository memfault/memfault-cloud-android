package com.memfault.cloud.sdk.internal

import android.net.Uri
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLSocketFactory

private const val CONNECT_TIMEOUT_MS = 10000
private const val READ_TIMEOUT_MS = 60000

internal class UrlConnectionHttpClient : HttpClient {

    override fun request(
        method: String,
        uri: Uri,
        headers: Map<String, String>?,
        requestBody: RequestBody?
    ): HttpResponse? {
        val urlString = uri.toString()
        Logger.v("$method $urlString")
        val url = URL(urlString)
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
            }

            when (urlConnection) {
                is HttpsURLConnection ->
                    urlConnection.sslSocketFactory =
                        SSLSocketFactory.getDefault() as SSLSocketFactory
            }
            headers?.forEach {
                urlConnection.addRequestProperty(it.key, it.value)
            }
            requestBody?.let {
                it.contentType()?.let { contentType ->
                    urlConnection.addRequestProperty("Content-Type", contentType)
                }
                urlConnection.setFixedLengthStreamingMode(it.contentLength())
                urlConnection.doOutput = true
                urlConnection.outputStream.use { outputStream ->
                    it.writeTo(outputStream)
                    outputStream.flush()
                }
            }

            // ////////////////////////////////////////////////////////////
            // This starts the connection
            val responseCode = urlConnection.responseCode
            val responseMessage = urlConnection.responseMessage
            val rawHeaders = urlConnection.headerFields as Map<String?, List<String>>

            when {
                (responseCode > 299) -> urlConnection.errorStream
                else -> urlConnection.inputStream
            }.let { responseBodyStream ->
                val formattedResponseHeaders = rawHeaders.map {
                    it.key to it.value.joinToString()
                }.toMap()
                return HttpResponse(
                    responseCode,
                    responseMessage,
                    responseBodyStream,
                    HttpHeaderMap(formattedResponseHeaders),
                    object : CloseableConnection {
                        override fun disconnect() {
                            urlConnection.disconnect()
                        }
                    }
                )
            }
        } catch (e: Exception) {
            Logger.e("$method failed $urlString", e)
        }
        urlConnection?.disconnect()
        return null
    }
}
