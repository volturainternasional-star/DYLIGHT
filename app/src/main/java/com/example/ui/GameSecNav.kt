package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

enum class GameSecNavTab(
  val label: String,
  val subtitle: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
  val isDrawerVisible: Boolean = true,
  val adminOnly: Boolean = false
) {
  TUGAS_SEKOLAH("Tugas Sekolah", "Buku PR & Jadwal Belajar", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_tugas_sekolah"),
  MARKET("Market", "Chart Saham & Kripto Purbaya", Icons.Filled.ShowChart, Icons.Outlined.ShowChart, "nav_market"),
  BERANDA("Beranda", "Dashboard Utama & Ringkasan", Icons.Filled.Home, Icons.Outlined.Home, "nav_beranda"),
  PORTFOLIO("Portfolio", "Riwayat Profit & Konsistensi", Icons.Filled.Star, Icons.Outlined.Star, "nav_portfolio"),
  PROFIL("Profile", "Akun Google & Verifikasi", Icons.Filled.Person, Icons.Outlined.Person, "nav_profil"),
  ADMIN_PANEL("Admin", "Panel User & Keamanan Server", Icons.Filled.Lock, Icons.Outlined.Lock, "nav_admin", adminOnly = false),

  // Learning Modules & Secondary Modules
  MODUL("Modul", "10 Modul Keamanan Game", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_modul", isDrawerVisible = true),
  TERMINAL("Terminal", "CLI Sandbox & Memory", Icons.Filled.Terminal, Icons.Outlined.Terminal, "nav_terminal", isDrawerVisible = false),
  DETEKSI("Deteksi", "Logika Anti-Cheat & Hooking", Icons.Filled.Security, Icons.Outlined.Security, "nav_deteksi", isDrawerVisible = false),
  KUIS("Kuis", "Uji Pemahaman & Sertifikasi", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "nav_kuis", isDrawerVisible = false),
  TOOLS_HUB("Tools", "Hub Tools & Security API", Icons.Filled.Build, Icons.Outlined.Build, "nav_tools_hub", isDrawerVisible = false),
  AI_HUB("AI & Sains", "Neural Core & Cloud Intelligence", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_ai_hub", isDrawerVisible = false),
  SECURITY_LAB("Security Lab", "Simulasi Validasi Game", Icons.Filled.Science, Icons.Outlined.Science, "nav_security_lab", isDrawerVisible = false),
  INVESTIGASI("Investigasi", "Forensik & Bukti Klaim", Icons.Filled.FactCheck, Icons.Outlined.FactCheck, "nav_investigasi", isDrawerVisible = false),
  CODING("Coding", "Coding Academy Playground", Icons.Filled.Code, Icons.Outlined.Code, "nav_coding", isDrawerVisible = false)
}
