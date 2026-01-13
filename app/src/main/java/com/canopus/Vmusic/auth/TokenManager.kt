package com.canopus.Vmusic.auth

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class TokenManager(context: Context) {

    companion object {
        private const val PREF_FILE_NAME = "auth_token_prefs"
        private const val KEY_HOLODEX_JWT = "holodex_jwt"
        private const val KEY_USER_ID = "user_id"
    }

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        PREF_FILE_NAME,
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveJwt(token: String) {
        sharedPreferences.edit {
            putString(KEY_HOLODEX_JWT, token)
        }
    }

    fun getJwt(): String? {
        return sharedPreferences.getString(KEY_HOLODEX_JWT, null)
    }

    fun saveUserId(id: String) {
        sharedPreferences.edit {
            putString(KEY_USER_ID, id)
        }
    }

    fun getUserId(): String? {
        return sharedPreferences.getString(KEY_USER_ID, null)
    }


    fun clearJwt() {
        sharedPreferences.edit {
            remove(KEY_HOLODEX_JWT)
            remove(KEY_USER_ID)
        }
    }
}