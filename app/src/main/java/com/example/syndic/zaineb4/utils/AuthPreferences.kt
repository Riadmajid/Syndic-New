package com.example.syndic.zaineb4.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Helper to manage authentication preferences (Remember Me)
 */
class AuthPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_SAVED_EMAIL = "saved_email"
    }

    var rememberMe: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_ME, false)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply()

    var savedEmail: String
        get() = prefs.getString(KEY_SAVED_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SAVED_EMAIL, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
