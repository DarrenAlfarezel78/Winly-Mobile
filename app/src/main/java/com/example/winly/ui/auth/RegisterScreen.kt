package com.example.winly.ui.auth

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onNext: (String, String, String) -> Unit  // nama, role, instansi
) {
    var username by remember { mutableStateOf("") }
    var instansi by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("peserta") }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5))) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(20.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("WINLY", color = Color(0xFF6789BA), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF6789BA), modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("STEP 1 DARI 3", color = Color(0xFF0061D1), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text("Buat Akun\nAnda", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, lineHeight = 40.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Input Username
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            placeholder = { Text("Nama Lengkap", color = Color.LightGray) },
            leadingIcon = {
                Box(modifier = Modifier.padding(start = 12.dp, end = 8.dp).size(36.dp).clip(CircleShape).background(Color(0xFFF0F4F8)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, tint = Color(0xFF0061D1), modifier = Modifier.size(20.dp))
                }
            },
            modifier = Modifier.fillMaxWidth().height(64.dp).shadow(elevation = 4.dp, shape = RoundedCornerShape(50.dp)),
            shape = RoundedCornerShape(50.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color(0xFF0061D1)),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input Instansi (BARU)
        OutlinedTextField(
            value = instansi,
            onValueChange = { instansi = it },
            placeholder = { Text("Asal Sekolah / Kampus / Instansi", color = Color.LightGray) },
            leadingIcon = {
                Box(modifier = Modifier.padding(start = 12.dp, end = 8.dp).size(36.dp).clip(CircleShape).background(Color(0xFFF0F4F8)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Business, null, tint = Color(0xFF0061D1), modifier = Modifier.size(20.dp))
                }
            },
            modifier = Modifier.fillMaxWidth().height(64.dp).shadow(elevation = 4.dp, shape = RoundedCornerShape(50.dp)),
            shape = RoundedCornerShape(50.dp),
            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color(0xFF0061D1)),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Pilihan Role
        Text("Pilih Peran Anda:", modifier = Modifier.fillMaxWidth().padding(start = 12.dp, bottom = 8.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

        RoleSelectionCard(
            title = "Peserta",
            description = "Mencari dan mendaftar perlombaan",
            icon = Icons.Default.Person,
            isSelected = selectedRole == "peserta",
            onClick = { selectedRole = "peserta" }
        )

        Spacer(modifier = Modifier.height(12.dp))

        RoleSelectionCard(
            title = "Penyelenggara",
            description = "Mempublikasikan perlombaan baru",
            icon = Icons.Default.BusinessCenter,
            isSelected = selectedRole == "penyelenggara",
            onClick = { selectedRole = "penyelenggara" }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Tombol Next
        Button(
            onClick = {
                if (username.isNotBlank()) {
                    onNext(username, selectedRole, instansi)
                } else {
                    Toast.makeText(context, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(0.85f).height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061D1))
        ) {
            Text("LANJUTKAN", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text("Sudah punya akun? Log In", color = Color.Black, fontSize = 13.sp)
        }
    }
}

@Composable
fun RoleSelectionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFE5F0FF) else Color.White,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) Color(0xFF0061D1) else Color(0xFFE0E0E0))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = if (isSelected) Color(0xFF0061D1) else Color.Gray, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isSelected) Color.Black else Color.DarkGray)
                Text(description, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}