package com.example.winly.data

data class OrganizerResponse(
    val status: String,
    val data: List<OrganizerData>
)

data class OrganizerData(
    val id: Int,
    val name: String,
    val instansi: String,
    val tanggal: String
)