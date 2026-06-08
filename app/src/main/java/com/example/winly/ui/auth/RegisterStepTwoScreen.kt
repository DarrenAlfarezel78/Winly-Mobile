package com.example.winly.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.winly.api.LoginResponse
import com.example.winly.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun RegisterStepTwoScreen(
    userName: String,
    selectedRole: String,
    instansi: String = "",   // BARU: terima instansi dari step 1
    onBack: () -> Unit,
    onSignUp: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().background(Color.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5))) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("WINLY", color = Color(0xFF6789BA), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.School, null, tint = Color(0xFF6789BA), modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text("STEP 2 DARI 3", color = Color(0xFF0061D1), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text("Amankan Akun\nAnda", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, lineHeight = 40.sp)

        Spacer(modifier = Modifier.height(8.dp))

        // Info user dari step 1
        Surface(color = Color(0xFFE5F0FF), shape = RoundedCornerShape(12.dp)) {
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, null, tint = Color(0xFF0061D1), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("$userName • ${selectedRole.replaceFirstChar { it.uppercase() }}", color = Color(0xFF0061D1), fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Input Email
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("EMAIL ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(start = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("name@example.com", color = Color.LightGray) },
                trailingIcon = { Icon(Icons.Default.AlternateEmail, null, tint = Color(0xFF6789BA)) },
                modifier = Modifier.fillMaxWidth().height(60.dp).shadow(4.dp, RoundedCornerShape(50.dp)),
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color(0xFF0061D1)),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray, modifier = Modifier.padding(start = 20.dp))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Minimal 6 karakter", color = Color.LightGray) },
                trailingIcon = { Icon(Icons.Default.Lock, null, tint = Color(0xFF6789BA)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().height(60.dp).shadow(4.dp, RoundedCornerShape(50.dp)),
                shape = RoundedCornerShape(50.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White, unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color(0xFF0061D1)),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Tombol Daftar
        Button(
            onClick = {
                if (email.isNotEmpty() && password.length >= 6) {
                    isLoading = true
                    RetrofitClient.instance.registerUser(
                        name     = userName,
                        email    = email,
                        password = password,
                        role     = selectedRole,
                        instansi = instansi   // BARU: kirim instansi ke API
                    ).enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            isLoading = false
                            val body = response.body()
                            if (response.isSuccessful && body?.status == "success") {
                                onSignUp(email)
                            } else {
                                Toast.makeText(context, body?.message ?: "Gagal mendaftar", Toast.LENGTH_LONG).show()
                            }
                        }
                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            isLoading = false
                            Toast.makeText(context, "Koneksi error: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "Lengkapi data (Password min 6 karakter)", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(0.85f).height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061D1)),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("DAFTAR SEKARANG", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Icon(Icons.Default.ArrowForward, null)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}