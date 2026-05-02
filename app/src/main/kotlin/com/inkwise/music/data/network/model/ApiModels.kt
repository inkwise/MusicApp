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
