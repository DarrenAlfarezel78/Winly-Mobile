package com.example.winly.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.winly.R

@Composable
fun GreetingScreen(onEnterArena: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Spacer untuk memberi jarak dari atas agar posisi logo agak ke tengah atas
            Spacer(modifier = Modifier.weight(1f))

            // --- SECTION LOGO (PIALA) ---
            // Box luar untuk efek "Glow" kuning tipis di belakang logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(45.dp),
                        ambientColor = Color(0xFFFFD700), // Warna emas/kuning
                        spotColor = Color(0xFFFFD700)
                    )
                    .clip(RoundedCornerShape(45.dp))
                    .background(Color.White)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_trophy),
                    contentDescription = "Winly Trophy",
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SECTION TEXT ---
            Text(
                text = "WINLY",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6789BA), // Warna biru muda sesuai gambar
                letterSpacing = 1.sp
            )

            Text(
                text = "MULAI KEMENANGANMU.",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.LightGray,
                letterSpacing = 2.sp
            )

            // Spacer untuk mendorong tombol ke paling bawah
            Spacer(modifier = Modifier.weight(1.2f))

            // --- SECTION BUTTON ---
            Button(
                onClick = onEnterArena,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .shadow(10.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6789BA) // Biru yang senada dengan logo
                )
            ) {
                Text(
                    text = "MASUK ARENA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}