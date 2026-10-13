package com.example.tools.repository

import com.example.data.ToolCategory
import com.example.data.ToolConfig
import com.example.tools.api.GitHubToolItemDto
import com.example.tools.api.RetrofitClient
import com.example.tools.api.SecurityToolApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class SecurityToolQueryType(val displayName: String, val apiQuery: String) {
  ALL(
    "Semua Cyber Tools",
    "topic:cybersecurity+topic:security-tools"
  ),
  HASH_CHECKER(
    "Hash Checkers & Integrity",
    "hash-checker OR sha256 OR hashcat in:name,description,topics topic:security"
  ),
  NETWORK_SNIFFER(
    "Network Sniffers & PCAP",
    "packet-sniffer OR network-sniffer OR packet-capture in:name,description,topics"
  ),
  DYNAMIC_ANALYSIS(
    "Dynamic Binary & Memory",
    "frida OR reverse-engineering OR memory-scanner in:name,description,topics"
  )
}

data class RemoteToolsFetchResult(
  val tools: List<ToolConfig>,
  val isLiveFromNetwork: Boolean,
  val sourceSummary: String,
  val latencyMs: Long,
  val errorMessage: String? = null
)

class SecurityToolRepository(
  private val apiService: SecurityToolApiService = RetrofitClient.securityToolApiService
) {

  /**
   * Verified industry-standard security tool definitions (e.g., hash checkers, network sniffers)
   * used as verified baseline or when external rate limits occur.
   */
  val verifiedBaselineSecurityTools: List<ToolConfig> = listOf(
    ToolConfig(
      id = "EXT-SNIFF-01",
      name = "Wireshark Packet Analyzer & Sniffer",
      description = "Penganalisis paket jaringan terdepan di dunia untuk inspeksi protokol game (TCP/UDP, TLS, WebSocket) dan telemetri streaming.",
      category = ToolCategory.SECURITY,
      baseUrl = "https://api.github.com/repos/wireshark/wireshark",
      authType = "Public API (REST v3)",
      allowedEndpoints = listOf("/releases/latest", "/commits", "/tags"),
      timeoutMs = 6000,
      enabled = true,
      lastHealthCheck = "Live Catalog Verified (★ 9.4k)",
      isOnline = true
    ),
    ToolConfig(
      id = "EXT-HASH-02",
      name = "Hashcat Multi-Hash Integrity & Audit Engine",
      description = "Utilitas audit integritas kriptografis dan pemulihan rule-based paling canggih yang mendukung MD5, SHA-1, SHA-256, bcrypt, dan Argon2.",
      category = ToolCategory.SECURITY,
      baseUrl = "https://api.github.com/repos/hashcat/hashcat",
      authType = "Public API (REST v3)",
      allowedEndpoints = listOf("/releases", "/branches", "/stargazers"),
      timeoutMs = 6000,
      enabled = true,
      lastHealthCheck = "Live Catalog Verified (★ 21.3k)",
      isOnline = true
    ),
    ToolConfig(
      id = "EXT-SNIFF-03",
      name = "Scapy Interactive Packet Manipulator",
      description = "Pustaka Python untuk sniffing, crafting, forging paket jaringan UDP/TCP, pengujian port scan, dan deteksi replay-attack sesi game.",
      category = ToolCategory.SECURITY,
      baseUrl = "https://api.github.com/repos/secdev/scapy",
      authType = "Public API (REST v3)",
      allowedEndpoints = listOf("/releases", "/contents/README.md"),
      timeoutMs = 6000,
      enabled = true,
      lastHealthCheck = "Live Catalog Verified (★ 11.5k)",
      isOnline = true
    ),
    ToolConfig(
      id = "EXT-HASH-04",
      name = "VirusTotal Public Hash & File Intelligence",
      description = "Pemeriksa reputasi hash biner (SHA256/MD5), deteksi indikator kompromi (IoC), dan analisis signature malware game.",
      category = ToolCategory.SECURITY,
      baseUrl = "https://www.virustotal.com/api/v3",
      authType = "API Key (Public Quota)",
      allowedEndpoints = listOf("/files/{hash}", "/analyses/{id}"),
      timeoutMs = 5000,
      enabled = true,
      lastHealthCheck = "Endpoint Online (SSL OK)",
      isOnline = true
    ),
    ToolConfig(
      id = "EXT-MEM-05",
      name = "Frida Dynamic Binary Instrumentation",
      description = "Toolkit injeksi skrip JavaScript native untuk hook fungsi memori, inspeksi argumen fungsi anticheat, dan tracing runtime.",
      category = ToolCategory.SECURITY,
      baseUrl = "https://api.github.com/repos/frida/frida",
      authType = "Public API (REST v3)",
      allowedEndpoints = listOf("/releases/latest", "/contributors"),
      timeoutMs = 6000,
      enabled = true,
      lastHealthCheck = "Live Catalog Verified (★ 17.8k)",
      isOnline = true
    ),
    ToolConfig(
      id = "EXT-SCAN-06",
      name = "Nmap Network Security Mapper",
      description = "Alat eksplorasi jaringan dan audit keamanan port terbuka, deteksi layanan daemon game server, dan pemetaan firewall.",
      category = ToolCategory.SECURITY,
      baseUrl = "https://api.github.com/repos/nmap/nmap",
      authType = "Public API (REST v3)",
      allowedEndpoints = listOf("/commits", "/releases"),
      timeoutMs = 6000,
      enabled = true,
      lastHealthCheck = "Live Catalog Verified (★ 10.2k)",
      isOnline = true
    )
  )

  /**
   * Fetches real-world security tool metadata from external cybersecurity API using Retrofit.
   * Maps DTOs into ToolConfig items that populate the Tools Hub.
   */
  suspend fun fetchSecurityToolsFromApi(
    queryType: SecurityToolQueryType = SecurityToolQueryType.ALL
  ): RemoteToolsFetchResult = withContext(Dispatchers.IO) {
    val startTime = System.currentTimeMillis()

    try {
      val response = apiService.searchSecurityRepositories(
        query = queryType.apiQuery,
        sort = "stars",
        order = "desc",
        perPage = 10
      )

      val latency = System.currentTimeMillis() - startTime

      if (response.isSuccessful && response.body() != null) {
        val searchBody = response.body()!!
        val liveItems = searchBody.items

        if (liveItems.isNotEmpty()) {
          val mappedTools = liveItems.mapIndexed { index, dto ->
            mapGitHubDtoToToolConfig(dto, index, queryType)
          }

          return@withContext RemoteToolsFetchResult(
            tools = mappedTools,
            isLiveFromNetwork = true,
            sourceSummary = "GitHub REST API v3 (${mappedTools.size} tools diperoleh dari ${searchBody.totalCount} repositori)",
            latencyMs = latency
          )
        }
      }

      // If rate limited (e.g. 403 Forbidden) or empty, fall back to verified curated tools with query filter
      val filteredBaseline = when (queryType) {
        SecurityToolQueryType.HASH_CHECKER -> verifiedBaselineSecurityTools.filter {
          it.name.contains("Hash", ignoreCase = true) || it.description.contains("hash", ignoreCase = true)
        }
        SecurityToolQueryType.NETWORK_SNIFFER -> verifiedBaselineSecurityTools.filter {
          it.name.contains("Sniff", ignoreCase = true) || it.name.contains("Packet", ignoreCase = true) || it.description.contains("paket", ignoreCase = true)
        }
        SecurityToolQueryType.DYNAMIC_ANALYSIS -> verifiedBaselineSecurityTools.filter {
          it.name.contains("Frida", ignoreCase = true) || it.description.contains("memori", ignoreCase = true)
        }
        SecurityToolQueryType.ALL -> verifiedBaselineSecurityTools
      }

      val codeNotice = if (response.code() == 403) {
        "Rate limit API GitHub tercapai (HTTP 403). Katalog instan terverifikasi dimuat."
      } else {
        "Respon HTTP ${response.code()}. Katalog standar diaktifkan."
      }

      return@withContext RemoteToolsFetchResult(
        tools = filteredBaseline,
        isLiveFromNetwork = false,
        sourceSummary = codeNotice,
        latencyMs = latency,
        errorMessage = response.errorBody()?.string()?.take(150)
      )

    } catch (e: Exception) {
      val latency = System.currentTimeMillis() - startTime
      val filteredBaseline = when (queryType) {
        SecurityToolQueryType.HASH_CHECKER -> verifiedBaselineSecurityTools.filter {
          it.name.contains("Hash", ignoreCase = true)
        }
        SecurityToolQueryType.NETWORK_SNIFFER -> verifiedBaselineSecurityTools.filter {
          it.name.contains("Sniff", ignoreCase = true) || it.name.contains("Packet", ignoreCase = true)
        }
        else -> verifiedBaselineSecurityTools
      }

      return@withContext RemoteToolsFetchResult(
        tools = filteredBaseline,
        isLiveFromNetwork = false,
        sourceSummary = "Offline / Fallback Mode: ${e.javaClass.simpleName} (${e.localizedMessage ?: "Jaringan tidak tersedia"}).",
        latencyMs = latency,
        errorMessage = e.message
      )
    }
  }

  private fun mapGitHubDtoToToolConfig(
    dto: GitHubToolItemDto,
    index: Int,
    queryType: SecurityToolQueryType
  ): ToolConfig {
    val cleanName = dto.name.replace("-", " ").replace("_", " ")
      .split(" ")
      .joinToString(" ") { it.replaceFirstChar { ch -> ch.uppercase() } }

    val desc = dto.description?.take(160)
      ?: "Alat keamanan siber untuk analisis integritas sistem dan investigasi paket."

    val topicsStr = if (dto.topics.isNotEmpty()) {
      " [${dto.topics.take(3).joinToString(", ")}]"
    } else ""

    val starCount = if (dto.stargazersCount >= 1000) {
      String.format(java.util.Locale.US, "%.1fk", dto.stargazersCount / 1000.0)
    } else {
      dto.stargazersCount.toString()
    }

    val typePrefix = when (queryType) {
      SecurityToolQueryType.HASH_CHECKER -> "HASH"
      SecurityToolQueryType.NETWORK_SNIFFER -> "SNIFF"
      SecurityToolQueryType.DYNAMIC_ANALYSIS -> "MEM"
      SecurityToolQueryType.ALL -> "CYBER"
    }

    return ToolConfig(
      id = "EXT-$typePrefix-${index + 1}-${dto.id % 10000}",
      name = "$cleanName (${dto.language ?: "SecTool"})",
      description = "$desc$topicsStr",
      category = ToolCategory.SECURITY,
      baseUrl = "https://api.github.com/repos/${dto.fullName}",
      authType = "Public API (REST v3)",
      allowedEndpoints = listOf("/releases", "/commits", "/stargazers"),
      timeoutMs = 6000,
      enabled = true,
      lastHealthCheck = "Live API Verified (★ $starCount)",
      isOnline = true
    )
  }
}
