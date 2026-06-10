package com.example.winly.ui.home

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.winly.api.CompetitionDetailResponse
import com.example.winly.api.LoginResponse
import com.example.winly.api.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditCompetitionScreen(
    competitionId: Int,
    onBack: () -> Unit,
    onUpdateSuccess: () -> Unit
) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var isUpdating by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }

    // State poster
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var posterUrl by remember { mutableStateOf("") }

    // State form
    var judul by remember { mutableStateOf("") }
    var kategori by remember { mutableStateOf("Teknologi & IT") }
    var tingkatPendidikan by remember { mutableStateOf("SMA/SMK") }
    var wilayah by remember { mutableStateOf("Nasional") }
    var tanggal by remember { mutableStateOf("") }
    var tanggalTutup by remember { mutableStateOf("") }
    var hargaDaftar by remember { mutableStateOf("") }
    var linkPendaftaran by remember { mutableStateOf("") }
    var linkJuknis by remember { mutableStateOf("") }

    var isWilayahDropdownExpanded by remember { mutableStateOf(false) }
    var isKategoriDropdownExpanded by remember { mutableStateOf(false) }
    val listKategori = listOf("Teknologi & IT", "Sains & Matematika", "Ekonomi & Bisnis", "Karya Tulis & Riset", "Seni & Desain", "Soshum & Hukum")

    val calendar = Calendar.getInstance()
    val datePicker = DatePickerDialog(context, { _, y, m, d -> tanggal = "$y-${m + 1}-$d" }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    val dateTutupPicker = DatePickerDialog(context, { _, y, m, d -> tanggalTutup = "$y-${m + 1}-$d" }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    // AMBIL DATA LAMA (MEMORI) SAAT HALAMAN DIBUKA
    LaunchedEffect(competitionId) {
        RetrofitClient.instance.getCompetitionDetail(competitionId).enqueue(object : Callback<CompetitionDetailResponse> {
            override fun onResponse(call: Call<CompetitionDetailResponse>, response: Response<CompetitionDetailResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val data = response.body()?.data
                    if (data != null) {
                        judul = data.judulLomba ?: ""
                        kategori = data.kategori ?: "Teknologi & IT"
                        tingkatPendidikan = data.tingkatPendidikan ?: "SMA/SMK"
                        wilayah = data.tingkatLomba ?: "Nasional"
                        tanggal = data.tanggalPelaksanaan ?: ""
                        tanggalTutup = data.tanggalTutupDaftar ?: ""
                        hargaDaftar = data.biayaPendaftaran?.toString() ?: ""
                        linkPendaftaran = data.linkPendaftaran ?: ""
                        linkJuknis = data.linkPanduan ?: data.deskripsi ?: ""
                        posterUrl = data.posterUrl ?: ""
                    }
                } else {
                    Toast.makeText(context, "Gagal memuat data lama", Toast.LENGTH_SHORT).show()
                }
                isLoading = false
            }
            override fun onFailure(call: Call<CompetitionDetailResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Error koneksi", Toast.LENGTH_SHORT).show()
            }
        })
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            isUploadingImage = true
            CoroutineScope(Dispatchers.Main).launch {
                val url = uploadToCloudinary(context, it) // Pastikan fungsi ini masih ada dan bisa diakses
                isUploadingImage = false
                if (url != null) {
                    posterUrl = url
                    Toast.makeText(context, "Poster baru berhasil disiapkan!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal upload poster", Toast.LENGTH_SHORT).show()
                    selectedImageUri = null
                }
            }
        }
    }

    fun simpanPerubahan() {
        isUpdating = true
        RetrofitClient.instance.updateCompetition(
            id = competitionId,
            judul = judul,
            kategori = kategori,
            tingkatPendidikan = tingkatPendidikan,
            tingkatLomba = wilayah,
            deskripsi = linkJuknis,
            linkPendaftaran = linkPendaftaran,
            linkPanduan = linkJuknis,
            tanggalPelaksanaan = tanggal,
            tanggalTutupDaftar = tanggalTutup,
            biayaPendaftaran = hargaDaftar.toIntOrNull() ?: 0,
            posterUrl = posterUrl
        ).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                isUpdating = false
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(context, "Perubahan berhasil disimpan!", Toast.LENGTH_SHORT).show()
                    onUpdateSuccess()
                } else {
                    Toast.makeText(context, "Gagal: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                isUpdating = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Lomba", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0061D1))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader("POSTER LOMBA")
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF2F2F2)).border(2.dp, if (posterUrl.isNotEmpty()) Color(0xFF0061D1) else Color(0xFFE0E0E0), RoundedCornerShape(16.dp)).clickable { if (!isUploadingImage) imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (isUploadingImage) {
                    CircularProgressIndicator(color = Color(0xFF0061D1))
                } else if (posterUrl.isNotEmpty() || selectedImageUri != null) {
                    AsyncImage(
                        model = selectedImageUri ?: posterUrl,
                        contentDescription = "Poster",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        Surface(color = Color.White.copy(0.9f), shape = RoundedCornerShape(50.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, null, tint = Color(0xFF0061D1), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ganti Poster", fontSize = 12.sp, color = Color(0xFF0061D1), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddPhotoAlternate, null, tint = Color(0xFF0061D1), modifier = Modifier.size(40.dp))
                        Text("Tap untuk ganti poster", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0061D1))
                    }
                }
            }

            SectionHeader("INFORMASI DASAR")

            OutlinedTextField(value = judul, onValueChange = { judul = it }, label = { Text("Judul Perlombaan *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            Column {
                Text("Kategori Lomba *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = isKategoriDropdownExpanded, onExpandedChange = { isKategoriDropdownExpanded = !isKategoriDropdownExpanded }) {
                    OutlinedTextField(value = kategori, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isKategoriDropdownExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = isKategoriDropdownExpanded, onDismissRequest = { isKategoriDropdownExpanded = false }) {
                        listKategori.forEach { kat -> DropdownMenuItem(text = { Text(kat) }, onClick = { kategori = kat; isKategoriDropdownExpanded = false }) }
                    }
                }
            }

            Column {
                Text("Tingkat Pendidikan *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SD", "SMP", "SMA/SMK", "Mahasiswa", "Umum").forEach { pend ->
                        SelectableChip(text = pend, isSelected = tingkatPendidikan == pend) { tingkatPendidikan = pend }
                    }
                }
            }

            Column {
                Text("Tingkat Wilayah Lomba *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = isWilayahDropdownExpanded, onExpandedChange = { isWilayahDropdownExpanded = !isWilayahDropdownExpanded }) {
                    OutlinedTextField(value = wilayah, onValueChange = {}, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isWilayahDropdownExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp))
                    ExposedDropdownMenu(expanded = isWilayahDropdownExpanded, onDismissRequest = { isWilayahDropdownExpanded = false }) {
                        listOf("Kota", "Provinsi", "Nasional", "Internasional").forEach { w -> DropdownMenuItem(text = { Text(w) }, onClick = { wilayah = w; isWilayahDropdownExpanded = false }) }
                    }
                }
            }

            OutlinedTextField(value = tanggal, onValueChange = {}, label = { Text("Tanggal Pelaksanaan *") }, modifier = Modifier.fillMaxWidth().clickable { datePicker.show() }, enabled = false, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black))
            OutlinedTextField(value = tanggalTutup, onValueChange = {}, label = { Text("Deadline Pendaftaran") }, modifier = Modifier.fillMaxWidth().clickable { dateTutupPicker.show() }, enabled = false, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black))
            OutlinedTextField(value = hargaDaftar, onValueChange = { hargaDaftar = it }, label = { Text("Biaya Pendaftaran (Rp)") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = linkPendaftaran, onValueChange = { linkPendaftaran = it }, label = { Text("Link Pendaftaran") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))
            OutlinedTextField(value = linkJuknis, onValueChange = { linkJuknis = it }, label = { Text("Deskripsi / Link Juknis") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { if (judul.isNotEmpty() && tanggal.isNotEmpty()) simpanPerubahan() else Toast.makeText(context, "Judul & Tanggal wajib diisi", Toast.LENGTH_SHORT).show() },
                    modifier = Modifier.height(50.dp), shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061D1)), enabled = !isUpdating && !isUploadingImage
                ) {
                    if (isUpdating) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Simpan Perubahan", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}