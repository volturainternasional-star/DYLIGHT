package com.example

import com.example.data.ToolCategory
import com.example.tools.api.GitHubSearchResponse
import com.example.tools.api.GitHubToolItemDto
import com.example.tools.api.RetrofitClient
import com.example.tools.api.SecurityToolApiService
import com.example.tools.repository.SecurityToolQueryType
import com.example.tools.repository.SecurityToolRepository
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class SecurityToolRepositoryTest {

  @Test
  fun testRetrofitClientInitialization() {
    assertNotNull(RetrofitClient.retrofit)
    assertNotNull(RetrofitClient.securityToolApiService)
    assertNotNull(RetrofitClient.moshi)
  }

  @Test
  fun testVerifiedBaselineSecurityToolsContainsExpectedCategories() {
    val repository = SecurityToolRepository()
    val baseline = repository.verifiedBaselineSecurityTools

    assertTrue("Baseline tools should not be empty", baseline.isNotEmpty())

    // Must contain network sniffers
    val hasSniffer = baseline.any {
      it.name.contains("Wireshark", ignoreCase = true) ||
        it.description.contains("paket", ignoreCase = true)
    }
    assertTrue("Baseline should contain network sniffer tool like Wireshark", hasSniffer)

    // Must contain hash checkers
    val hasHashChecker = baseline.any {
      it.name.contains("Hashcat", ignoreCase = true) ||
        it.description.contains("MD5", ignoreCase = true) ||
        it.name.contains("VirusTotal", ignoreCase = true)
    }
    assertTrue("Baseline should contain hash checker tool", hasHashChecker)

    // Verify all tools conform to security standards
    baseline.forEach { tool ->
      assertEquals(ToolCategory.SECURITY, tool.category)
      assertTrue("URL must be HTTPS: ${tool.baseUrl}", tool.baseUrl.startsWith("https://"))
      assertTrue("Tool must specify allowed endpoints", tool.allowedEndpoints.isNotEmpty())
      assertTrue("Tool ID must start with EXT-", tool.id.startsWith("EXT-"))
    }
  }

  @Test
  fun testFetchWithMockedSuccessfulApi() = runBlocking {
    val fakeDto = GitHubToolItemDto(
      id = 101L,
      name = "game-packet-sniffer",
      fullName = "secdev/game-packet-sniffer",
      htmlUrl = "https://github.com/secdev/game-packet-sniffer",
      description = "Fast real-time game network packet sniffer and UDP inspector",
      stargazersCount = 3500,
      language = "Rust",
      topics = listOf("packet-sniffer", "cybersecurity")
    )

    val fakeApiService = object : SecurityToolApiService {
      override suspend fun searchSecurityRepositories(
        query: String,
        sort: String,
        order: String,
        perPage: Int
      ): Response<GitHubSearchResponse> {
        return Response.success(
          GitHubSearchResponse(
            totalCount = 1,
            incompleteResults = false,
            items = listOf(fakeDto)
          )
        )
      }

      override suspend fun getRepositoryDetails(
        owner: String,
        repo: String
      ): Response<GitHubToolItemDto> {
        return Response.success(fakeDto)
      }

      override suspend fun fetchCustomToolsCatalog(
        customUrl: String
      ): Response<List<com.example.tools.api.SecurityToolMetadataDto>> {
        return Response.success(emptyList())
      }
    }

    val repository = SecurityToolRepository(fakeApiService)
    val result = repository.fetchSecurityToolsFromApi(SecurityToolQueryType.NETWORK_SNIFFER)

    assertTrue("Result must be marked live from network", result.isLiveFromNetwork)
    assertEquals(1, result.tools.size)
    val tool = result.tools[0]
    assertTrue(tool.name.contains("Game Packet Sniffer", ignoreCase = true))
    assertTrue(tool.description.contains("Fast real-time game network packet sniffer"))
    assertEquals("https://api.github.com/repos/secdev/game-packet-sniffer", tool.baseUrl)
    assertEquals(ToolCategory.SECURITY, tool.category)
    assertTrue(tool.lastHealthCheck.contains("★ 3.5k"))
  }

  @Test
  fun testFetchWithApiErrorGracefulFallback() = runBlocking {
    val errorApiService = object : SecurityToolApiService {
      override suspend fun searchSecurityRepositories(
        query: String,
        sort: String,
        order: String,
        perPage: Int
      ): Response<GitHubSearchResponse> {
        val errorBody = "{\"message\": \"API rate limit exceeded\"}".toResponseBody("application/json".toMediaTypeOrNull())
        return Response.error(403, errorBody)
      }

      override suspend fun getRepositoryDetails(owner: String, repo: String): Response<GitHubToolItemDto> {
        return Response.error(404, "".toResponseBody(null))
      }

      override suspend fun fetchCustomToolsCatalog(customUrl: String): Response<List<com.example.tools.api.SecurityToolMetadataDto>> {
        return Response.error(500, "".toResponseBody(null))
      }
    }

    val repository = SecurityToolRepository(errorApiService)
    val result = repository.fetchSecurityToolsFromApi(SecurityToolQueryType.HASH_CHECKER)

    assertFalse("Must be graceful fallback when HTTP 403 occurs", result.isLiveFromNetwork)
    assertTrue("Should return hash tools from curated catalog", result.tools.isNotEmpty())
    assertTrue(result.tools.any { it.name.contains("Hash", ignoreCase = true) })
    assertTrue(result.sourceSummary.contains("HTTP 403") || result.sourceSummary.contains("Rate limit"))
  }
}
