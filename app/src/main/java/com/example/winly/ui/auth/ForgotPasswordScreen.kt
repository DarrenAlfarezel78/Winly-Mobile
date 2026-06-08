package com.example.winly.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
fun ForgotPasswordScreen(onBack: () -> Unit) {
    // Step 1 = input email, Step 2 = input OTP + password baru
    var currentStep by remember { mutableIntStateOf(1) }
    var email by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF8F9FA)) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentStep == 2) currentStep = 1 else onBack()
                    },
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.White)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, modifier = Modifier.size(20.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("WINLY", color = Color(0xFF6789BA), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.School, null, tint = Color(0xFF6789BA), modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Card utama
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.White)
                    .padding(28.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // Icon
                    Box(
                        modifier = Modifier.size(90.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFF0F4F8)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentStep == 1) Icons.Default.Mail else Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = Color(0xFF0061D1),
                            modifier = Modifier.size(45.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Step indicator
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StepDot(isActive = currentStep == 1)
                        StepDot(isActive = currentStep == 2)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = if (currentStep == 1) "Lupa Password?" else "Reset Password",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (currentStep == 1)
                            "Masukkan email kamu untuk\nmendapatkan kode OTP"
                        else
                            "Masukkan kode OTP dan\npassword baru kamu",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(36.dp))

                    // ============================================================
                    // STEP 1: Input Email
                    // ============================================================
                    if (currentStep == 1) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("EMAIL ADDRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = email,
                                onValueChange = { email = it },
                                placeholder = { Text("name@example.com", color = Color.LightGray) },
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(18.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF5F5F5),
                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                if (email.isNotEmpty()) {
                                    isLoading = true
                                    RetrofitClient.instance.sendForgotOtp(email = email)
                                        .enqueue(object : Callback<LoginResponse> {
                                            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                                isLoading = false
                                                val body = response.body()
                                                if (body?.status == "success") {
                                                    Toast.makeText(context, "Kode OTP: ${body.data?.role ?: ""}\nCek console/log", Toast.LENGTH_LONG).show()
                                                    currentStep = 2
                                                } else {
                                                    Toast.makeText(context, body?.message ?: "Gagal mengirim OTP", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                                isLoading = false
                                                Toast.makeText(context, "Koneksi error: ${t.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                } else {
                                    Toast.makeText(context, "Email wajib diisi!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061D1)),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Kirim Kode OTP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    // ============================================================
                    // STEP 2: Input OTP + Password Baru
                    // ============================================================
                    if (currentStep == 2) {
                        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {

                            // Info email
                            Surface(color = Color(0xFFE5F0FF), shape = RoundedCornerShape(12.dp)) {
                                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Email, null, tint = Color(0xFF0061D1), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(email, color = Color(0xFF0061D1), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Input OTP
                            Column {
                                Text("KODE OTP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                TextField(
                                    value = otp,
                                    onValueChange = { if (it.length <= 6) otp = it },
                                    placeholder = { Text("000000", color = Color.LightGray) },
                                    leadingIcon = { Icon(Icons.Default.VpnKey, null, tint = Color.Gray) },
                                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(18.dp)),
                                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }

                            // Input Password Baru
                            Column {
                                Text("PASSWORD BARU", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                TextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    placeholder = { Text("Minimal 6 karakter", color = Color.LightGray) },
                                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = Color.Gray) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(18.dp)),
                                    colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF5F5F5), unfocusedContainerColor = Color(0xFFF5F5F5), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                                    singleLine = true
                                )
                            }

                            // Konfirmasi Password
                            Column {
                                Text("KONFIRMASI PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                TextField(
                                    value = confirmPassword,
                                    onValueChange = { confirmPassword = it },
                                    placeholder = { Text("Ulangi password baru", color = Color.LightGray) },
                                    leadingIcon = { Icon(Icons.Default.LockOpen, null, tint = if (confirmPassword == newPassword && confirmPassword.isNotEmpty()) Color(0xFF22C55E) else Color.Gray) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(18.dp)),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFF5F5F5),
                                        unfocusedContainerColor = Color(0xFFF5F5F5),
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    singleLine = true
                                )
                                // Indikator password match
                                if (confirmPassword.isNotEmpty()) {
                                    Text(
                                        if (confirmPassword == newPassword) "✓ Password cocok" else "✗ Password tidak cocok",
                                        color = if (confirmPassword == newPassword) Color(0xFF22C55E) else Color(0xFFDC2626),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                when {
                                    otp.length != 6 -> Toast.makeText(context, "Masukkan 6 digit OTP", Toast.LENGTH_SHORT).show()
                                    newPassword.length < 6 -> Toast.makeText(context, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                                    newPassword != confirmPassword -> Toast.makeText(context, "Password tidak cocok!", Toast.LENGTH_SHORT).show()
                                    else -> {
                                        isLoading = true
                                        RetrofitClient.instance.resetPassword(
                                            email = email,
                                            otp = otp,
                                            newPassword = newPassword
                                        ).enqueue(object : Callback<LoginResponse> {
                                            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                                                isLoading = false
                                                val body = response.body()
                                                if (body?.status == "success") {
                                                    Toast.makeText(context, "Password berhasil direset! Silakan login.", Toast.LENGTH_LONG).show()
                                                    onBack()
                                                } else {
                                                    Toast.makeText(context, body?.message ?: "Gagal reset password", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                                isLoading = false
                                                Toast.makeText(context, "Koneksi error: ${t.message}", Toast.LENGTH_SHORT).show()
                                            }
                                        })
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0061D1)),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.CheckCircle, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Reset Password", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier.clickable { if (currentStep == 2) currentStep = 1 else onBack() }.padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null, tint = Color(0xFF0061D1), modifier = Modifier.size(18.dp))
                        Text(
                            if (currentStep == 2) "Kembali ke Step 1" else "Kembali ke Login",
                            color = Color(0xFF0061D1),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepDot(isActive: Boolean) {
    Box(
        modifier = Modifier
            .width(if (isActive) 24.dp else 8.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(if (isActive) Color(0xFF0061D1) else Color(0xFFD1D5DB))
    )
}