package com.nammaskill.domain.model

data class AdminModel(
    val adminId: String = "",
    val skillCenterName: String = "",
    val email: String = "",
    val address: String = "",
    val latitude: Double = 12.9716, // Default to Bengaluru
    val longitude: Double = 77.5946,
    val role: String = "admin",
    val createdAt: Long = System.currentTimeMillis()
)
