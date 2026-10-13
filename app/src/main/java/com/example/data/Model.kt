package com.example.data

/**
 * Data class representing a section within a learning module.
 */
data class ModuleSection(
  val heading: String,
  val body: String,
  val bulletPoints: List<String> = emptyList(),
  val codeExample: String? = null
)

/**
 * Data class for learning modules containing title, primary content,
 * an optional list of steps or points, and structured metadata.
 */
data class LearningModule(
  val id: Int = 0,
  val numberLabel: String = "",
  val title: String,
  val subtitle: String = "",
  val content: String = "",
  val steps: List<String>? = null,
  val sections: List<ModuleSection> = emptyList(),
  val clientResponsibilities: List<String> = emptyList(),
  val serverResponsibilities: List<String> = emptyList(),
  val keyTakeaway: String = ""
)

/**
 * Concise Data Class for learning modules with title, content,
 * and an optional list of steps.
 */
data class Module(
  val title: String,
  val content: String,
  val steps: List<String>? = null
)

data class QuizQuestion(
  val id: Int,
  val question: String,
  val options: List<String>,
  val correctIndex: Int,
  val explanation: String
)

data class TerminalLog(
  val id: Long,
  val isInput: Boolean,
  val text: String,
  val isError: Boolean = false
)

data class DetectionStep(
  val stepNumber: Int,
  val title: String,
  val formula: String,
  val explanation: String,
  val technicalNote: String
)

data class ReferenceItem(
  val id: String,
  val title: String,
  val summary: String,
  val keyTerms: List<String>,
  val industrialPractice: String
)

// === 4. Analisis Log Latihan ===
data class ForensicLogItem(
  val id: String,
  val timestamp: String,
  val userId: String,
  val action: String,
  val coordinates: String,
  val result: String,
  val source: String,
  val isSuspicious: Boolean = false,
  val anomalyReason: String = ""
)

// === 5. Kasus Investigasi & Claim Checker ===
data class EvidenceItem(
  val id: String,
  val title: String,
  val source: String,
  val timestamp: String,
  val detail: String,
  val isAuthentic: Boolean
)

data class InvestigationCase(
  val id: String,
  val title: String,
  val problemDescription: String,
  val investigationQuestion: String,
  val evidenceList: List<EvidenceItem>,
  val alternativeExplanations: List<String>,
  val actualFactSummary: String
)

enum class ClaimVerdict(val label: String) {
  SUPPORTED("Didukung bukti"),
  CONTRADICTED("Bertentangan dengan bukti"),
  INSUFFICIENT("Belum cukup bukti"),
  UNVERIFIABLE("Tidak dapat diverifikasi")
}

data class ClaimItem(
  val id: String,
  val claimText: String,
  val source: String,
  val date: String,
  val supportingEvidence: String,
  val opposingEvidence: String,
  val unknownAspects: String,
  val verdict: ClaimVerdict,
  val analystNotes: String
)

// === 6. Coding Academy ===
data class CodingExercise(
  val id: String,
  val levelNumber: Int,
  val levelName: String,
  val title: String,
  val description: String,
  val initialCode: String,
  val expectedOutput: String,
  val hint: String,
  val solutionCode: String
)

// === 7. Market Intelligence ===
data class CryptoAsset(
  val symbol: String,
  val name: String,
  val priceUsd: Double,
  val change24hPercent: Double,
  val volume24hUsd: String,
  val marketCapUsd: String,
  val circulatingSupply: String,
  val athUsd: Double,
  val atlUsd: Double,
  val provider: String = "Public API (Cached)",
  val isRealtime: Boolean = false,
  val lastUpdated: String,
  val chartPoints: List<Float>
)

data class StockAsset(
  val symbol: String,
  val name: String,
  val exchange: String,
  val price: Double,
  val currency: String,
  val changePercent: Double,
  val volume: String,
  val openPrice: Double,
  val highPrice: Double,
  val lowPrice: Double,
  val prevClose: Double,
  val range52w: String,
  val marketCap: String,
  val peRatio: Double,
  val eps: Double,
  val dividendYield: Double,
  val quoteStatus: String = "Delayed Quote (15 min)"
)

// === 8. Market Tools & Portfolio ===
data class PortfolioItem(
  val id: String,
  val symbol: String,
  val assetType: String, // "Crypto" or "Stock"
  val buyPrice: Double,
  val units: Double,
  val buyFee: Double,
  val currentPrice: Double,
  val purchaseDate: String
)

// === 10. Daily Streak & Contribution ===
data class StreakDay(
  val dateIso: String,
  val activityCount: Int
)

data class AchievementBadge(
  val id: String,
  val title: String,
  val description: String,
  val isUnlocked: Boolean
)

// === 11. Tools Hub (PDF Page 10-11) ===
enum class ToolCategory(val label: String) {
  SECURITY("Security"),
  CODING("Coding"),
  MARKET("Market"),
  DATA("Data"),
  PRODUCTIVITY("Productivity")
}

data class ToolConfig(
  val id: String,
  val name: String,
  val description: String,
  val category: ToolCategory,
  val baseUrl: String,
  val authType: String, // "None", "API Key (Server-side)", "OAuth 2.0"
  val allowedEndpoints: List<String>,
  val timeoutMs: Int = 5000,
  val enabled: Boolean = true,
  val lastHealthCheck: String = "Belum diuji",
  val isOnline: Boolean = true
)
