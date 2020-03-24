package com.memfault.cloud.sdk.internal

import com.memfault.cloud.sdk.GetLatestReleaseCallback
import com.memfault.cloud.sdk.MemfaultDeviceInfo
import com.memfault.cloud.sdk.MemfaultOtaPackage
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream

import java.util.concurrent.Executor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

internal class GetLatestReleaseTaskTest {

    @Test
    fun createCallbackFromResponse_returnsOnErrorIfNoResponse() {
        val callback : GetLatestReleaseCallback = mock()
        createTask(callback).createCallbackTaskFromResponse(null).run()
        verify(callback).onError(any<IOException>())
    }

    @Test
    fun createCallbackFromResponse_returnsOnErrorOnSadStatusCode() {
        val mockResponse = mock<HttpResponse> {
            on { code } doReturn 300
        }
        val callback: GetLatestReleaseCallback = mock()
        createTask(callback).createCallbackTaskFromResponse(mockResponse).run()
        verify(callback).onError(any())
    }

    @Test
    fun createCallbackFromResponse_returnsUpToDateOn204() {
        val mockResponse = mock<HttpResponse> {
            on { code } doReturn 204
        }
        val callback: GetLatestReleaseCallback = mock()
        createTask(callback).createCallbackTaskFromResponse(mockResponse).run()
        verify(callback).onUpToDate()
    }

    @Test
    fun createCallbackFromResponse_returnsOtaPackageOnTwoHundred() {
        val mockResponse = mock<HttpResponse> {
            on { code } doReturn 200
            on { body } doReturn RESPONSE.byteInputStream(Charsets.UTF_8) as InputStream
        }
        val callback: GetLatestReleaseCallback = mock()
        createTask(callback).createCallbackTaskFromResponse(mockResponse).run()

        argumentCaptor<MemfaultOtaPackage>().apply {
            verify(callback).onUpdateAvailable(capture())
            assertEquals(APP_VERSION, firstValue.appVersion)
        }
    }

    @Test
    fun jsonToOtaPackage_createdFromValidResponse() {
        val otaPackage =
            GetLatestReleaseTask.jsonToOtaPackage(JSON_OBJECT)
        assertEquals(LOCATION, otaPackage.location)
        assertEquals(RELEASE_NOTES, otaPackage.releaseNotes)
        assertEquals(APP_VERSION, otaPackage.appVersion)
    }

    @Test
    fun jsonToOtaPackage_throwsWithEmptyJson() {
        try {
            GetLatestReleaseTask.jsonToOtaPackage(JSONObject())
        } catch (e: JSONException) {
            return
        }
        fail("Expected exception")
    }

    companion object {
        val LOCATION = """
https://bar.s3.amazonaws.com/foo
""".replace("\n", "")
        const val RELEASE_NOTES = ""
        const val APP_VERSION = "1.0.0"
        const val RESPONSE = """
{
    "artifacts": [
        {
            "build_id": "",
            "created_date": "2019-05-22T21:43:20.356397+00:00",
            "filename": "Screen Shot 2019-05-21 at 8.20.50 AM.png",
            "hardware_version": "proto",
            "id": 63,
            "md5": "43c821cfb039f59aa81078f60885abe4",
            "type": "firmware",
            "updated_date": "2019-05-22T21:43:20.360850+00:00",
            "url": "https://bar.s3.amazonaws.com/foo"
        }
    ],
    "count_devices": 1,
    "created_date": "2019-04-28T07:47:07.840855+00:00",
    "extra_info": null,
    "id": 37,
    "min_version": "",
    "notes": "",
    "revision": "",
    "updated_date": "2019-04-28T07:47:07.840866+00:00",
    "version": "1.0.0"
}
"""
        val JSON_OBJECT = JSONObject(RESPONSE)
    }

    private val device = MemfaultDeviceInfo(
        deviceSerial = "abc",
        hardwareVersion = "proto",
        currentVersion = "1.0.0",
        softwareType = "main"
    )

    private val executor: Executor = Executor { p0 -> p0.run() }

    fun createTask(callback: GetLatestReleaseCallback) = GetLatestReleaseTask(
        deviceInfo = device,
        executor = executor,
        memfaultHttpApi = mock(),
        callback = callback
    )
}
