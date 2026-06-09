package com.example.winly.data

import com.google.gson.annotations.SerializedName

data class OrganizerResponse(
    val status: String,
    val data: List<OrganizerData>
)

data class OrganizerData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("instansi") val instansi: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("ktp_url") val ktpUrl: String?,
    @SerializedName("legalitas_url") val legalitasUrl: String?,
    @SerializedName("tanggal") val tanggal: String?
)
