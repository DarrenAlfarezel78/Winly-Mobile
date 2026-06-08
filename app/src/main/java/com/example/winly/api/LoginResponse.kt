package com.example.winly.api

import com.google.gson.annotations.SerializedName

// ============================================================
// LOGIN RESPONSE - Perbaikan: tambah field id & data lengkap
// ============================================================
data class LoginResponse(
    @SerializedName("status")  val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data")    val data: UserData? = null
)

data class UserData(
    @SerializedName("id")          val id: Int? = 0,        // PENTING: fix hardcode userId
    @SerializedName("name")        val name: String? = null,
    @SerializedName("email")       val email: String? = null,
    @SerializedName("role")        val role: String? = null,
    @SerializedName("sisa_kuota")  val sisaKuota: Int? = 0,
    @SerializedName("instansi")    val instansi: String? = null,
    @SerializedName("phone")       val phone: String? = null,
    @SerializedName("avatar_url")  val avatarUrl: String? = null
)
