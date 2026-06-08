package com.example.winly.data

import com.google.gson.annotations.SerializedName

data class AdminStatsResponse(
    val status: String,
    val data: AdminStatsData?
)

data class AdminStatsData(
    @SerializedName("total_peserta")
    val totalPeserta: Int,

    @SerializedName("total_kompetisi")
    val totalKompetisi: Int,

    @SerializedName("total_penyelenggara")
    val totalPenyelenggara: Int,

    @SerializedName("pending_verifikasi")
    val pendingVerifikasi: Int
)