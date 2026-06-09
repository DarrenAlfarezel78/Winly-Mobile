package com.example.winly.ui.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.winly.api.BookmarkResponse
import com.example.winly.api.CompetitionModel
import com.example.winly.api.CompetitionResponse
import com.example.winly.api.LoginResponse
import com.example.winly.api.RetrofitClient
import com.example.winly.api.UserResponse
import com.example.winly.data.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileOutputStream
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

fun hitungSisaHari(tanggalLomba: String?): String {
    if (tanggalLomba.isNullOrEmpty()) return "Tanggal segera hadir"
    return try {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val targetDate = format.parse(tanggalLomba)
        val diff = targetDate?.time?.minus(java.util.Calendar.getInstance().timeInMillis) ?: 0L
        val days = diff / (24 * 60 * 60 * 1000)
        when {
            days > 0   -> "Sisa $days hari lagi"
            days == 0L -> "Hari ini!"
            else       -> "Pendaftaran Ditutup"
        }
    } catch (_: Exception) { "Tanggal segera hadir" }
}

val listKategori = listOf("Teknologi & IT", "Sains & Matematika", "Ekonomi & Bisnis", "Karya Tulis & Riset", "Seni & Desain", "Soshum & Hukum")
val listPendidikan = listOf("SD", "SMP", "SMA/SMK", "Mahasiswa", "Umum")
val listTingkat = listOf("Kota", "Provinsi", "Nasional", "Internasional")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    userRole: String = "peserta",
    onNavigateToCreate: () -> Unit = {},
    onNavigateToDetail: (Int) -> Unit = {},
    onNavigateToKelola: (Int, String) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isFilterOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var selectedKategori by remember { mutableStateOf("") }
    var selectedPendidikan by remember { mutableStateOf("") }
    var selectedTingkat by remember { mutableStateOf("") }
    var tempKategori by remember { mutableStateOf("") }
    var tempPendidikan by remember { mutableStateOf("") }
    var tempTingkat by remember { mutableStateOf("") }

    val activeFilterCount = listOf(selectedKategori, selectedPendidikan, selectedTingkat).count { it.isNotEmpty() }

    Scaffold(containerColor = Color.White) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (selectedTab) {
                0 -> if (userRole == "penyelenggara") {
                    PenyelenggaraDashboard(
                        onNavigateToCreate = onNavigateToCreate,
                        onNavigateToKelola = onNavigateToKelola
                    )
                } else {
                    PesertaDashboard(
                        onOpenFilter = {isFilterOpen = true },
                        onNavigateToDetail = { selectedTab = 2 },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        activeKategori = selectedKategori,
                        activePendidikan = selectedPendidikan,
                        activeTingkat = selectedTingkat,
                        activeFilterCount = activeFilterCount
                    )
                }
                1 -> ExploreScreen(onNavigateToDetail = onNavigateToDetail)
                2 -> BookmarkScreen(onNavigateToDetail = onNavigateToDetail)
                3 -> ProfileTab(onLogout = onLogout)
            }
            CustomBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        if (isFilterOpen) {
            ModalBottomSheet(
                onDismissRequest = { isFilterOpen = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = Color.White,
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.LightGray) }
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Filter Lomba", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        TextButton(onClick = { tempKategori = ""; tempPendidikan = ""; tempTingkat = "" }) {
                            Text("Reset Semua", color = Color(0xFF0061D1), fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    FilterSection(title = "Kategori", options = listKategori, selected = tempKategori, onSelect = { tempKategori = if (tempKategori == it) "" else it })
                    Spacer(modifier = Modifier.height(20.dp))
                    FilterSection(title = "Jenjang Pendidikan", options = listPendidikan, selected = tempPendidikan, onSelect = { tempPendidikan = if (tempPendidikan == it) "" else it })
                    Spacer(modifier = Modifier.height(20.dp))
                    FilterSection(title = "Tingkat Lomba", options = listTingkat, selected = tempTingkat, onSelect = { tempTingkat = if (tempTingkat == it) "" else it })
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = { selectedKategori = tempKategori; selectedPendidikan = tempPendidikan; selectedTingkat = tempTingkat; isFilterOpen = false },
                        modifier = Modifier.fillMaxWidth().height(56.dp).shadow(4.dp, RoundedCornerShape(50.dp)),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061D1))
                    ) {
                        Icon(Icons.Default.FilterList, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        val aktif = listOf(tempKategori, tempPendidikan, tempTingkat).count { it.isNotEmpty() }
                        Text(if (aktif > 0) "Terapkan $aktif Filter" else "Terapkan Filter", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSection(title: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.DarkGray)
        Spacer(modifier = Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                val isSelected = selected == option
                Surface(modifier = Modifier.clickable { onSelect(option) }, shape = RoundedCornerShape(50.dp), color = if (isSelected) Color(0xFF0061D1) else Color(0xFFF2F2F2), border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE0E0E0))) {
                    Text(option, modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Color.White else Color.DarkGray)
                }
            }
        }
    }
}

@Composable
fun ActiveFilterChip(label: String, onRemove: () -> Unit) {
    Surface(shape = RoundedCornerShape(50.dp), color = Color(0xFFE5F0FF), border = BorderStroke(1.dp, Color(0xFF0061D1))) {
        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontSize = 11.sp, color = Color(0xFF0061D1), fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.Close, null, tint = Color(0xFF0061D1), modifier = Modifier.size(14.dp).clickable { onRemove() })
        }
    }
}

@Composable
fun ProfileTab(onLogout: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.getUserId()
    var showLogoutDialog by remember { mutableStateOf(false) }

    // State melacak file mana yang sedang diupload
    var uploadTargetType by remember { mutableStateOf("") } // "ktp" atau "legalitas"
    var isUploading by remember { mutableStateOf(false) }

    // Launcher Pemilih Dokumen (Mendukung Gambar & PDF)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = getFileFromUri(context, it)
            if (file != null) {
                isUploading = true

                // Konversi data menjadi Multipart data body
                val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val userIdBody = userId.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val typeBody = uploadTargetType.toRequestBody("text/plain".toMediaTypeOrNull())

                RetrofitClient.instance.uploadDocument(userIdBody, typeBody, filePart)
                    .enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            isUploading = false
                            if (response.body()?.status == "success") {
                                Toast.makeText(context, "Berhasil mengunggah dokumen $uploadTargetType!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Gagal: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            isUploading = false
                            Toast.makeText(context, "Koneksi gagal: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF0061D1)), contentAlignment = Alignment.Center) {
            Text(sessionManager.getName().firstOrNull()?.uppercaseChar()?.toString() ?: "U", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(sessionManager.getName(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(sessionManager.getEmail(), fontSize = 13.sp, color = Color.Gray)
        Text(sessionManager.getRole().uppercase(), fontSize = 11.sp, color = Color(0xFF0061D1), fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA))) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileInfoRow(Icons.Default.Person, "Nama", sessionManager.getName())
                ProfileInfoRow(Icons.Default.Email, "Email", sessionManager.getEmail())
                if (sessionManager.getInstansi().isNotEmpty()) ProfileInfoRow(Icons.Default.School, "Instansi", sessionManager.getInstansi())
                if (sessionManager.getRole() == "penyelenggara") ProfileInfoRow(Icons.Default.CardGiftcard, "Sisa Kuota", "${sessionManager.getSisaKuota()} lomba gratis")
            }
        }

        // 👇 SEKSI BERKAS VERIFIKASI (HANYA MUNCUL UNTUK PENYELENGGARA)
        if (sessionManager.getRole() == "penyelenggara") {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Dokumen Verifikasi Akun", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Text("Unggah berkas resmi untuk divalidasi oleh Tim Admin.", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isUploading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = Color(0xFF0061D1))
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    uploadTargetType = "ktp"
                                    filePickerLauncher.launch("image/*") // Mengutamakan gambar kamera untuk KTP
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.UploadFile, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Upload KTP", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    uploadTargetType = "legalitas"
                                    filePickerLauncher.launch("*/*") // Mendukung Gambar & Dokumen PDF berkas legalitas
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Legalitas (PDF/Img)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(onClick = { showLogoutDialog = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, Color(0xFFDC2626))) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color(0xFFDC2626))
            Spacer(Modifier.width(8.dp))
            Text("Logout", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Konfirmasi Logout", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah kamu yakin ingin keluar?") },
            confirmButton = { Button(onClick = { showLogoutDialog = false; onLogout() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))) { Text("Logout", fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") } }
        )
    }
}

@Composable
fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color(0xFF0061D1), modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PenyelenggaraDashboard(
    onNavigateToCreate: () -> Unit,
    onNavigateToKelola: (Int, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.getUserId()
    val namaUser = sessionManager.getName()

    var sisaKuota by remember { mutableIntStateOf(sessionManager.getSisaKuota()) }
    var myLombaList by remember { mutableStateOf<List<CompetitionModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedLombaId by remember { mutableStateOf<Int?>(null) }
    var isVerified by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        if (userId > 0) {
            RetrofitClient.instance.getUserInfo(userId).enqueue(object : Callback<UserResponse> {
                override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                    if (response.body()?.status == "success") {
                        sisaKuota = response.body()?.data?.sisaKuota ?: 0
                        sessionManager.updateSisaKuota(sisaKuota)
                        isVerified = response.body()?.data?.isOrganizerVerified == 1
                    }
                }
                override fun onFailure(call: Call<UserResponse>, t: Throwable) {}
            })
            RetrofitClient.instance.getMyCompetitions(userId).enqueue(object : Callback<CompetitionResponse> {
                override fun onResponse(call: Call<CompetitionResponse>, response: Response<CompetitionResponse>) {
                    isLoading = false
                    myLombaList = response.body()?.data ?: emptyList()
                }
                override fun onFailure(call: Call<CompetitionResponse>, t: Throwable) { isLoading = false }
            })
        }
    }

    if (showDeleteDialog && selectedLombaId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Lomba?", fontWeight = FontWeight.Bold) },
            text = { Text("Lomba yang dihapus tidak bisa dikembalikan.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        RetrofitClient.instance.deleteCompetition(selectedLombaId!!, userId)
                            .enqueue(object : Callback<LoginResponse> {
                                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                    if (response.body()?.status == "success") {
                                        myLombaList = myLombaList.filter { it.id?.toIntOrNull() != selectedLombaId }
                                        Toast.makeText(context, "Lomba berhasil dihapus!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
                            })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) { Text("Hapus") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Batal") } }
        )
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("WINLY", color = Color(0xFF0061D1), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE5F0FF)), contentAlignment = Alignment.Center) {
                    Text(namaUser.firstOrNull()?.uppercaseChar()?.toString() ?: "U", color = Color(0xFF0061D1), fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Surface(modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(24.dp)), shape = RoundedCornerShape(24.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFEEEEEE))) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Halo, $namaUser!", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3A8A))
                    Text("Kelola lomba kamu dari sini.", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(color = Color(0xFFE5F0FF), shape = RoundedCornerShape(12.dp)) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CardGiftcard, null, tint = Color(0xFF0061D1), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("SISA KUOTA GRATIS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("$sisaKuota lomba", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0061D1))
                            }
                        }
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(32.dp))

            // 👇 TAMPILKAN BANNER ORANYE JIKA BELUM TERVERIFIKASI
            if (!isVerified) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFFFCC80))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Akun Belum Diverifikasi", fontWeight = FontWeight.Bold, color = Color(0xFFD97706), fontSize = 14.sp)
                            Text("Silakan ke menu Profile untuk mengunggah KTP dan Dokumen Legalitas agar dapat menambah lomba.", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Kompetisi Anda", fontSize = 18.sp, fontWeight = FontWeight.Bold)

                // 👇 TOMBOL + TAMBAH HANYA MUNCUL JIKA SUDAH TERVERIFIKASI
                if (isVerified) {
                    Button(onClick = onNavigateToCreate, shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061D1)), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), modifier = Modifier.height(36.dp)) {
                        Text("+ Tambah", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        if (isLoading) {
            item { Box(Modifier.fillMaxWidth().height(100.dp), Alignment.Center) { CircularProgressIndicator(color = Color(0xFF0061D1)) } }
        } else if (myLombaList.isEmpty()) {
            item { Text("Belum ada lomba. Tambahkan lomba baru!", color = Color.Gray, modifier = Modifier.fillMaxWidth().padding(top = 20.dp), textAlign = TextAlign.Center) }
        } else {
            items(myLombaList) { lomba ->
                val biaya = lomba.biayaPendaftaran?.toIntOrNull() ?: 0
                PenyelenggaraCompetitionCard(
                    title = lomba.judulLomba ?: "Tanpa Judul",
                    price = if (biaya == 0) "FREE" else "Rp $biaya",
                    tanggalLomba = lomba.tanggalPelaksanaan ?: "",
                    onKelola = { onNavigateToKelola(lomba.id?.toIntOrNull() ?: 0, lomba.judulLomba ?: "") },
                    onHapus = { selectedLombaId = lomba.id?.toIntOrNull(); showDeleteDialog = true }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun PenyelenggaraCompetitionCard(
    title: String,
    price: String,
    tanggalLomba: String,
    onKelola: () -> Unit = {},
    onHapus: () -> Unit = {}
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFEEEEEE)), elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color(0xFFF2F2F2)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(40.dp))
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = Color(0xFF0061D1), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(hitungSisaHari(tanggalLomba), color = Color(0xFF0061D1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(price, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Kelola Lomba →", color = Color(0xFF0061D1), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onKelola() })
                Text("Hapus Lomba", color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onHapus() })
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PesertaDashboard(
    onOpenFilter: () -> Unit,
    onNavigateToDetail: (Int) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    activeKategori: String = "",
    activePendidikan: String = "",
    activeTingkat: String = "",
    activeFilterCount: Int = 0
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val namaUser = sessionManager.getName()

    var listLomba by remember { mutableStateOf<List<CompetitionModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(searchQuery, activeKategori, activePendidikan, activeTingkat) {
        isLoading = true
        RetrofitClient.instance.getCompetitions(
            search            = searchQuery.ifEmpty { null },
            kategori          = activeKategori.ifEmpty { null },
            tingkatPendidikan = activePendidikan.ifEmpty { null },
            tingkatLomba      = activeTingkat.ifEmpty { null }
        ).enqueue(object : Callback<CompetitionResponse> {
            override fun onResponse(call: Call<CompetitionResponse>, response: Response<CompetitionResponse>) {
                isLoading = false
                listLomba = response.body()?.data ?: emptyList()
            }
            override fun onFailure(call: Call<CompetitionResponse>, t: Throwable) { isLoading = false }
        })
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(45.dp).clip(CircleShape).background(Color(0xFF0061D1)), contentAlignment = Alignment.Center) {
                        Text(namaUser.firstOrNull()?.uppercaseChar()?.toString() ?: "U", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("WELCOME BACK", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("Hi, $namaUser! 👋", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                IconButton(onClick = {}) { Icon(Icons.Outlined.Notifications, null, tint = Color(0xFF0061D1)) }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = searchQuery, onValueChange = onSearchQueryChange,
                    placeholder = { Text("Cari lomba...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { onSearchQueryChange("") }) { Icon(Icons.Default.Clear, null) } },
                    modifier = Modifier.weight(1f).height(56.dp).clip(RoundedCornerShape(16.dp)),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF2F2F2), unfocusedContainerColor = Color(0xFFF2F2F2), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(contentAlignment = Alignment.TopEnd) {
                    Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(if (activeFilterCount > 0) Color(0xFF0061D1) else Color(0xFFC2D9FF)).clickable { onOpenFilter() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Tune, null, tint = if (activeFilterCount > 0) Color.White else Color(0xFF0061D1))
                    }
                    if (activeFilterCount > 0) {
                        Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(Color(0xFFDC2626)), contentAlignment = Alignment.Center) {
                            Text("$activeFilterCount", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        if (activeFilterCount > 0) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (activeKategori.isNotEmpty()) ActiveFilterChip(label = activeKategori, onRemove = {})
                    if (activePendidikan.isNotEmpty()) ActiveFilterChip(label = activePendidikan, onRemove = {})
                    if (activeTingkat.isNotEmpty()) ActiveFilterChip(label = activeTingkat, onRemove = {})
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(32.dp)).background(Brush.linearGradient(listOf(Color(0xFF0061D1), Color(0xFF2196F3)))).padding(24.dp)) {
                Column {
                    Text("SELAMAT DATANG", color = Color.White.copy(0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("Temukan\nLomba Terbaik!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 30.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(color = Color.White.copy(0.2f), shape = RoundedCornerShape(20.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${listLomba.size} lomba tersedia", color = Color.White, fontSize = 11.sp)
                        }
                    }
                }
                Box(modifier = Modifier.align(Alignment.TopEnd).size(60.dp).clip(CircleShape).background(Color.White.copy(0.2f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFFFD700), modifier = Modifier.size(32.dp))
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(when { searchQuery.isNotEmpty() -> "Hasil Pencarian"; activeFilterCount > 0 -> "Hasil Filter"; else -> "Lomba Terbaru" }, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                if (searchQuery.isNotEmpty() || activeFilterCount > 0) Text("${listLomba.size} hasil", color = Color(0xFF0061D1), fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                Box(Modifier.fillMaxWidth().height(140.dp), Alignment.Center) { CircularProgressIndicator(color = Color(0xFF0061D1)) }
            } else if (listLomba.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(140.dp), Alignment.Center) { Text("Tidak ada lomba yang ditemukan.", color = Color.Gray) }
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(listLomba) { lomba ->
                        RecommendationCard(lomba = lomba, onClick = { onNavigateToDetail(lomba.id?.toIntOrNull() ?: 0) })
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(100.dp)) }
    }
}

@Composable
fun RecommendationCard(lomba: CompetitionModel, onClick: () -> Unit = {}) {
    Card(modifier = Modifier.width(260.dp).clickable { onClick() }, shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(Color(0xFFF2F2F2)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(48.dp))
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(lomba.kategori?.uppercase() ?: "UMUM", color = Color(0xFF0061D1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(lomba.judulLomba ?: "Tanpa Judul", fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp), maxLines = 2, minLines = 2)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, null, tint = Color(0xFF0061D1), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(hitungSisaHari(lomba.tanggalPelaksanaan), color = Color(0xFF0061D1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                val biaya = lomba.biayaPendaftaran?.toIntOrNull() ?: 0
                Text(if (biaya == 0) "GRATIS" else "Rp $biaya", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = if (biaya == 0) Color(0xFF22C55E) else Color.Black)
            }
        }
    }
}

@Composable
fun ExploreScreen(onNavigateToDetail: (Int) -> Unit = {}) {
    val kategoriList = listOf(
        Triple("Teknologi & IT",      Icons.Default.Computer,   Color(0xFF0061D1)),
        Triple("Sains & Matematika",  Icons.Default.Science,    Color(0xFF7C3AED)),
        Triple("Ekonomi & Bisnis",    Icons.AutoMirrored.Filled.TrendingUp, Color(0xFF059669)),
        Triple("Karya Tulis & Riset", Icons.AutoMirrored.Filled.MenuBook,   Color(0xFFD97706)),
        Triple("Seni & Desain",       Icons.Default.Palette,    Color(0xFFDB2777)),
        Triple("Soshum & Hukum",      Icons.Default.Gavel,      Color(0xFF0891B2)),
    )

    var selectedKategori by remember { mutableStateOf<String?>(null) }
    var listLomba by remember { mutableStateOf<List<CompetitionModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(selectedKategori) {
        if (selectedKategori != null) {
            isLoading = true
            RetrofitClient.instance.getCompetitions(kategori = selectedKategori)
                .enqueue(object : Callback<CompetitionResponse> {
                    override fun onResponse(call: Call<CompetitionResponse>, response: Response<CompetitionResponse>) {
                        isLoading = false
                        listLomba = response.body()?.data ?: emptyList()
                    }
                    override fun onFailure(call: Call<CompetitionResponse>, t: Throwable) { isLoading = false }
                })
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp), contentPadding = PaddingValues(bottom = 100.dp)) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text("JELAJAHI", fontSize = 11.sp, color = Color(0xFF0061D1), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text("Explore Lomba", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text("Temukan lomba sesuai bidangmu", fontSize = 13.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(20.dp))
        }
        item {
            Text("Kategori", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(12.dp))
            val rows = kategoriList.chunked(2)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { (nama, icon, color) ->
                            val isSelected = selectedKategori == nama
                            Card(
                                modifier = Modifier.weight(1f).height(90.dp).clickable { selectedKategori = if (isSelected) null else nama; if (isSelected) listLomba = emptyList() },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) color else color.copy(alpha = 0.1f)),
                                elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 0.dp)
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.Center) {
                                    Icon(icon, null, tint = if (isSelected) Color.White else color, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(nama, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else color, lineHeight = 14.sp)
                                }
                            }
                        }
                        if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
                // Widget Deadline Mendekat
                Spacer(modifier = Modifier.height(16.dp))
                DeadlineWidget(onNavigateToPortfolio = { onNavigateToDetail(2) }) // Anggap tab 2 adalah Portfolio

            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        if (selectedKategori != null) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Lomba $selectedKategori", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    if (!isLoading) Text("${listLomba.size} lomba", color = Color(0xFF0061D1), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (isLoading) {
                item { Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) { CircularProgressIndicator(color = Color(0xFF0061D1)) } }
            } else if (listLomba.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(120.dp), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Belum ada lomba di kategori ini", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(listLomba) { lomba ->
                    val biaya = lomba.biayaPendaftaran?.toIntOrNull() ?: 0
                    val warna = kategoriList.find { it.first == lomba.kategori }?.third ?: Color(0xFF0061D1)
                    val ikon = kategoriList.find { it.first == lomba.kategori }?.second ?: Icons.Default.EmojiEvents
                    Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToDetail(lomba.id?.toIntOrNull() ?: 0) }.padding(bottom = 12.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(warna.copy(0.1f)), contentAlignment = Alignment.Center) {
                                Icon(ikon, null, tint = warna, modifier = Modifier.size(28.dp))
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(lomba.judulLomba ?: "", fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(lomba.tingkatLomba ?: "", fontSize = 10.sp, color = Color(0xFF0061D1), fontWeight = FontWeight.Bold)
                                    Text("•", fontSize = 10.sp, color = Color.Gray)
                                    Text(hitungSisaHari(lomba.tanggalPelaksanaan), fontSize = 10.sp, color = Color(0xFF0061D1), fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(if (biaya == 0) "GRATIS" else "Rp $biaya", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (biaya == 0) Color(0xFF22C55E) else Color.DarkGray)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                        }
                    }
                }
            }
        } else {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TouchApp, null, tint = Color.LightGray, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pilih kategori di atas\nuntuk melihat lomba", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}



@Composable
fun CustomBottomNav(selectedTab: Int, onTabSelected: (Int) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.padding(horizontal = 24.dp, vertical = 24.dp).fillMaxWidth().height(80.dp), shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp), color = Color.White.copy(0.85f)) {
        Row(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
            CustomNavItem(Icons.Default.Home, "HOME", selectedTab == 0) { onTabSelected(0) }
            CustomNavItem(Icons.Outlined.Explore, "EXPLORE", selectedTab == 1) { onTabSelected(1) }
            CustomNavItem(Icons.Outlined.Stars, "PORTFOLIO", selectedTab == 2) { onTabSelected(2) }
            CustomNavItem(Icons.Outlined.Person, "PROFILE", selectedTab == 3) { onTabSelected(3) }
        }
    }
}

@Composable
fun CustomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) Color(0xFF0061D1) else Color.Gray
    Column(modifier = Modifier.clickable(onClick = onClick, indication = null, interactionSource = remember { MutableInteractionSource() }).padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(if (isSelected) Color(0xFF0061D1).copy(0.1f) else Color.Transparent).padding(horizontal = 16.dp, vertical = 8.dp)) {
            Icon(icon, label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = color, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
fun DeadlineWidget(onNavigateToPortfolio: () -> Unit = {}) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val userId = sessionManager.getUserId()

    var deadlineLomba by remember { mutableStateOf<List<CompetitionModel>>(emptyList()) }

    LaunchedEffect(userId) {
        if (userId > 0) {
            RetrofitClient.instance.getBookmarks(userId)
                .enqueue(object : Callback<BookmarkResponse> {
                    override fun onResponse(
                        call: Call<BookmarkResponse>,
                        response: Response<BookmarkResponse>
                    ) {
                        val allBookmarks = response.body()?.data ?: emptyList()
                        // Filter yang deadlinenya <= 7 hari
                        deadlineLomba = allBookmarks.filter { lomba ->
                            val tanggal = lomba.tanggalTutupDaftar ?: lomba.tanggalPelaksanaan
                            if (tanggal.isNullOrEmpty()) return@filter false
                            try {
                                val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                val deadline = format.parse(tanggal)
                                val diff = deadline?.time?.minus(java.util.Calendar.getInstance().timeInMillis) ?: -1L
                                val days = diff / (24 * 60 * 60 * 1000)
                                days in 0..7
                            } catch (_: Exception) { false }
                        }
                    }
                    override fun onFailure(call: Call<BookmarkResponse>, t: Throwable) {}
                })
        }
    }

    if (deadlineLomba.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onNavigateToPortfolio() },
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFFFFF7ED),
        border = BorderStroke(1.dp, Color(0xFFFED7AA))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFF97316), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deadline Mendekat!", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFFEA580C))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Lihat Semua", fontSize = 11.sp, color = Color(0xFFF97316), fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color(0xFFF97316), modifier = Modifier.size(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            deadlineLomba.take(2).forEach { lomba ->
                val tanggal = lomba.tanggalTutupDaftar ?: lomba.tanggalPelaksanaan
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF97316))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(lomba.judulLomba ?: "", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(hitungSisaHari(tanggal), fontSize = 10.sp, color = Color(0xFFF97316), fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (deadlineLomba.size > 2) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("+${deadlineLomba.size - 2} lomba lainnya", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(start = 18.dp))
            }
        }
    }
}
fun getFileFromUri(context: Context, uri: Uri): File? {
    return try {
        val contentResolver = context.contentResolver
        var fileName = "upload_doc_${System.currentTimeMillis()}.jpg" // Nama default jika gagal ambil nama asli

        // Ambil nama file asli beserta ekstensinya langsung dari HP
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }

        val inputStream = contentResolver.openInputStream(uri) ?: return null
        // Buat file temporary dengan nama asli filenya
        val tempFile = File(context.cacheDir, fileName)
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}