package com.example.winly.data

import android.content.Context
import android.content.SharedPreferences

// ============================================================
// SESSION MANAGER - Menyimpan data user setelah login
// Solusi untuk masalah userId hardcode & tidak ada session
// ============================================================
class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("winly_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_USER_ID    = "user_id"
        const val KEY_NAME       = "name"
        const val KEY_EMAIL      = "email"
        const val KEY_ROLE       = "role"
        const val KEY_SISA_KUOTA = "sisa_kuota"
        const val KEY_INSTANSI   = "instansi"
        const val KEY_PHONE      = "phone"
        const val KEY_AVATAR     = "avatar_url"
        const val KEY_IS_LOGGED  = "is_logged_in"
    }

    // Simpan semua data user setelah login berhasil
    fun saveSession(
        id: Int, name: String, email: String, role: String,
        sisaKuota: Int, instansi: String, phone: String, avatarUrl: String
    ) {
        prefs.edit().apply {
            putInt(KEY_USER_ID, id)
            putString(KEY_NAME, name)
            putString(KEY_EMAIL, email)
            putString(KEY_ROLE, role)
            putInt(KEY_SISA_KUOTA, sisaKuota)
            putString(KEY_INSTANSI, instansi)
            putString(KEY_PHONE, phone)
            putString(KEY_AVATAR, avatarUrl)
            putBoolean(KEY_IS_LOGGED, true)
            apply()
        }
    }

    fun getUserId(): Int         = prefs.getInt(KEY_USER_ID, 0)
    fun getName(): String        = prefs.getString(KEY_NAME, "") ?: ""
    fun getEmail(): String       = prefs.getString(KEY_EMAIL, "") ?: ""
    fun getRole(): String        = prefs.getString(KEY_ROLE, "peserta") ?: "peserta"
    fun getSisaKuota(): Int      = prefs.getInt(KEY_SISA_KUOTA, 0)
    fun getInstansi(): String    = prefs.getString(KEY_INSTANSI, "") ?: ""
    fun getPhone(): String       = prefs.getString(KEY_PHONE, "") ?: ""
    fun getAvatarUrl(): String   = prefs.getString(KEY_AVATAR, "") ?: ""
    fun isLoggedIn(): Boolean    = prefs.getBoolean(KEY_IS_LOGGED, false)

    // Update sisa kuota setelah upload lomba
    fun updateSisaKuota(newKuota: Int) {
        prefs.edit().putInt(KEY_SISA_KUOTA, newKuota).apply()
    }

    // Hapus semua data saat logout
    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
