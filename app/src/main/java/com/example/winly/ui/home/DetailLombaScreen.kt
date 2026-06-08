package com.example.winly.ui.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.winly.DeadlineScheduler
import com.example.winly.api.CompetitionDetailModel
import com.example.winly.api.CompetitionDetailResponse
import com.example.winly.api.BookmarkResponse
import com.example.winly.api.RetrofitClient
import com.example.winly.data.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailLombaScreen(
    competitionId: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.getUserId()

    var lomba by remember { mutableStateOf<CompetitionDetailModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isBookmarked by remember { mutableStateOf(false) }
    var isBookmarkLoading by remember { mutableStateOf(false) }

    // Fetch detail lomba
    LaunchedEffect(competitionId) {
        RetrofitClient.instance.getCompetitionDetail(competitionId)
            .enqueue(object : Callback<CompetitionDetailResponse> {
                override fun onResponse(call: Call<CompetitionDetailResponse>, response: Response<CompetitionDetailResponse>) {
                    isLoading = false
                    if (response.isSuccessful && response.body()?.status == "success") {
                        lomba = response.body()?.data
                    }
                }
                override fun onFailure(call: Call<CompetitionDetailResponse>, t: Throwable) {
                    isLoading = false
                    Toast.makeText(context, "Gagal memuat detail lomba", Toast.LENGTH_SHORT).show()
                }
            })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detail Lomba", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Tombol Bookmark di header
                    IconButton(
                        onClick = {
                            if (!isBookmarkLoading && userId > 0) {
                                isBookmarkLoading = true
                                RetrofitClient.instance.toggleBookmark(userId, competitionId)
                                    .enqueue(object : Callback<BookmarkResponse> {
                                        override fun onResponse(call: Call<BookmarkResponse>, response: Response<BookmarkResponse>) {
                                            isBookmarkLoading = false
                                            val body = response.body()
                                            if (body?.status == "success") {
                                                isBookmarked = body.isBookmarked ?: false
                                                val msg = if (isBookmarked) "Lomba disimpan ke koleksi!" else "Bookmark dihapus"
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

                                                // Jadwalkan/batalkan notifikasi
                                                if (isBookmarked) {
                                                    DeadlineScheduler.scheduleDeadlineNotification(
                                                        context         = context,
                                                        competitionId   = competitionId,
                                                        judulLomba      = lomba?.judulLomba ?: "",
                                                        tanggalDeadline = lomba?.tanggalTutupDaftar ?: lomba?.tanggalPelaksanaan
                                                    )
                                                } else {
                                                    DeadlineScheduler.cancelDeadlineNotification(context, competitionId)
                                                }
                                            }
                                        }
                                        override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                                            isBookmarkLoading = false
                                        }
                                    })
                            }
                        }
                    ) {
                        Icon(
                            if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (isBookmarked) Color(0xFF0061D1) else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0061D1))
            }
            return@Scaffold
        }

        if (lomba == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Lomba tidak ditemukan", color = Color.Gray)
            }
            return@Scaffold
        }

        val data = lomba!!
        val biaya = data.biayaPendaftaran ?: 0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Banner poster
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF0061D1)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color.White.copy(0.3f), modifier = Modifier.size(80.dp))
                    if (data.isVerified == true) {
                        Surface(color = Color(0xFF22C55E), shape = RoundedCornerShape(50.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("TERVERIFIKASI", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                // Judul & kategori
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ChipLabel(data.tingkatLomba ?: "Nasional", Color(0xFF0061D1))
                            ChipLabel(data.tingkatPendidikan ?: "Umum", Color(0xFF7C3AED))
                            if (biaya == 0) ChipLabel("GRATIS", Color(0xFF22C55E))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(data.judulLomba ?: "Tanpa Judul", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text(data.kategori ?: "", color = Color(0xFF0061D1), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Info penting
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Informasi Lomba", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        InfoRow(Icons.Default.DateRange,     "Pelaksanaan",    data.tanggalPelaksanaan ?: "-")
                        InfoRow(Icons.Default.EventBusy,     "Tutup Daftar",   data.tanggalTutupDaftar ?: "-")
                        InfoRow(Icons.Default.AttachMoney,   "Biaya Daftar",   if (biaya == 0) "GRATIS" else "Rp ${biaya}")
                        InfoRow(Icons.Default.School,        "Jenjang",        data.tingkatPendidikan ?: "-")
                        InfoRow(Icons.Default.LocationOn,    "Tingkat",        data.tingkatLomba ?: "-")
                        InfoRow(Icons.Default.Timer,         "Sisa Waktu",     hitungSisaHari(data.tanggalTutupDaftar ?: data.tanggalPelaksanaan))
                    }
                }

                // Penyelenggara
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Penyelenggara", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        InfoRow(Icons.Default.Person,        "Nama",     data.namaPenyelenggara ?: "-")
                        InfoRow(Icons.Default.Business,      "Instansi", data.instansiPenyelenggara ?: "-")
                        InfoRow(Icons.Default.Email,         "Email",    data.emailPenyelenggara ?: "-")
                    }
                }

                // Deskripsi
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Deskripsi", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(data.deskripsi ?: "Tidak ada deskripsi", color = Color.DarkGray, lineHeight = 22.sp)
                    }
                }

                // Tombol aksi
                if (!data.linkPendaftaran.isNullOrEmpty()) {
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(data.linkPendaftaran))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061D1))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Daftar Sekarang", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // Tombol share
                OutlinedButton(
                    onClick = {
                        val shareText = "Cek lomba ini di Winly!\n\n${data.judulLomba}\nKategori: ${data.kategori}\nTanggal: ${data.tanggalPelaksanaan}\n\nDownload Winly sekarang!"
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Bagikan lomba via"))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Bagikan Lomba Ini", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun ChipLabel(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(50.dp)) {
        Text(text, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color(0xFF0061D1), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
