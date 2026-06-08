package com.example.winly.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.winly.api.LoginResponse
import com.example.winly.api.RetrofitClient
import com.example.winly.data.OrganizerDetailData
import com.example.winly.data.OrganizerDetailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminReviewScreen(
    organizerId: Int,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var organizer by remember { mutableStateOf<OrganizerDetailData?>(null) }

    LaunchedEffect(organizerId) {
        isLoading = true
        RetrofitClient.instance.getOrganizerDetail(organizerId).enqueue(object : Callback<OrganizerDetailResponse> {
            override fun onResponse(call: Call<OrganizerDetailResponse>, response: Response<OrganizerDetailResponse>) {
                organizer = response.body()?.data
                isLoading = false
            }
            override fun onFailure(call: Call<OrganizerDetailResponse>, t: Throwable) {
                isLoading = false
                Toast.makeText(context, "Gagal mengambil data: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun handleAction(action: String) {
        isLoading = true
        RetrofitClient.instance.verifyOrganizer(organizerId, action)
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    isLoading = false
                    if (response.body()?.status == "success") {
                        Toast.makeText(context, response.body()?.message, Toast.LENGTH_SHORT).show()
                        onClose()
                    } else {
                        Toast.makeText(context, "Gagal: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
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
            TopAppBar(
                title = { Text("Review Organizer", color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.PendingActions,
                        contentDescription = null,
                        tint = Color(0xFF0D47A1),
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    )
                },
                actions = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF0D47A1))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        if (isLoading && organizer == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF0D47A1))
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8F9FA))
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("INFORMASI ORGANISASI", color = Color(0xFF0D47A1), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Nama Organisasi", fontSize = 12.sp, color = Color.Gray)
                        Text(organizer?.name ?: "-", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Instansi", fontSize = 12.sp, color = Color.Gray)
                        Text(organizer?.instansi ?: "-", fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Email", fontSize = 12.sp, color = Color.Gray)
                        Text(organizer?.email ?: "-", fontSize = 14.sp)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Tanggal Pendaftaran", fontSize = 12.sp, color = Color.Gray)
                        Text(organizer?.createdAt ?: "-", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Dokumen Legalitas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                if (!organizer?.legalitasUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = organizer?.legalitasUrl,
                        contentDescription = "Dokumen Legalitas",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Text("Tidak ada dokumen")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("KTP Penanggung Jawab", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                if (!organizer?.ktpUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = organizer?.ktpUrl,
                        contentDescription = "KTP",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { handleAction("approve") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Verifikasi / Setujui", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { handleAction("reject") },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isLoading
                ) {
                    Icon(Icons.Default.HighlightOff, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tolak Pendaftaran", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}