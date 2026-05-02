package com.inkwise.music.data.network

import com.inkwise.music.data.network.model.AuthResponse
import com.inkwise.music.data.network.model.HealthResponse
import com.inkwise.music.data.network.model.LoginRequest
import com.inkwise.music.data.network.model.ProfileResponse
import com.inkwise.music.data.network.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponse>

    @GET("/health")
    suspend fun healthCheck(): Response<HealthResponse>
}
