package com.inkwise.music.data.network.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String? = null
)

data class AuthResponse(
    val message: String,
    val token: String,
    val user: UserInfo
)

data class UserInfo(
    val id: Int,
    val username: String,
    val email: String?
)

data class ProfileResponse(
    val user: UserInfo
)

data class HealthResponse(
    val status: String
)

// 音乐列表
data class MusicItem(
    val id: Long,
    val title: String,
    val artist: ArtistInfo?,
    val album: String?,
    val genre: String?,
    val duration: Double,
    val format: String?,
    val oss_url: String?,
    val cover_url: String?,
    val download_url: String?,
    val stream_url: String?,
    val size: Long? = null,
    val bitrate: Int? = null,
    val sample_rate: Int? = null,
    val channels: Int? = null,
    val codec: String? = null
)

data class ArtistInfo(
    val id: Long,
    val name: String
)

data class Pagination(
    val page: Int,
    val page_size: Int,
    val total: Int,
    val total_pages: Int
)

data class MusicListResponse(
    val data: List<MusicItem>,
    val pagination: Pagination
)
