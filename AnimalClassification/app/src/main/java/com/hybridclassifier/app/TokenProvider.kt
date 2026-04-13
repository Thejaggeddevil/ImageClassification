package com.hybridclassifier.app.data

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Simple in-memory token store.
 * Set immediately on login — no DataStore race condition.
 * OkHttp interceptor reads from here for every API call.
 */
@Singleton
class TokenProvider @Inject constructor() {
    @Volatile
    private var token: String? = null

    fun getToken(): String? = token

    fun setToken(newToken: String?) {
        token = newToken
    }

    fun clear() {
        token = null
    }

    fun hasToken(): Boolean = token != null
}
