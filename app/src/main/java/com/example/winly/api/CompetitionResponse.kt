package com.example.winly.api

import com.google.gson.annotations.SerializedName

// ============================================================
// COMPETITION RESPONSE - Update sesuai field database baru
// ============================================================
data class CompetitionResponse(
    @SerializedName("status")  val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("total")   val total: Int? = 0,
    @SerializedName("data")    val data: List<CompetitionModel>? = null
)

data class CompetitionModel(
    @SerializedName("id")                       val id: String? = "",
    @SerializedName("penyelenggara_id")         val penyelenggaraId: String? = "",
    @SerializedName("judul_lomba")              val judulLomba: String? = "",
    @SerializedName("kategori")                 val kategori: String? = "",
    @SerializedName("tingkat_pendidikan")       val tingkatPendidikan: String? = "",
    @SerializedName("tingkat_lomba")            val tingkatLomba: String? = "",
    @SerializedName("deskripsi")                val deskripsi: String? = "",
    @SerializedName("link_pendaftaran")         val linkPendaftaran: String? = "",
    @SerializedName("link_panduan")             val linkPanduan: String? = "",
    @SerializedName("poster_url")               val posterUrl: String? = null,
    @SerializedName("biaya_pendaftaran")        val biayaPendaftaran: String? = "0",
    @SerializedName("tanggal_buka_daftar")      val tanggalBukaDaftar: String? = "",
    @SerializedName("tanggal_tutup_daftar")     val tanggalTutupDaftar: String? = "",
    @SerializedName("tanggal_pelaksanaan")      val tanggalPelaksanaan: String? = "",
    @SerializedName("status")                   val statusLomba: String? = "",
    @SerializedName("is_verified")              val isVerified: Boolean? = false,
    @SerializedName("nama_penyelenggara")       val namaPenyelenggara: String? = "",
    @SerializedName("instansi_penyelenggara")   val instansiPenyelenggara: String? = ""
)

// Model khusus untuk detail lomba (lebih lengkap)
data class CompetitionDetailModel(
    @SerializedName("id")                       val id: String? = "",
    @SerializedName("penyelenggara_id")         val penyelenggaraId: String? = "",
    @SerializedName("judul_lomba")              val judulLomba: String? = "",
    @SerializedName("kategori")                 val kategori: String? = "",
    @SerializedName("tingkat_pendidikan")       val tingkatPendidikan: String? = "",
    @SerializedName("tingkat_lomba")            val tingkatLomba: String? = "",
    @SerializedName("deskripsi")                val deskripsi: String? = "",
    @SerializedName("link_pendaftaran")         val linkPendaftaran: String? = "",
    @SerializedName("link_panduan")             val linkPanduan: String? = "",
    @SerializedName("poster_url")               val posterUrl: String? = null,
    @SerializedName("biaya_pendaftaran")        val biayaPendaftaran: Int? = 0,
    @SerializedName("tanggal_buka_daftar")      val tanggalBukaDaftar: String? = "",
    @SerializedName("tanggal_tutup_daftar")     val tanggalTutupDaftar: String? = "",
    @SerializedName("tanggal_pelaksanaan")      val tanggalPelaksanaan: String? = "",
    @SerializedName("status")                   val statusLomba: String? = "",
    @SerializedName("is_verified")              val isVerified: Boolean? = false,
    @SerializedName("nama_penyelenggara")       val namaPenyelenggara: String? = "",
    @SerializedName("email_penyelenggara")      val emailPenyelenggara: String? = "",
    @SerializedName("instansi_penyelenggara")   val instansiPenyelenggara: String? = "",
    @SerializedName("phone_penyelenggara")      val phonePenyelenggara: String? = ""
)

data class CompetitionDetailResponse(
    @SerializedName("status")  val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data")    val data: CompetitionDetailModel? = null
)

// Model untuk bookmark
data class BookmarkResponse(
    @SerializedName("status")        val status: String? = null,
    @SerializedName("message")       val message: String? = null,
    @SerializedName("is_bookmarked") val isBookmarked: Boolean? = null,
    @SerializedName("total")         val total: Int? = 0,
    @SerializedName("data")          val data: List<CompetitionModel>? = null


)

// Model Registrasi
data class RegistrationModel(
    @SerializedName("id")                  val id: String? = "",
    @SerializedName("peserta_id")          val pesertaId: String? = "",
    @SerializedName("competition_id")      val competitionId: String? = "",
    @SerializedName("status_pendaftaran")  val statusPendaftaran: String? = "pending",
    @SerializedName("nama_peserta")        val namaPeserta: String? = "",
    @SerializedName("email_peserta")       val emailPeserta: String? = "",
    @SerializedName("instansi_peserta")    val instansiPeserta: String? = "",
    @SerializedName("catatan")             val catatan: String? = "",
    @SerializedName("created_at")          val createdAt: String? = ""
)

data class RegistrationResponse(
    @SerializedName("status")  val status: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("total")   val total: Int? = 0,
    @SerializedName("data")    val data: List<RegistrationModel>? = null
)