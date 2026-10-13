package com.example.security

import java.security.MessageDigest
import java.util.Locale
import kotlin.math.log2

data class FileAnalysisResult(
  val fileName: String,
  val byteSize: Long,
  val md5Hash: String,
  val sha256Hash: String,
  val entropy: Double,
  val entropyAssessment: String,
  val detectedFormat: String,
  val threatKeywordsFound: List<ThreatMatch>,
  val configIssues: List<ConfigIssue>,
  val overallRiskScore: Int, // 0 - 100
  val riskLevel: String // "AMAN", "PERLU PERHATIAN", "BAHAYA TINGGI"
)

data class ThreatMatch(
  val keyword: String,
  val category: String, // "Hooking/Frida", "Memory Editing", "Privilege Escalation", "Insecure Network"
  val severity: String // "CRITICAL", "HIGH", "MEDIUM"
)

data class ConfigIssue(
  val key: String,
  val value: String,
  val description: String,
  val severity: String
)

object RealGameSecurityAnalyzer {

  private val threatDatabase = listOf(
    ThreatMatch("frida", "Hooking/Frida Framework", "CRITICAL"),
    ThreatMatch("gum-js-loop", "Frida Gum Core Agent", "CRITICAL"),
    ThreatMatch("xposed", "Xposed Framework Hook", "CRITICAL"),
    ThreatMatch("xposedbridge", "Xposed Native Bridge", "CRITICAL"),
    ThreatMatch("cydia", "Substrate Hooking Engine", "CRITICAL"),
    ThreatMatch("ptrace", "Process Memory Debugger Attachment", "HIGH"),
    ThreatMatch("gameguardian", "Memory Scan & Editor Tool", "CRITICAL"),
    ThreatMatch("cheatengine", "Memory Editing Signature", "CRITICAL"),
    ThreatMatch("speedhack", "Kinematic Delta Time Manipulation", "CRITICAL"),
    ThreatMatch("godmode", "Invulnerability State Flag", "CRITICAL"),
    ThreatMatch("unlimitedgold", "Currency Ledger Bypass", "CRITICAL"),
    ThreatMatch("unlimitedammo", "Weapon Ledger Bypass", "CRITICAL"),
    ThreatMatch("bypassssl", "Certificate Pinning Nullification", "HIGH"),
    ThreatMatch("allowallhostname", "Insecure SSL Trust Manager", "HIGH"),
    ThreatMatch("libart.so", "Runtime VM Hooking Vector", "MEDIUM"),
    ThreatMatch("/data/local/tmp", "World-Writable Native Execution Directory", "MEDIUM"),
    ThreatMatch("rootbeer", "Root Cloaking / Detection Bypass", "MEDIUM")
  )

  /**
   * Performs real cryptographic hashing, Shannon entropy calculation,
   * magic bytes detection, and keyword vulnerability scanning on provided content.
   */
  fun analyzeContent(fileName: String, content: String): FileAnalysisResult {
    val bytes = content.toByteArray(Charsets.UTF_8)
    val size = bytes.size.toLong()

    // 1. Real Hashes
    val md5 = hashString("MD5", bytes)
    val sha256 = hashString("SHA-256", bytes)

    // 2. Real Shannon Entropy: H = -sum(P(i) * log2(P(i)))
    val entropy = calculateShannonEntropy(bytes)
    val entropyAssessment = when {
      entropy > 7.3 -> "Sangat Tinggi (${String.format(Locale.US, "%.2f", entropy)} / 8.0) - Indikasi Terenkripsi, Terkompresi, atau Obfuscated Packed"
      entropy > 6.0 -> "Moderat (${String.format(Locale.US, "%.2f", entropy)} / 8.0) - Karakter Campuran Biner & Teks"
      else -> "Rendah (${String.format(Locale.US, "%.2f", entropy)} / 8.0) - Teks Biasa / Konfigurasi Terbuka (Uncompressed)"
    }

    // 3. File Signature / Magic Bytes Detection
    val detectedFormat = detectFormat(content, bytes)

    // 4. Keyword Threat Matcher
    val lowerContent = content.lowercase(Locale.ROOT)
    val matches = mutableListOf<ThreatMatch>()
    threatDatabase.forEach { item ->
      if (lowerContent.contains(item.keyword.lowercase(Locale.ROOT))) {
        matches.add(item)
      }
    }

    // 5. Game Configuration Security Audit
    val configIssues = auditConfiguration(content)

    // 6. Overall Risk Scoring
    var score = 0
    matches.forEach {
      score += when (it.severity) {
        "CRITICAL" -> 35
        "HIGH" -> 20
        else -> 10
      }
    }
    configIssues.forEach {
      score += when (it.severity) {
        "CRITICAL" -> 30
        "HIGH" -> 20
        else -> 10
      }
    }
    val finalScore = score.coerceIn(0, 100)
    val riskLevel = when {
      finalScore >= 60 -> "BAHAYA TINGGI (Eksploit / Anomali Ditemukan)"
      finalScore >= 25 -> "PERLU PERHATIAN (Konfigurasi Lemah)"
      else -> "AMAN (Tidak Ditemukan Ancaman Signifikan)"
    }

    return FileAnalysisResult(
      fileName = fileName.ifBlank { "game_file_dump.bin" },
      byteSize = size,
      md5Hash = md5,
      sha256Hash = sha256,
      entropy = entropy,
      entropyAssessment = entropyAssessment,
      detectedFormat = detectedFormat,
      threatKeywordsFound = matches,
      configIssues = configIssues,
      overallRiskScore = finalScore,
      riskLevel = riskLevel
    )
  }

  private fun calculateShannonEntropy(bytes: ByteArray): Double {
    if (bytes.isEmpty()) return 0.0
    val frequencyMap = IntArray(256)
    for (b in bytes) {
      frequencyMap[b.toInt() and 0xFF]++
    }

    var entropy = 0.0
    val total = bytes.size.toDouble()
    for (count in frequencyMap) {
      if (count > 0) {
        val p = count / total
        entropy -= p * (log2(p))
      }
    }
    return entropy
  }

  private fun hashString(algorithm: String, bytes: ByteArray): String {
    val md = MessageDigest.getInstance(algorithm)
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
  }

  private fun detectFormat(content: String, bytes: ByteArray): String {
    val trimmed = content.trim()
    return when {
      bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() -> "ZIP / Android APK Package (PK..)"
      bytes.size >= 4 && bytes[0] == 0x7F.toByte() && bytes[1] == 0x45.toByte() && bytes[2] == 0x4C.toByte() -> "ELF Linux/Android Native Binary (.so)"
      bytes.size >= 3 && bytes[0] == 0x64.toByte() && bytes[1] == 0x65.toByte() && bytes[2] == 0x78.toByte() -> "Android Dalvik Executable (dex)"
      trimmed.startsWith("{") && trimmed.endsWith("}") -> "JSON Game Data / State Config"
      trimmed.startsWith("[") && trimmed.endsWith("]") -> "JSON Array Ledger"
      trimmed.startsWith("<?xml") || (trimmed.startsWith("<") && trimmed.endsWith(">")) -> "XML Game Manifest / Layout"
      trimmed.contains("=") && (trimmed.contains("[") || trimmed.lines().size > 1) -> "INI / Properties Configuration"
      else -> "Plaintext Script / Raw Binary Stream"
    }
  }

  private fun auditConfiguration(content: String): List<ConfigIssue> {
    val issues = mutableListOf<ConfigIssue>()
    val lines = content.lines()

    for (line in lines) {
      val lower = line.lowercase(Locale.ROOT).trim()

      if (lower.contains("insecure_http") && (lower.contains("true") || lower.contains("1"))) {
        issues.add(ConfigIssue(line, "true", "Mengizinkan protokol HTTP tanpa enkripsi (Rentan MitM)", "CRITICAL"))
      }
      if (lower.contains("verify_cooldown") && (lower.contains("false") || lower.contains("0"))) {
        issues.add(ConfigIssue(line, "false", "Verifikasi cooldown diserahkan ke client (Rentan Cooldown Hack)", "CRITICAL"))
      }
      if (lower.contains("authoritative_movement") && (lower.contains("false") || lower.contains("0"))) {
        issues.add(ConfigIssue(line, "false", "Server tidak memvalidasi posisi resmi (Rentan Speed & Teleport Hack)", "CRITICAL"))
      }
      if (lower.contains("debug_mode") && (lower.contains("true") || lower.contains("1"))) {
        issues.add(ConfigIssue(line, "true", "Mode debug aktif di lingkungan rilis produksi (Membuka log sensitif)", "HIGH"))
      }
      if (lower.contains("ssl_pinning") && (lower.contains("false") || lower.contains("0"))) {
        issues.add(ConfigIssue(line, "false", "SSL Pinning dinonaktifkan (Memudahkan sniffing paket proxy)", "HIGH"))
      }
      if (lower.contains("allow_backup") && lower.contains("true")) {
        issues.add(ConfigIssue(line, "true", "Pencadangan file aplikasi diizinkan (Data privat game dapat diekstrak)", "MEDIUM"))
      }
    }

    return issues
  }
}
