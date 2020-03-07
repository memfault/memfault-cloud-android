package com.memfault.cloud.sdk.internal

import android.net.Uri
import okio.Buffer
import okio.buffer
import okio.sink
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

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

data class HttpResponse(
    val code: Int,
    val message: String,
    val body: InputStream,
    val headers: Map<String, String>
)

fun HttpResponse?.isSucessful(): Boolean = if (this == null) false else this.code < 300

internal interface HttpClient {
    fun request(
        method: String,
        uri: Uri,
        headers: Map<String, String>? = null,
        requestBody: RequestBody? = null
    ): HttpResponse?
}
