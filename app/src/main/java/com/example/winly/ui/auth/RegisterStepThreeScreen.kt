package com.example.winly.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.winly.api.LoginResponse
import com.example.winly.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@Composable
fun RegisterStepThreeScreen(
    email: String, // Email yang dilempar dari Step 2
    onBack: () -> Unit,
    onVerifySuccess: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER SECTION ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFF5F5F5))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("WINLY", color = Color(0xFF6789BA), fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(50.dp))

        // --- TITLE SECTION ---
        Text("STEP 3 DARI 3", color = Color(0xFF0061D1), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Text("Verifikasi\nAkun", fontSize = 40.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, lineHeight = 44.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Masukkan 6 digit kode OTP yang telah kami kirimkan ke database untuk email:\n$email", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)

        Spacer(modifier = Modifier.height(40.dp))

        // --- INPUT OTP SECTION ---
        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6) otpCode = it }, // Batasi 6 karakter
            placeholder = { Text("000000", color = Color.LightGray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFF6789BA)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(0.8f).height(64.dp).shadow(8.dp, RoundedCornerShape(50.dp)),
            shape = RoundedCornerShape(50.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color(0xFF0061D1)
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 8.sp)
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- BUTTON SECTION ---
        Button(
            onClick = {
                if (otpCode.length == 6) {
                    isLoading = true

                    // Memanggil API Verify
                    RetrofitClient.instance.verifyUser(email, otpCode).enqueue(object : Callback<LoginResponse> {
                        override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                            isLoading = false
                            val body = response.body()

                            if (response.isSuccessful && body?.status == "success") {
                                Toast.makeText(context, "Verifikasi Berhasil!", Toast.LENGTH_SHORT).show()
                                onVerifySuccess() // Pindah ke halaman Login
                            } else {
                                Toast.makeText(context, body?.message ?: "Kode OTP Salah", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                            isLoading = false
                            Toast.makeText(context, "Koneksi Putus: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                } else {
                    Toast.makeText(context, "Masukkan 6 digit angka OTP", Toast.LENGTH_SHORT).show()
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("VERIFIKASI", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}