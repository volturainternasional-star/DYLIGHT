package com.example.tools.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

  private const val GITHUB_BASE_URL = "https://api.github.com/"

  private val loggingInterceptor by lazy {
    HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BASIC
    }
  }

  private val okHttpClient by lazy {
    OkHttpClient.Builder()
      .connectTimeout(10, TimeUnit.SECONDS)
      .readTimeout(10, TimeUnit.SECONDS)
      .addInterceptor(loggingInterceptor)
      .build()
  }

  val moshi: Moshi by lazy {
    Moshi.Builder()
      .add(KotlinJsonAdapterFactory())
      .build()
  }

  val retrofit: Retrofit by lazy {
    Retrofit.Builder()
      .baseUrl(GITHUB_BASE_URL)
      .client(okHttpClient)
      .addConverterFactory(MoshiConverterFactory.create(moshi))
      .build()
  }

  val securityToolApiService: SecurityToolApiService by lazy {
    retrofit.create(SecurityToolApiService::class.java)
  }
}
