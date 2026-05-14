package com.nammaskill.domain.model

data class UserModel(
    val userId: String = "",
    val name: String = "",
    val age: String = "",
    val gender: String = "",
    val email: String = "",
    val phone: String = "",
    val aadhaar: String = "",
    val role: String = "user"
)
