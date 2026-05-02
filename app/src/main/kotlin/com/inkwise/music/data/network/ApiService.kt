package com.inkwise.music.data.network

import com.inkwise.music.data.network.model.AuthResponse
import com.inkwise.music.data.network.model.HealthResponse
import com.inkwise.music.data.network.model.LoginRequest
import com.inkwise.music.data.network.model.CreatePlaylistRequest
import com.inkwise.music.data.network.model.MusicListResponse
import com.inkwise.music.data.network.model.PlaylistListResponse
import com.inkwise.music.data.network.model.PlaylistResponse
import com.inkwise.music.data.network.model.ProfileResponse
import com.inkwise.music.data.network.model.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("/profile")
    suspend fun getProfile(@Header("Authorization") token: String): Response<ProfileResponse>

    @GET("/health")
    suspend fun healthCheck(): Response<HealthResponse>

    @GET("/music/list")
    suspend fun getMusicList(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null
    ): Response<MusicListResponse>

    @POST("/playlists")
    suspend fun createPlaylist(
        @Header("Authorization") token: String,
        @Body request: CreatePlaylistRequest
    ): Response<PlaylistResponse>

    @GET("/playlists")
    suspend fun getPlaylists(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 50
    ): Response<PlaylistListResponse>
}
