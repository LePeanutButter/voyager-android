package com.voyager.tourism.data.interceptor

import com.voyager.tourism.data.local.PreferencesManager
import com.voyager.tourism.data.session.SessionInvalidationNotifier
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test

class UnauthorizedResponseInterceptorTest {

    @Test
    fun `401 clears session and notifies`() {
        val prefs = mockk<PreferencesManager>(relaxed = true)
        val notifier = mockk<SessionInvalidationNotifier>(relaxed = true)
        val request = Request.Builder().url("https://api.example.com/").build()
        val response401 = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(401)
            .message("Unauthorized")
            .body("".toResponseBody(null))
            .build()

        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns response401

        UnauthorizedResponseInterceptor(prefs, notifier).intercept(chain)

        verify { prefs.clearAuthData() }
        verify { notifier.notifySessionExpired() }
    }

    @Test
    fun `200 does not clear session`() {
        val prefs = mockk<PreferencesManager>(relaxed = true)
        val notifier = mockk<SessionInvalidationNotifier>(relaxed = true)
        val request = Request.Builder().url("https://api.example.com/").build()
        val response200 = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("{}".toResponseBody(null))
            .build()

        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        every { chain.proceed(request) } returns response200

        UnauthorizedResponseInterceptor(prefs, notifier).intercept(chain)

        verify(exactly = 0) { prefs.clearAuthData() }
        verify(exactly = 0) { notifier.notifySessionExpired() }
    }
}
