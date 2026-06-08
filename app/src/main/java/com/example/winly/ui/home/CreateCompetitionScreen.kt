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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.winly.api.LoginResponse
import com.example.winly.api.RetrofitClient
import com.example.winly.data.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.*

// ====================================================================
// CLOUDINARY CONFIG
// ====================================================================
private const val CLOUDINARY_CLOUD_NAME = "dt1sgatsn"
private const val CLOUDINARY_UPLOAD_PRESET = "ml_default"
private const val CLOUDINARY_URL = "https://api.cloudinary.com/v1_1/$CLOUDINARY_CLOUD_NAME/image/upload"

// ====================================================================
// FUNGSI UPLOAD KE CLOUDINARY
// ====================================================================
suspend fun uploadToCloudinary(context: android.content.Context, imageUri: Uri): String? {
    return withContext(Dispatchers.IO) {
        try {
            // Convert URI ke File
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return@withContext null
            val tempFile = File.createTempFile("poster_", ".jpg", context.cacheDir)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            // Upload ke Cloudinary
            val client = OkHttpClient()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", tempFile.name, tempFile.asRequestBody("image/*".toMediaTypeOrNull()))
                .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                .addFormDataPart("folder", "winly_posters")
                .build()

            val request = Request.Builder()
                .url(CLOUDINARY_URL)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && responseBody != null) {
                val json = JSONObject(responseBody)
                json.getString("secure_url") // Return URL gambar
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateCompetitionScreen(onBack: () -> Unit, onUploadSuccess: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    // Ambil userId dari session - FIX hardcode!
    val userId = sessionManager.getUserId()
    var sisaKuota by remember { mutableIntStateOf(sessionManager.getSisaKuota()) }

    var isLoading by remember { mutableStateOf(false) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var showQrisDialog by remember { mutableStateOf(false) }

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

    val calendar = Calendar.getInstance()

    val datePicker = DatePickerDialog(context,
        { _, y, m, d -> tanggal = "$y-${m + 1}-$d" },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    val dateTutupPicker = DatePickerDialog(context,
        { _, y, m, d -> tanggalTutup = "$y-${m + 1}-$d" },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Launcher pilih gambar dari galeri
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Upload ke Cloudinary otomatis setelah pilih gambar
            isUploadingImage = true
            CoroutineScope(Dispatchers.Main).launch {
                val url = uploadToCloudinary(context, it)
                isUploadingImage = false
                if (url != null) {
                    posterUrl = url
                    Toast.makeText(context, "Poster berhasil diupload! ✅", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Gagal upload poster, coba lagi", Toast.LENGTH_SHORT).show()
                    selectedImageUri = null
                }
            }
        }
    }

    fun uploadLomba() {
        isLoading = true
        RetrofitClient.instance.createCompetition(
            penyelenggaraId   = userId,
            judul             = judul,
            kategori          = kategori,
            tingkatPendidikan = tingkatPendidikan,
            tingkatLomba      = wilayah,
            deskripsi         = linkJuknis,
            linkPendaftaran   = linkPendaftaran,
            linkPanduan       = linkJuknis,
            tanggal           = tanggal,
            tanggalTutup      = tanggalTutup,
            biaya             = hargaDaftar.toIntOrNull() ?: 0,
            posterUrl         = posterUrl // <--- INI PINTU POSTERNYA SUDAH SAYA TAMBAHKAN
        ).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                isLoading = false
                val body = response.body()
                // Tampilkan pesan detail dari server
                Toast.makeText(context, "Server: ${body?.message}", Toast.LENGTH_LONG).show()
                if (response.isSuccessful && body?.status == "success") {
                    if (sisaKuota > 0) {
                        sessionManager.updateSisaKuota(sisaKuota - 1)
                        sisaKuota--
                    }
                    showQrisDialog = false
                    onUploadSuccess()
                } else {
                    Toast.makeText(context, response.body()?.message ?: "Gagal", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Buat Lomba Baru", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Alert kuota
            if (sisaKuota <= 0) {
                Surface(color = Color(0xFFFEF2F2), border = BorderStroke(1.dp, Color(0xFFFCA5A5)), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, null, tint = Color(0xFFDC2626))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kuota gratis habis. Penerbitan lomba ini akan dikenakan biaya admin.", color = Color(0xFF991B1B), fontSize = 12.sp)
                    }
                }
            } else {
                Surface(color = Color(0xFFEFF6FF), border = BorderStroke(1.dp, Color(0xFF93C5FD)), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CardGiftcard, null, tint = Color(0xFF0061D1))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sisa kuota gratis: $sisaKuota lomba", color = Color(0xFF0061D1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // ================================================================
            // UPLOAD POSTER - BARU!
            // ================================================================
            SectionHeader("POSTER LOMBA")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF2F2F2))
                    .border(2.dp, if (selectedImageUri != null) Color(0xFF0061D1) else Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                    .clickable { if (!isUploadingImage) imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (isUploadingImage) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF0061D1))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Mengupload poster...", fontSize = 12.sp, color = Color.Gray)
                    }
                } else if (selectedImageUri != null && posterUrl.isNotEmpty()) {
                    // Tampilkan preview gambar
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = "Poster Lomba",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Overlay tombol ganti
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
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
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tap untuk pilih poster", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0061D1))
                        Text("JPG, PNG (Opsional)", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }

            if (posterUrl.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF22C55E), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Poster berhasil diupload ke Cloudinary", fontSize = 11.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                }
            }

            // ================================================================
            // INFORMASI DASAR
            // ================================================================
            SectionHeader("INFORMASI DASAR")

            OutlinedTextField(
                value = judul, onValueChange = { judul = it },
                label = { Text("Judul Perlombaan *") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
            )

            // Tingkat Pendidikan
            Column {
                Text("Tingkat Pendidikan *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SD", "SMP", "SMA/SMK", "Mahasiswa", "Umum").forEach { pend ->
                        SelectableChip(text = pend, isSelected = tingkatPendidikan == pend) { tingkatPendidikan = pend }
                    }
                }
            }

            // Tingkat Wilayah
            Column {
                Text("Tingkat Wilayah Lomba *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = isWilayahDropdownExpanded, onExpandedChange = { isWilayahDropdownExpanded = !isWilayahDropdownExpanded }) {
                    OutlinedTextField(
                        value = wilayah, onValueChange = {}, readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isWilayahDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(expanded = isWilayahDropdownExpanded, onDismissRequest = { isWilayahDropdownExpanded = false }) {
                        listOf("Kota", "Provinsi", "Nasional", "Internasional").forEach { w ->
                            DropdownMenuItem(text = { Text(w) }, onClick = { wilayah = w; isWilayahDropdownExpanded = false })
                        }
                    }
                }
            }

            // Tanggal Pelaksanaan
            OutlinedTextField(
                value = tanggal, onValueChange = {},
                label = { Text("Tanggal Pelaksanaan *") },
                modifier = Modifier.fillMaxWidth().clickable { datePicker.show() },
                enabled = false, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledLabelColor = Color.Gray, disabledBorderColor = Color.Gray),
                trailingIcon = { Icon(Icons.Default.DateRange, null) },
                placeholder = { Text("Pilih tanggal") }
            )

            // Deadline Pendaftaran
            OutlinedTextField(
                value = tanggalTutup, onValueChange = {},
                label = { Text("Deadline Pendaftaran") },
                modifier = Modifier.fillMaxWidth().clickable { dateTutupPicker.show() },
                enabled = false, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.Black, disabledLabelColor = Color.Gray, disabledBorderColor = Color.Gray),
                trailingIcon = { Icon(Icons.Default.EventBusy, null) },
                placeholder = { Text("Pilih deadline (opsional)") }
            )

            OutlinedTextField(
                value = hargaDaftar, onValueChange = { hargaDaftar = it },
                label = { Text("Biaya Pendaftaran (Rp) - 0 jika gratis") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = linkPendaftaran, onValueChange = { linkPendaftaran = it },
                label = { Text("Link Pendaftaran (Google Form, dll)") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = linkJuknis, onValueChange = { linkJuknis = it },
                label = { Text("Link Juknis / Deskripsi") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            )

            // Tombol submit
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("Batal", color = Color.Gray, fontWeight = FontWeight.Bold) }
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = {
                        if (judul.isNotEmpty() && tanggal.isNotEmpty()) {
                            val isPremium = wilayah in listOf("Provinsi", "Nasional", "Internasional")
                            if (isPremium || sisaKuota <= 0) showQrisDialog = true
                            else uploadLomba()
                        } else {
                            Toast.makeText(context, "Judul dan Tanggal wajib diisi!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.height(50.dp),
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061D1)),
                    enabled = !isLoading && !isUploadingImage
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Terbitkan Lomba", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Dialog QRIS
        if (showQrisDialog) {
            AlertDialog(
                onDismissRequest = { showQrisDialog = false },
                containerColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE5F0FF)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.QrCodeScanner, null, tint = Color(0xFF0061D1))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Pembayaran Platform", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, textAlign = TextAlign.Center)
                    }
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Penerbitan lomba tingkat $wilayah memerlukan biaya platform.", textAlign = TextAlign.Center, fontSize = 13.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.size(150.dp).background(Color.White).border(2.dp, Color(0xFF0061D1), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.QrCode2, null, modifier = Modifier.size(120.dp), tint = Color.Black)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Total Tagihan", color = Color.Gray, fontSize = 12.sp)
                        Text("Rp 50.000", fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color(0xFF0061D1))
                    }
                },
                confirmButton = {
                    Button(onClick = { uploadLomba() }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(50.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061D1))) {
                        Text("Saya Sudah Bayar & Terbitkan", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQrisDialog = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Batalkan", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, paddingBottom: Int = 8) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(4.dp).height(18.dp).background(Color(0xFF0061D1), RoundedCornerShape(4.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color.DarkGray)
    }
    Spacer(modifier = Modifier.height(paddingBottom.dp))
}

@Composable
fun SelectableChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) Color(0xFFE5F0FF) else Color.White,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, if (isSelected) Color(0xFF0061D1) else Color.LightGray),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(text, color = if (isSelected) Color(0xFF0061D1) else Color.Gray, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
    }
}