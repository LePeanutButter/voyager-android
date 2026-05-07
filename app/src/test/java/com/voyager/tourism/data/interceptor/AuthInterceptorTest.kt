package com.voyager.tourism.data.interceptor

import com.voyager.tourism.data.local.TokenManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthInterceptorTest {

    @Test
    fun `adds bearer when token present`() {
        val tokenManager = mockk<TokenManager>()
        every { tokenManager.getToken() } returns "abc123"

        val request = Request.Builder().url("https://api.example.com/v1/trips").build()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        var sent: Request? = null
        every { chain.proceed(any()) } answers {
            sent = firstArg()
            val r = sent!!
            Response.Builder()
                .request(r)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("".toResponseBody(null))
                .build()
        }

        val interceptor = AuthInterceptor(tokenManager)
        val response = interceptor.intercept(chain)

        assertEquals(200, response.code)
        verify { chain.proceed(any()) }
        assertEquals("Bearer abc123", sent!!.header("Authorization"))
    }

    @Test
    fun `no header when token null`() {
        val tokenManager = mockk<TokenManager>()
        every { tokenManager.getToken() } returns null

        val request = Request.Builder().url("https://api.example.com/").build()
        val chain = mockk<Interceptor.Chain>()
        every { chain.request() } returns request
        var sent: Request? = null
        every { chain.proceed(any()) } answers {
            sent = firstArg()
            val r = sent!!
            Response.Builder()
                .request(r)
                .protocol(Protocol.HTTP_1_1)
                .code(204)
                .message("No Content")
                .body("".toResponseBody(null))
                .build()
        }

        AuthInterceptor(tokenManager).intercept(chain)

        verify { chain.proceed(any()) }
        assertNull(sent!!.header("Authorization"))
    }
}
