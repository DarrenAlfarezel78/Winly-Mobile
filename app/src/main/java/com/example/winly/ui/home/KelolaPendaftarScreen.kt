package com.example.winly.ui.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.winly.api.LoginResponse
import com.example.winly.api.RegistrationModel
import com.example.winly.api.RegistrationResponse
import com.example.winly.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaPendaftarScreen(
    competitionId: Int,
    judulLomba: String = "Lomba",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var pendaftarList by remember { mutableStateOf<List<RegistrationModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("semua") }

    fun loadPendaftar() {
        isLoading = true
        RetrofitClient.instance.getRegistrations(competitionId)
            .enqueue(object : Callback<RegistrationResponse> {
                override fun onResponse(call: Call<RegistrationResponse>, response: Response<RegistrationResponse>) {
                    isLoading = false
                    pendaftarList = response.body()?.data ?: emptyList()
                }
                override fun onFailure(call: Call<RegistrationResponse>, t: Throwable) {
                    isLoading = false
                    Toast.makeText(context, "Gagal memuat pendaftar", Toast.LENGTH_SHORT).show()
                }
            })
    }

    LaunchedEffect(competitionId) { loadPendaftar() }

    val filteredList = when (selectedFilter) {
        "pending"   -> pendaftarList.filter { it.statusPendaftaran == "pending" }
        "diterima"  -> pendaftarList.filter { it.statusPendaftaran == "diterima" }
        "ditolak"   -> pendaftarList.filter { it.statusPendaftaran == "ditolak" }
        else        -> pendaftarList
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Kelola Pendaftar", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text(judulLomba, fontSize = 11.sp, color = Color.Gray, maxLines = 1)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Summary card
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryChip("Total", pendaftarList.size.toString(), Color(0xFF0061D1))
                SummaryChip("Pending", pendaftarList.count { it.statusPendaftaran == "pending" }.toString(), Color(0xFFF97316))
                SummaryChip("Diterima", pendaftarList.count { it.statusPendaftaran == "diterima" }.toString(), Color(0xFF22C55E))
                SummaryChip("Ditolak", pendaftarList.count { it.statusPendaftaran == "ditolak" }.toString(), Color(0xFFDC2626))
            }

            // Filter tabs
            Row(
                modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 16.dp).padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("semua", "pending", "diterima", "ditolak").forEach { filter ->
                    FilterTab(
                        label = filter.replaceFirstChar { it.uppercase() },
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            if (isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF0061D1))
                }
            } else if (filteredList.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PeopleOutline, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Belum ada pendaftar", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(filteredList) { pendaftar ->
                        PendaftarCard(
                            pendaftar = pendaftar,
                            onTerima = {
                                RetrofitClient.instance.updateRegistration(
                                    registrationId = pendaftar.id?.toIntOrNull() ?: 0,
                                    status = "diterima"
                                ).enqueue(object : Callback<LoginResponse> {
                                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                        if (response.body()?.status == "success") {
                                            Toast.makeText(context, "Pendaftar diterima! ✅", Toast.LENGTH_SHORT).show()
                                            loadPendaftar()
                                        }
                                    }
                                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
                                })
                            },
                            onTolak = {
                                RetrofitClient.instance.updateRegistration(
                                    registrationId = pendaftar.id?.toIntOrNull() ?: 0,
                                    status = "ditolak"
                                ).enqueue(object : Callback<LoginResponse> {
                                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                        if (response.body()?.status == "success") {
                                            Toast.makeText(context, "Pendaftar ditolak ❌", Toast.LENGTH_SHORT).show()
                                            loadPendaftar()
                                        }
                                    }
                                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {}
                                })
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
fun SummaryChip(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
fun FilterTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50.dp),
        color = if (isSelected) Color(0xFF0061D1) else Color(0xFFF2F2F2)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else Color.Gray
        )
    }
}

@Composable
fun PendaftarCard(
    pendaftar: RegistrationModel,
    onTerima: () -> Unit,
    onTolak: () -> Unit
) {
    val status = pendaftar.statusPendaftaran ?: "pending"
    val statusColor = when (status) {
        "diterima" -> Color(0xFF22C55E)
        "ditolak"  -> Color(0xFFDC2626)
        else       -> Color(0xFFF97316)
    }
    val statusLabel = when (status) {
        "diterima" -> "Diterima"
        "ditolak"  -> "Ditolak"
        else       -> "Pending"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar inisial
                Box(
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(Color(0xFFE5F0FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        pendaftar.namaPeserta?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = Color(0xFF0061D1), fontWeight = FontWeight.Bold, fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(pendaftar.namaPeserta ?: "-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(pendaftar.emailPeserta ?: "-", fontSize = 11.sp, color = Color.Gray)
                    if (!pendaftar.instansiPeserta.isNullOrEmpty()) {
                        Text(pendaftar.instansiPeserta, fontSize = 11.sp, color = Color(0xFF0061D1))
                    }
                }
                // Badge status
                Surface(color = statusColor.copy(0.1f), shape = RoundedCornerShape(50.dp)) {
                    Text(statusLabel, color = statusColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
            }

            // Tanggal daftar
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Daftar: ${pendaftar.createdAt?.take(10) ?: "-"}", fontSize = 11.sp, color = Color.Gray)
            }

            // Tombol aksi - hanya tampil jika masih pending
            if (status == "pending") {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Tombol Tolak
                    OutlinedButton(
                        onClick = onTolak,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tolak", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    // Tombol Terima
                    Button(
                        onClick = onTerima,
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                    ) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Terima", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}