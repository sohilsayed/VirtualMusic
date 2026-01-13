package com.canopus.Vmusic.auth

import com.canopus.Vmusic.data.api.HolodexApiService
import com.canopus.Vmusic.data.api.LoginRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Orchestrates the entire authentication flow, from exchanging the Discord
 * auth code to logging into the Holodex backend.
 */
class AuthRepository(
    private val holodexApiService: HolodexApiService,
    private val authService: AuthorizationService
) {

    /**
     * Exchanges a one-time authorization code from Discord for an access token.
     * This is a suspending function that wraps the AppAuth callback-based API.
     */
    suspend fun exchangeDiscordCodeForToken(tokenRequest: TokenRequest): TokenResponse {
        return suspendCoroutine { continuation ->
            authService.performTokenRequest(tokenRequest) { response, ex ->
                if (response != null) {
                    continuation.resume(response)
                } else {
                    continuation.resumeWithException(
                        ex ?: IllegalStateException("Token exchange failed with null exception")
                    )
                }
            }
        }
    }

    /**
     * Uses the Discord access token to log into the Holodex backend and get a JWT.
     */
    suspend fun loginToHolodex(discordAccessToken: String): String {
        val request = LoginRequest(service = "discord", token = discordAccessToken)
        val response = holodexApiService.login(request)

        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.jwt
        } else {
            throw Exception("Holodex login failed: ${response.errorBody()?.string()}")
        }
    }
}