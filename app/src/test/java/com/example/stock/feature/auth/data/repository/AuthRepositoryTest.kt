package com.example.stock.feature.auth.data.repository

import com.example.stock.core.data.auth.TokenProvider
import com.example.stock.core.data.local.TokenStore
import com.example.stock.feature.auth.data.remote.AuthApi
import com.example.stock.feature.auth.data.remote.LoginRequest
import com.example.stock.feature.auth.data.remote.LoginResponse
import com.example.stock.feature.auth.data.remote.SignupRequest
import com.example.stock.feature.auth.data.remote.SignupResponse
import com.example.stock.util.TestDispatcherProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Unit tests for [AuthRepository].
 *
 * Tests cover login, logout, signup, and token checking functionality.
 * Uses MockK for mocking dependencies and Truth for assertions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {
    private val api: AuthApi = mockk()
    private val tokenStore: TokenStore = mockk(relaxed = true)
    private val tokenProvider: TokenProvider = mockk(relaxed = true)

    private val scheduler = TestCoroutineScheduler()
    private lateinit var dispatcherProvider: TestDispatcherProvider

    private lateinit var repo: AuthRepository

    @Before
    fun setUp() {
        dispatcherProvider = TestDispatcherProvider(scheduler)
        repo = AuthRepository(
            api = api,
            tokenStore = tokenStore,
            tokenProvider = tokenProvider,
            dispatcherProvider = dispatcherProvider
        )
    }

    // region Login Tests

    @Test
    fun `login success - updates provider and saves token`() =
        runTest(scheduler) {
            val email = "test@example.com"
            val password = "password"
            val response = LoginResponse(token = "token_123")

            coEvery { api.login(LoginRequest(email, password)) } returns response

            repo.login(email, password)

            coVerify(exactly = 1) { tokenProvider.update("token_123") }
            coVerify(exactly = 1) { tokenStore.save("token_123") }
            coVerify(exactly = 1) { api.login(LoginRequest(email, password)) }
        }

    @Test
    fun `login fails when api returns error - token not updated`() = runTest(scheduler) {
        val email = "test@example.com"
        val password = "wrong_password"
        val errorResponse = Response.error<LoginResponse>(
            401,
            "Unauthorized".toResponseBody(null)
        )

        coEvery { api.login(LoginRequest(email, password)) } throws HttpException(errorResponse)

        val result = runCatching { repo.login(email, password) }

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(HttpException::class.java)
        // Token should not be updated when API fails
        verify { tokenProvider wasNot Called }
        verify { tokenStore wasNot Called }
    }

    @Test
    fun `login fails when token store save throws - provider remains updated in current impl`() =
        runTest(scheduler) {
            // In current implementation, if save fails, exception propagates without rollback
            val email = "test@example.com"
            val pass = "password"
            val response = LoginResponse(token = "token123")

            coEvery { api.login(LoginRequest(email, pass)) } returns response
            coEvery { tokenStore.save("token123") } throws IOException("disk full")

            val result = runCatching { repo.login(email, pass) }

            assertThat(result.isFailure).isTrue()
            assertThat(result.exceptionOrNull()).isInstanceOf(IOException::class.java)
            assertThat(result.exceptionOrNull()?.message).isEqualTo("disk full")
            // tokenProvider.update is called (no rollback expected)
            coVerify(exactly = 1) { tokenProvider.update("token123") }
            coVerify(exactly = 1) { tokenStore.save("token123") }
        }

    // endregion

    // region Logout Tests

    @Test
    fun `logout - clears provider and store`() = runTest(scheduler) {
        // when
        repo.logout()

        // then
        coVerify(exactly = 1) { tokenProvider.clear() }
        coVerify(exactly = 1) { tokenStore.clear() }
    }

    // endregion

    // region Signup Tests

    @Test
    fun `signup success - returns response message`() = runTest(scheduler) {
        val email = "new@example.com"
        val password = "password123"
        val response = SignupResponse(message = "User created successfully")

        coEvery { api.signup(SignupRequest(email, password)) } returns response

        val result = repo.signup(email, password)

        assertThat(result.message).isEqualTo("User created successfully")
        coVerify(exactly = 1) { api.signup(SignupRequest(email, password)) }
    }

    @Test
    fun `signup fails when api returns error`() = runTest(scheduler) {
        val email = "existing@example.com"
        val password = "password123"
        val errorResponse = Response.error<SignupResponse>(
            409,
            "User already exists".toResponseBody(null)
        )

        coEvery { api.signup(SignupRequest(email, password)) } throws HttpException(errorResponse)

        val result = runCatching { repo.signup(email, password) }

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isInstanceOf(HttpException::class.java)
    }

    // endregion

    // region HasToken Tests

    @Test
    fun `hasToken returns true when token exists`() = runTest(scheduler) {
        coEvery { tokenProvider.awaitRestoration() } returns Unit
        every { tokenProvider.getToken() } returns "valid_token"

        val result = repo.hasToken()

        assertThat(result).isTrue()
    }

    @Test
    fun `hasToken returns false when token is null`() = runTest(scheduler) {
        coEvery { tokenProvider.awaitRestoration() } returns Unit
        every { tokenProvider.getToken() } returns null

        val result = repo.hasToken()

        assertThat(result).isFalse()
    }

    // endregion
}