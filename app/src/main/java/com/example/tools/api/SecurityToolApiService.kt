package com.example.tools.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Retrofit Service to fetch real-world security tool metadata (hash checkers, network sniffers, etc.)
 * from external cybersecurity APIs and open developer directories.
 */
interface SecurityToolApiService {

  /**
   * Search external cybersecurity tool repositories (e.g. hash-checkers, packet-sniffers).
   */
  @Headers(
    "Accept: application/vnd.github.v3+json",
    "User-Agent: GameSecLab-Android/1.0"
  )
  @GET("search/repositories")
  suspend fun searchSecurityRepositories(
    @Query("q") query: String,
    @Query("sort") sort: String = "stars",
    @Query("order") order: String = "desc",
    @Query("per_page") perPage: Int = 15
  ): Response<GitHubSearchResponse>

  /**
   * Fetch specific tool repository metadata by owner and repository name.
   */
  @Headers(
    "Accept: application/vnd.github.v3+json",
    "User-Agent: GameSecLab-Android/1.0"
  )
  @GET("repos/{owner}/{repo}")
  suspend fun getRepositoryDetails(
    @Path("owner") owner: String,
    @Path("repo") repo: String
  ): Response<GitHubToolItemDto>

  /**
   * Generic URL loader for cybersecurity metadata catalogs / JSON feeds.
   */
  @Headers("User-Agent: GameSecLab-Android/1.0")
  @GET
  suspend fun fetchCustomToolsCatalog(
    @Url customUrl: String
  ): Response<List<SecurityToolMetadataDto>>
}
