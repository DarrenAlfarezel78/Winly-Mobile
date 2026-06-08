package com.example.winly.data

import com.google.gson.annotations.SerializedName

data class OrganizerDetailResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: OrganizerDetailData?
)

data class OrganizerDetailData(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("instansi") val instansi: String,
    @SerializedName("created_at") val createdAt: String ,
    @SerializedName("ktp_url") val ktpUrl: String?,
    @SerializedName("legalitas_url") val legalitasUrl: String?
)