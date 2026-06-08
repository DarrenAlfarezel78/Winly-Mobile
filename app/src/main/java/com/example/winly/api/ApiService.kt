package com.example.winly.api

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*

// Model untuk UserInfo & UserResponse
data class UserResponse(
    @SerializedName("status")     val status: String? = null,
    @SerializedName("message")    val message: String? = null,
    @SerializedName("data")       val data: UserInfoData? = null
)

data class UserInfoData(
    @SerializedName("id")          val id: Int? = 0,
    @SerializedName("name")        val name: String? = null,
    @SerializedName("email")       val email: String? = null,
    @SerializedName("role")        val role: String? = null,
    @SerializedName("sisa_kuota")  val sisaKuota: Int? = 0,
    @SerializedName("instansi")    val instansi: String? = null,
    @SerializedName("phone")       val phone: String? = null,
    @SerializedName("avatar_url")  val avatarUrl: String? = null
)

// ============================================================
// API SERVICE - Semua endpoint Winly
// ============================================================
interface ApiService {

    // 1. LOGIN
    @FormUrlEncoded
    @POST("login.php")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    // 2. REGISTER
    @FormUrlEncoded
    @POST("register.php")
    fun registerUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("role") role: String,
        @Field("instansi") instansi: String = ""
    ): Call<LoginResponse>

    // 3. VERIFIKASI OTP
    @FormUrlEncoded
    @POST("verify.php")
    fun verifyUser(
        @Field("email") email: String,
        @Field("code") code: String
    ): Call<LoginResponse>

    // 4. AMBIL SEMUA LOMBA
    @GET("get_competitions.php")
    fun getCompetitions(
        @Query("search") search: String? = null,
        @Query("kategori") kategori: String? = null,
        @Query("tingkat_pendidikan") tingkatPendidikan: String? = null,
        @Query("tingkat_lomba") tingkatLomba: String? = null
    ): Call<CompetitionResponse>

    // 5. DETAIL SATU LOMBA (INI YANG SUDAH DIPERBAIKI)
    @GET("get_competition_detail.php")
    fun getCompetitionDetail(
        @Query("id") id: Int
    ): Call<CompetitionDetailResponse>

    // 6. LOMBA MILIK PENYELENGGARA
    @GET("get_my_competitions.php")
    fun getMyCompetitions(
        @Query("penyelenggara_id") id: Int
    ): Call<CompetitionResponse>

    // 7. BUAT LOMBA BARU (SUDAH ADA POSTER URL)
    @FormUrlEncoded
    @POST("create_competition.php")
    fun createCompetition(
        @Field("penyelenggara_id")   penyelenggaraId: Int,
        @Field("judul_lomba")        judul: String,
        @Field("kategori")           kategori: String,
        @Field("tingkat_pendidikan") tingkatPendidikan: String,
        @Field("tingkat_lomba")      tingkatLomba: String,
        @Field("deskripsi")          deskripsi: String,
        @Field("link_pendaftaran")   linkPendaftaran: String,
        @Field("link_panduan")       linkPanduan: String,
        @Field("tanggal_pelaksanaan") tanggal: String,
        @Field("tanggal_tutup_daftar") tanggalTutup: String,
        @Field("biaya_pendaftaran")  biaya: Int,
        @Field("poster_url")         posterUrl: String = "" // TAMBAHKAN INI
    ): Call<LoginResponse>

    // 8. HAPUS LOMBA
    @FormUrlEncoded
    @POST("delete_competition.php")
    fun deleteCompetition(
        @Field("competition_id")   competitionId: Int,
        @Field("penyelenggara_id") penyelenggaraId: Int
    ): Call<LoginResponse>

    // 9. INFO USER
    @GET("get_user_info.php")
    fun getUserInfo(
        @Query("user_id") userId: Int
    ): Call<UserResponse>

    // 10. BOOKMARK - GET
    @GET("bookmark.php")
    fun getBookmarks(
        @Query("user_id") userId: Int
    ): Call<BookmarkResponse>

    // 11. BOOKMARK - POST
    @FormUrlEncoded
    @POST("bookmark.php")
    fun toggleBookmark(
        @Field("user_id")        userId: Int,
        @Field("competition_id") competitionId: Int
    ): Call<BookmarkResponse>

    // 12. AMBIL SERTIFIKAT USER
    @GET("get_certificates.php")
    fun getCertificates(
        @Query("user_id") userId: Int
    ): Call<LoginResponse>

    // 13. FORGOT PASSWORD - Kirim OTP
    @FormUrlEncoded
    @POST("forgot_password.php")
    fun sendForgotOtp(
        @Field("action") action: String = "send_otp",
        @Field("email")  email: String
    ): Call<LoginResponse>

    // 14. FORGOT PASSWORD - Reset Password
    @FormUrlEncoded
    @POST("forgot_password.php")
    fun resetPassword(
        @Field("action")       action: String = "reset_password",
        @Field("email")        email: String,
        @Field("otp")          otp: String,
        @Field("new_password") newPassword: String
    ): Call<LoginResponse>

    // 15. GET REGISTRATIONS
    @GET("get_registrations.php")
    fun getRegistrations(
        @Query("competition_id") competitionId: Int
    ): Call<RegistrationResponse> // Pastikan file RegistrationResponse.kt sudah ada atau biarkan error kuning sementara jika belum dibuat

    // 16. UPDATE STATUS PENDAFTARAN
    @FormUrlEncoded
    @POST("update_registration.php")
    fun updateRegistration(
        @Field("registration_id") registrationId: Int,
        @Field("status")          status: String,
        @Field("catatan")         catatan: String = ""
    ): Call<LoginResponse>
}