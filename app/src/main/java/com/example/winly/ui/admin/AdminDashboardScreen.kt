package com.example.winly.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.winly.data.AdminStatsData
import com.example.winly.api.RetrofitClient // Sesuaikan dengan lokasi RetrofitClient kamu
import kotlinx.coroutines.launch

@Composable
fun AdminDashboardScreen(
    onLogoutClick: () -> Unit,
    onVerificationClick: () -> Unit
) {
    var stats by remember { mutableStateOf<AdminStatsData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Mengambil data dari API saat layar pertama kali dibuka
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getAdminDashboardStats()
            if (response.isSuccessful && response.body()?.status == "success") {
                stats = response.body()?.data
            } else {
                errorMessage = "Gagal memuat data statistik."
            }
        } catch (e: Exception) {
            errorMessage = "Terjadi kesalahan jaringan: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)) // Warna background abu-abu terang
            .padding(20.dp)
    ) {
        // --- TOP BAR ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Admin Icon",
                    tint = Color(0xFF0D47A1),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Winly Admin",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0D47A1)
                )
            }

            TextButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Log Out",
                    tint = Color(0xFF0D47A1)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Log Out", color = Color(0xFF0D47A1), fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- GREETING ---
        Text(
            text = "Halo, Admin!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF102A43)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Berikut adalah ringkasan performa platform hari ini.",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- STATS GRID ---
        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0D47A1))
            }
        } else if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red)
        } else {
            // Baris 1
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "TOTAL PESERTA",
                    value = stats?.totalPeserta?.toString() ?: "0",
                    icon = Icons.Default.People,
                    iconTint = Color(0xFF0D47A1),
                    iconBg = Color(0xFFE3F2FD)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "TOTAL KOMPETISI",
                    value = stats?.totalKompetisi?.toString() ?: "0",
                    icon = Icons.Default.EmojiEvents,
                    iconTint = Color(0xFF2E7D32),
                    iconBg = Color(0xFFE8F5E9)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Baris 2
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "PENYELENGGARA",
                    value = stats?.totalPenyelenggara?.toString() ?: "0",
                    icon = Icons.Default.Work,
                    iconTint = Color(0xFFF57F17),
                    iconBg = Color(0xFFFFF3E0)
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    title = "VERIFIKASI TERPENDING",
                    value = stats?.pendingVerifikasi?.toString() ?: "0",
                    icon = Icons.Default.VerifiedUser,
                    iconTint = Color(0xFFC62828),
                    iconBg = Color(0xFFFFEBEE),
                    isAlert = (stats?.pendingVerifikasi ?: 0) > 0
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- TINDAKAN CEPAT ---
        Text(
            text = "Tindakan Cepat",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF102A43)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0D47A1))
                .clickable { onVerificationClick() }
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Verifikasi Penyelenggara",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Ada ${stats?.pendingVerifikasi ?: 0} pengajuan yang butuh review",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    isAlert: Boolean = false
) {
    Card(
        modifier = modifier.height(120.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Garis merah di kiri jika statusnya alert (pending > 0)
            if (isAlert) {
                Box(modifier = Modifier.fillMaxHeight().width(4.dp).background(Color(0xFFC62828)))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }

                Column {
                    Text(text = title, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = value,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isAlert) Color(0xFFC62828) else Color(0xFF0D47A1)
                    )
                }
            }
        }
    }
}