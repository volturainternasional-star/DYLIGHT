package com.example.tools.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GitHubSearchResponse(
  @Json(name = "total_count") val totalCount: Int = 0,
  @Json(name = "incomplete_results") val incompleteResults: Boolean = false,
  @Json(name = "items") val items: List<GitHubToolItemDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class GitHubToolItemDto(
  @Json(name = "id") val id: Long = 0L,
  @Json(name = "name") val name: String = "",
  @Json(name = "full_name") val fullName: String = "",
  @Json(name = "html_url") val htmlUrl: String = "",
  @Json(name = "description") val description: String? = null,
  @Json(name = "stargazers_count") val stargazersCount: Int = 0,
  @Json(name = "language") val language: String? = null,
  @Json(name = "forks_count") val forksCount: Int = 0,
  @Json(name = "open_issues_count") val openIssuesCount: Int = 0,
  @Json(name = "topics") val topics: List<String> = emptyList(),
  @Json(name = "homepage") val homepage: String? = null
)

@JsonClass(generateAdapter = true)
data class SecurityToolMetadataDto(
  @Json(name = "tool_id") val toolId: String,
  @Json(name = "name") val name: String,
  @Json(name = "category") val category: String,
  @Json(name = "description") val description: String,
  @Json(name = "tool_type") val toolType: String, // HASH_CHECKER, NETWORK_SNIFFER, etc.
  @Json(name = "base_url") val baseUrl: String,
  @Json(name = "documentation_url") val documentationUrl: String,
  @Json(name = "allowed_endpoints") val allowedEndpoints: List<String>,
  @Json(name = "auth_type") val authType: String = "Public API",
  @Json(name = "target_protocols") val targetProtocols: String = "TCP/UDP/IP"
)
