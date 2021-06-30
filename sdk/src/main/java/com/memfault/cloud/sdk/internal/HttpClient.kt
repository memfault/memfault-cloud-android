package com.memfault.cloud.sdk.internal

import android.net.Uri
import java.io.Closeable
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.util.Locale
import java.util.UUID
import okio.Buffer
import okio.buffer
import okio.sink

private const val TWO_HYPHENS = "--"
private const val CRLF = "\r\n"

interface RequestBody {
    fun contentType(): String?
    fun writeTo(outputStream: OutputStream)
    fun contentLength(): Int
}

data class Part(
    internal val body: RequestBody
)

data class DataRequestBody(
    internal val byteArray: ByteArray,
    private val contentType: String? = null
) : RequestBody {
    override fun contentType(): String? = contentType

    override fun writeTo(outputStream: OutputStream) {
        outputStream.sink().buffer().let {
            it.write(byteArray)
            it.flush()
        }
    }

    override fun contentLength(): Int = byteArray.size
}

data class MixedMultipartBody(
    private val parts: List<Part>,
    private val boundary: String = UUID.randomUUID().toString()
) : RequestBody {
    private val totalContentLength: Int by lazy {
        Buffer().also {
            this.writeTo(it.outputStream())
        }.size.toInt()
    }

    override fun contentType(): String {
        return "multipart/mixed; boundary=$boundary"
    }

    override fun writeTo(outputStream: OutputStream) {
        outputStream.sink().buffer().let { sink ->
            for (part in parts) {
                sink.writeUtf8(TWO_HYPHENS)
                sink.writeUtf8(boundary)
                sink.writeUtf8(CRLF)

                part.body.contentType()?.let {
                    sink.writeUtf8("Content-Type: ")
                        .writeUtf8(it)
                        .writeUtf8(CRLF)
                }

                sink.writeUtf8("Content-Length: ")
                    .writeDecimalLong(part.body.contentLength().toLong())
                    .writeUtf8(CRLF)

                sink.writeUtf8(CRLF)
                sink.flush()
                part.body.writeTo(sink.outputStream())
                sink.writeUtf8(CRLF)
            }
            sink.writeUtf8(TWO_HYPHENS)
            sink.writeUtf8(boundary)
            sink.writeUtf8(TWO_HYPHENS)
            sink.writeUtf8(CRLF)
            sink.flush()
        }
    }

    override fun contentLength(): Int = totalContentLength
}

// NB: While `getHeaderFields` claims to return `Map<String, String>` it is expected
// to contain a `null` key if it comes from the cache (containing the status code):
//
// null => "HTTP/1.1 200 OK"
//
// See:
// - https://issuetracker.google.com/issues/37032975
// - https://github.com/square/okhttp/issues/1523
// - https://developer.android.com/reference/java/net/CacheResponse.html#getHeaders()
class HttpHeaderMap(private val mixedCaseHeaders: Map<String?, String>) : Map<String?, String> by mixedCaseHeaders {
    private val headersByLowercaseKey by lazy { mixedCaseHeaders.mapKeys { it.key?.toLowerCase(Locale.ROOT) } }

    override fun containsKey(key: String?) =
        headersByLowercaseKey.containsKey(key?.toLowerCase(Locale.ROOT))

    override fun get(key: String?): String? =
        headersByLowercaseKey.get(key?.toLowerCase(Locale.ROOT))

    override val entries: Set<Map.Entry<String?, String>>
        get() = headersByLowercaseKey.entries

    override val keys: Set<String?>
        get() = headersByLowercaseKey.keys
}

/**
 * A simpler wrapper interface, to avoid polluting the [HttpResponse] API with a [HttpURLConnection].
 */
interface CloseableConnection {
    fun disconnect()
}

data class HttpResponse(
    val code: Int,
    val message: String,
    val body: InputStream,
    val headers: HttpHeaderMap,
    val connection: CloseableConnection
) : Closeable {
    override fun close() {
        try {
            body.close()
            connection.disconnect()
        } catch (rethrown: RuntimeException) {
            throw rethrown
        } catch (_: Exception) {
        }
    }
}

fun HttpResponse?.isSucessful(): Boolean = if (this == null) false else this.code < 300

internal interface HttpClient {
    fun request(
        method: String,
        uri: Uri,
        headers: Map<String, String>? = null,
        requestBody: RequestBody? = null
    ): HttpResponse?
}
