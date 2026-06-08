package com.example.winly.ui.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.winly.DeadlineScheduler
import com.example.winly.api.BookmarkResponse
import com.example.winly.api.CompetitionModel
import com.example.winly.api.LoginResponse
import com.example.winly.api.RetrofitClient
import com.example.winly.data.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun BookmarkScreen(onNavigateToDetail: (Int) -> Unit = {}) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.getUserId()

    var bookmarkList by remember { mutableStateOf<List<CompetitionModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<Int?>(null) }

    // Fetch bookmark
    fun loadBookmarks() {
        isLoading = true
        RetrofitClient.instance.getBookmarks(userId)
            .enqueue(object : Callback<BookmarkResponse> {
                override fun onResponse(call: Call<BookmarkResponse>, response: Response<BookmarkResponse>) {
                    isLoading = false
                    bookmarkList = response.body()?.data ?: emptyList()
                }
                override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {
                    isLoading = false
                    Toast.makeText(context, "Gagal memuat koleksi", Toast.LENGTH_SHORT).show()
                }
            })
    }

    LaunchedEffect(userId) { loadBookmarks() }

    // Dialog konfirmasi hapus bookmark
    if (showDeleteDialog && selectedId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Koleksi?", fontWeight = FontWeight.Bold) },
            text = { Text("Lomba ini akan dihapus dari koleksi kamu.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        RetrofitClient.instance.toggleBookmark(userId, selectedId!!)
                            .enqueue(object : Callback<BookmarkResponse> {
                                override fun onResponse(call: Call<BookmarkResponse>, response: Response<BookmarkResponse>) {
                                    if (response.body()?.status == "success") {
                                        // Batalkan notifikasi deadline saat bookmark dihapus
                                        DeadlineScheduler.cancelDeadlineNotification(
                                            context, selectedId!!
                                        )
                                        bookmarkList = bookmarkList.filter { it.id?.toIntOrNull() != selectedId }
                                        Toast.makeText(context, "Dihapus dari koleksi", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {}
                            })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") } }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {

        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                Text("KOLEKSI SAYA", fontSize = 11.sp, color = Color(0xFF0061D1), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text("Lomba Tersimpan", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                if (bookmarkList.isNotEmpty()) {
                    Text("${bookmarkList.size} lomba disimpan", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        // Konten
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0061D1))
                }
            }
            bookmarkList.isEmpty() -> {
                // Empty state
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.size(100.dp).clip(CircleShape).background(Color(0xFFE5F0FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.BookmarkBorder, null, tint = Color(0xFF0061D1), modifier = Modifier.size(48.dp))
                        }
                        Text("Belum Ada Koleksi", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Simpan lomba yang menarik dengan\ntombol bookmark di halaman detail",
                            fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
                ) {
                    items(bookmarkList) { lomba ->
                        BookmarkCard(
                            lomba = lomba,
                            onClick = { onNavigateToDetail(lomba.id?.toIntOrNull() ?: 0) },
                            onHapus = {
                                selectedId = lomba.id?.toIntOrNull()
                                showDeleteDialog = true
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
fun BookmarkCard(
    lomba: CompetitionModel,
    onClick: () -> Unit = {},
    onHapus: () -> Unit = {}
) {
    val biaya = lomba.biayaPendaftaran?.toIntOrNull() ?: 0
    val sisaHari = hitungSisaHari(lomba.tanggalPelaksanaan)
    val isDeadlineMepet = sisaHari.contains("Sisa") &&
            sisaHari.replace("Sisa ", "").replace(" hari lagi", "").toIntOrNull()?.let { it <= 7 } == true

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        border = if (isDeadlineMepet) BorderStroke(1.5.dp, Color(0xFFF97316)) else null
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            // Ikon kategori
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFE5F0FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFF0061D1), modifier = Modifier.size(28.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Badge kategori
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(color = Color(0xFFE5F0FF), shape = RoundedCornerShape(50.dp)) {
                        Text(lomba.kategori ?: "Umum", color = Color(0xFF0061D1), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                    }
                    if (lomba.tingkatLomba?.isNotEmpty() == true) {
                        Surface(color = Color(0xFFF3E8FF), shape = RoundedCornerShape(50.dp)) {
                            Text(lomba.tingkatLomba ?: "", color = Color(0xFF7C3AED), fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(lomba.judulLomba ?: "Tanpa Judul", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2)

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Sisa hari
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer, null,
                            tint = if (isDeadlineMepet) Color(0xFFF97316) else Color(0xFF0061D1),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            sisaHari,
                            fontSize = 11.sp,
                            color = if (isDeadlineMepet) Color(0xFFF97316) else Color(0xFF0061D1),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Biaya
                    Text(
                        if (biaya == 0) "GRATIS" else "Rp $biaya",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (biaya == 0) Color(0xFF22C55E) else Color.DarkGray
                    )
                }

                // Warning deadline mepet
                if (isDeadlineMepet) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(color = Color(0xFFFFF7ED), shape = RoundedCornerShape(8.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFF97316), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Deadline segera!", fontSize = 10.sp, color = Color(0xFFF97316), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tombol hapus bookmark
            IconButton(onClick = onHapus) {
                Icon(Icons.Default.Bookmark, null, tint = Color(0xFF0061D1), modifier = Modifier.size(22.dp))
            }
        }
    }
}